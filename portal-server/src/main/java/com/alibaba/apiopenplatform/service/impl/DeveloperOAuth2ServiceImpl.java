/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package com.alibaba.apiopenplatform.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.entity.Developer;
import com.alibaba.apiopenplatform.entity.DeveloperExternalIdentity;
import com.alibaba.apiopenplatform.repository.DeveloperExternalIdentityRepository;
import com.alibaba.apiopenplatform.repository.DeveloperRepository;
import com.alibaba.apiopenplatform.core.constant.Common;
import com.alibaba.apiopenplatform.service.gateway.factory.HTTPClientFactory;
import com.alibaba.apiopenplatform.service.DeveloperOAuth2Service;
import com.alibaba.apiopenplatform.service.DeveloperService;
import com.alibaba.apiopenplatform.service.PortalService;
import com.alibaba.apiopenplatform.support.portal.OidcConfig;
import com.alibaba.apiopenplatform.support.portal.PortalSettingConfig;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 开发者OAuth2服务实现类
 *
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeveloperOAuth2ServiceImpl implements DeveloperOAuth2Service {

    private final DeveloperRepository developerRepository;
    private final DeveloperExternalIdentityRepository developerExternalIdentityRepository;
    private final DeveloperService developerService;
    private final PortalService portalService;
    private final ContextHolder contextHolder;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        this.restTemplate = HTTPClientFactory.createRestTemplate();
    }

    @Override
    public void handleAuthorize(String provider, String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String portalId = contextHolder.getPortal();
        OidcConfig config = findOidcConfig(portalId, provider);

        if (config == null || !config.isEnabled()) {
            log.error("[OIDC配置未启用] provider={}", provider);
            throw new BusinessException(ErrorCode.OIDC_CONFIG_DISABLED);
        }

        String redirectUri = generateRedirectUri(request);
        String url = buildAuthorizationUrl(config, redirectUri, state);
        response.sendRedirect(url);
    }

    @Override
    public void handleCallback(String code, String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String portalId = contextHolder.getPortal();
        if (portalId == null) {
            response.sendRedirect("/?login=fail&msg=" + URLEncoder.encode("无法识别门户信息", "UTF-8"));
            return;
        }

        CallbackContext callbackContext = parseCallbackState(state);
        OidcConfig config = findOidcConfig(portalId, callbackContext.getProvider());
        if (config == null || !config.isEnabled()) {
            response.sendRedirect("/?login=fail&msg=" + URLEncoder.encode("OIDC配置未启用", "UTF-8"));
            return;
        }

        Map<String, Object> userInfoMap = fetchUserInfoMap(code, config, request);
        if (userInfoMap == null) {
            response.sendRedirect("/?login=fail&msg=" + URLEncoder.encode("获取用户信息失败", "UTF-8"));
            return;
        }

        UserInfo userInfo = extractUserInfo(userInfoMap);
        String rawInfoJson = new ObjectMapper().writeValueAsString(userInfoMap);

        if ("BINDING".equals(callbackContext.getMode())) {
            handleBindingMode(callbackContext.getProvider(), userInfo, rawInfoJson, portalId, response);
        } else {
            handleLoginMode(callbackContext.getProvider(), userInfo, rawInfoJson, callbackContext.getApiPrefix(), request, response);
        }
    }

    @Override
    public List<Map<String, Object>> listCurrentUserIdentities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();

        Optional<Developer> devOpt = developerRepository.findByDeveloperId(userId);
        if (!devOpt.isPresent()) {
            throw new BusinessException(ErrorCode.DEVELOPER_UNAUTHORIZED);
        }

        List<DeveloperExternalIdentity> identities = developerExternalIdentityRepository.findByDeveloper_DeveloperId(devOpt.get().getDeveloperId());

        return identities.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> listOidcProviders() {
        String portalId = contextHolder.getPortal();
        PortalResult portal = portalService.getPortal(portalId);
        PortalSettingConfig portalSetting = portal.getPortalSettingConfig();

        if (CollUtil.isEmpty(portalSetting.getOidcConfigs())) {
            return Collections.emptyList();
        }

        return portalSetting.getOidcConfigs().stream()
                .filter(OidcConfig::isEnabled)
                .map(this::convertOidcConfigToMap)
                .collect(Collectors.toList());
    }

    @Override
    public String generateRedirectUri(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        String baseUrl = scheme + "://" + serverName;
        if (serverPort != 80 && serverPort != 443) {
            baseUrl += ":" + serverPort;
        }

        String requestURI = request.getRequestURI();

        if (requestURI.contains("/api/v1/")) {
            return baseUrl + "/api/v1/developers/callback";
        } else {
            String state = request.getParameter("state");
            if (state != null && state.contains("API_PREFIX=/api/v1")) {
                return baseUrl + "/api/v1/developers/callback";
            } else {
                return baseUrl + "/developers/callback";
            }
        }
    }

    // 私有辅助方法
    private OidcConfig findOidcConfig(String portalId, String provider) {
        PortalResult portal = portalService.getPortal(portalId);
        PortalSettingConfig portalSetting = portal.getPortalSettingConfig();

        if (portalSetting == null || portalSetting.getOidcConfigs() == null) {
            return null;
        }

        return portalSetting.getOidcConfigs().stream()
                .filter(config -> config != null &&
                        config.getProvider() != null &&
                        provider.equals(config.getProvider()) &&
                        config.isEnabled())
                .findFirst()
                .orElse(null);
    }

    private String buildAuthorizationUrl(OidcConfig config, String redirectUri, String state) throws UnsupportedEncodingException {
        return config.getAuthorizationEndpoint()
                + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                + "&scope=" + URLEncoder.encode(config.getScopes(), "UTF-8")
                + "&response_type=code"
                + "&state=" + URLEncoder.encode(state, "UTF-8");
    }

    private CallbackContext parseCallbackState(String state) throws UnsupportedEncodingException {
        String decodedState = URLDecoder.decode(state, "UTF-8");
        String[] stateParts = decodedState.split("\\|");

        // 简化日志：不打印敏感/冗长信息

        CallbackContext context = new CallbackContext();

        if (decodedState.startsWith("BINDING|")) {
            if (stateParts.length >= 4) {
                context.setMode("BINDING");
                context.setProvider(stateParts[2]);
            }
        } else if (decodedState.startsWith("LOGIN|")) {
            if (stateParts.length >= 3) {  // 修复：只需要3个部分
                context.setMode("LOGIN");
                context.setProvider(stateParts[1]);

                for (String part : stateParts) {
                    if (part.startsWith("API_PREFIX=")) {
                        context.setApiPrefix(part.substring("API_PREFIX=".length()));
                        break;
                    }
                }
            }
        }

        return context;
    }

    private UserInfo extractUserInfo(Map<String, Object> userInfoMap) {
        String providerSubject = String.valueOf(userInfoMap.get("id"));
        if (providerSubject == null || "null".equals(providerSubject)) {
            Object subValue = userInfoMap.get("sub");
            if (subValue != null) {
                providerSubject = String.valueOf(subValue);
            }
        }

        String displayName = (String) userInfoMap.get("name");
        if (displayName == null || "null".equals(displayName)) {
            Object loginValue = userInfoMap.get("login");
            if (loginValue != null) {
                displayName = String.valueOf(loginValue);
            }
        }

        String email = (String) userInfoMap.get("email");

        return new UserInfo(providerSubject, displayName, email);
    }

    private void handleBindingMode(String provider, UserInfo userInfo, String rawInfoJson, String portalId, HttpServletResponse response) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();

        developerService.bindExternalIdentity(userId, provider, userInfo.getProviderSubject(),
                userInfo.getDisplayName(), rawInfoJson, portalId);

        response.sendRedirect("/settings/account?bind=success");
    }

    private void handleLoginMode(String provider, UserInfo userInfo, String rawInfoJson, String apiPrefix,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<AuthResponseResult> loginResult = developerService.handleExternalLogin(
                provider, userInfo.getProviderSubject(), userInfo.getEmail(),
                userInfo.getDisplayName(), rawInfoJson);

        if (loginResult.isPresent()) {
            String token = loginResult.get().getToken();
            Cookie tokenCookie = new Cookie(Common.AUTH_TOKEN_COOKIE, token);
            tokenCookie.setPath("/");
            tokenCookie.setHttpOnly(false); // 允许JavaScript访问
            tokenCookie.setMaxAge(3600); // 1小时过期
            response.addCookie(tokenCookie);

            String redirectUrl;
            if (apiPrefix != null && apiPrefix.startsWith("/")) {
                String protocol = request.getScheme();
                String serverName = request.getHeader("Host");
                if (serverName == null || serverName.isEmpty()) {
                    serverName = request.getServerName();
                }
                if (serverName.contains(":")) {
                    serverName = serverName.split(":")[0];
                }
                redirectUrl = protocol + "://" + serverName + "/?login=success&fromCookie=true";
            } else {
                redirectUrl = "/?login=success&fromCookie=true";
            }

            response.sendRedirect(redirectUrl);
        } else {
            log.warn("[OIDCCallback] 登录失败: provider={}", provider);
            response.sendRedirect("/?login=fail&msg=" + URLEncoder.encode("三方登录失败", "UTF-8"));
        }
    }

    private void setAuthCookie(HttpServletResponse response, String token) {
        Cookie tokenCookie = new Cookie("auth_token", token);
        tokenCookie.setPath("/");
        tokenCookie.setHttpOnly(false);
        tokenCookie.setMaxAge(3600);
        response.addCookie(tokenCookie);
    }

    private String buildLoginSuccessRedirectUrl(HttpServletRequest request, String apiPrefix) {
        if (apiPrefix != null && apiPrefix.startsWith("/")) {
            String protocol = request.getScheme();
            String serverName = request.getHeader("Host");
            if (serverName == null || serverName.isEmpty()) {
                serverName = request.getServerName();
            }
            if (serverName.contains(":")) {
                serverName = serverName.split(":")[0];
            }
            return protocol + "://" + serverName + "/?login=success&fromCookie=true";
        } else {
            return "/?login=success&fromCookie=true";
        }
    }

    private Map<String, Object> convertToMap(DeveloperExternalIdentity identity) {
        Map<String, Object> map = new HashMap<>();
        map.put("provider", identity.getProvider());
        map.put("subject", identity.getSubject());
        map.put("displayName", identity.getDisplayName());
        map.put("rawInfoJson", identity.getRawInfoJson());
        return map;
    }

    private Map<String, Object> convertOidcConfigToMap(OidcConfig config) {
        Map<String, Object> map = new HashMap<>();
        map.put("provider", config.getProvider());
        map.put("displayName", config.getName());
        map.put("icon", config.getLogoUrl());
        map.put("enabled", config.isEnabled());
        return map;
    }

    private Map<String, Object> fetchUserInfoMap(String code, OidcConfig config, HttpServletRequest request) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        String redirectUri = generateRedirectUri(request);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "Portal-Management/1.0");
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                ResponseEntity<Map> tokenResp = restTemplate.postForEntity(config.getTokenEndpoint(), entity, Map.class);
                String accessToken = (String) tokenResp.getBody().get("access_token");
                if (accessToken == null) {
                    return null;
                }
                HttpHeaders userHeaders = new HttpHeaders();
                userHeaders.setBearerAuth(accessToken);
                userHeaders.set("User-Agent", "Portal-Management/1.0");
                HttpEntity<Void> userEntity = new HttpEntity<>(userHeaders);
                ResponseEntity<Map> userResp = restTemplate.exchange(config.getUserInfoEndpoint(), HttpMethod.GET, userEntity, Map.class);
                return userResp.getBody();
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw e;
                }
                try {
                    Thread.sleep(2000L * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试中断", ie);
                }
            }
        }

        return null;
    }


    // 内部类
    private static class CallbackContext {
        private String mode;
        private String provider;
        private String apiPrefix;

        // getters and setters
        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getApiPrefix() {
            return apiPrefix;
        }

        public void setApiPrefix(String apiPrefix) {
            this.apiPrefix = apiPrefix;
        }
    }

    private static class UserInfo {
        private final String providerSubject;
        private final String displayName;
        private final String email;

        public UserInfo(String providerSubject, String displayName, String email) {
            this.providerSubject = providerSubject;
            this.displayName = displayName;
            this.email = email;
        }

        public String getProviderSubject() {
            return providerSubject;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEmail() {
            return email;
        }
    }
}
