package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.result.PortalResult;
import com.alibaba.apiopenplatform.entity.Developer;
import com.alibaba.apiopenplatform.entity.DeveloperExternalIdentity;
import com.alibaba.apiopenplatform.repository.DeveloperExternalIdentityRepository;
import com.alibaba.apiopenplatform.repository.DeveloperRepository;
import com.alibaba.apiopenplatform.service.DeveloperService;
import com.alibaba.apiopenplatform.service.PortalService;
import com.alibaba.apiopenplatform.support.portal.OidcConfig;
import com.alibaba.apiopenplatform.support.portal.PortalSettingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import com.alibaba.apiopenplatform.auth.JwtService;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;

import java.net.URLDecoder;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import javax.servlet.http.Cookie;
import javax.annotation.PostConstruct;

/**
 * @author zxd
 */
@Slf4j
@Tag(name = "开发者OAuth2登录管理", description = "开发者OAuth2统一回调与外部身份绑定相关接口")
@RestController
@RequestMapping("/developers")
@RequiredArgsConstructor
public class DeveloperOauth2Controller {

    private final DeveloperRepository developerRepository;
    private final DeveloperExternalIdentityRepository developerExternalIdentityRepository;
    private final DeveloperService developerService;
    private final PortalService portalService;
    private final ContextHolder contextHolder;
    
    private RestTemplate restTemplate;
    
    @PostConstruct
    public void init() {
        this.restTemplate = createRestTemplateWithSSLBypass();
    }

    @Operation(summary = "OIDC授权入口", description = "重定向到第三方登录页面")
    @GetMapping("/authorize")
    public void universalAuthorize(@RequestParam String provider, @RequestParam String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String portalId = contextHolder.getPortal();
        String newState = state;
        PortalResult portal = portalService.getPortal(portalId);
        PortalSettingConfig portalSetting = portal.getPortalSettingConfig();
        java.util.List<PortalSettingConfig> settings = Collections.singletonList(portalSetting);
        OidcConfig config = null;
        for (PortalSettingConfig setting : settings) {
            if (setting.getOidcConfigs() != null) {
                for (OidcConfig c : setting.getOidcConfigs()) {
                    if (provider.equals(c.getProvider())) {
                        config = c;
                        break;
                    }
                }
            }
            if (config != null) break;
        }
        if (config == null || !config.isEnabled()) {
            log.error("[OIDC配置未启用] provider={}, configs={}", provider, settings);
            throw new BusinessException(ErrorCode.OIDC_CONFIG_DISABLED);
        }
        String redirectUri = generateRedirectUri(request);
        
        String decodedState = URLDecoder.decode(newState, "UTF-8");
        if (decodedState.contains("API_PREFIX=")) {
            String[] parts = decodedState.split("\\|");
            for (String part : parts) {
                if (part.startsWith("API_PREFIX=")) {
                    String apiPrefix = part.substring("API_PREFIX=".length());
                    if (apiPrefix.startsWith("/")) {
                        String protocol = request.getScheme();
                        String serverName = request.getHeader("Host");
                        if (serverName == null || serverName.isEmpty()) {
                            serverName = request.getServerName();
                        }
                        if (serverName.contains(":")) {
                            serverName = serverName.split(":")[0];
                        }
                        redirectUri = protocol + "://" + serverName + apiPrefix + "/developers/callback";
                    }
                    break;
                }
            }
        }
        
        String url = config.getAuthorizationEndpoint()
                + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                + "&scope=" + URLEncoder.encode(config.getScopes(), "UTF-8")
                + "&response_type=code"
                + "&state=" + URLEncoder.encode(newState, "UTF-8");
        response.sendRedirect(url);
    }

    @Operation(summary = "OIDC统一回调", description = "处理第三方登录回调")
    @GetMapping("/callback")
    public void oidcCallback(@RequestParam String code, @RequestParam String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            log.info("[OIDCCallback] code={}, state={}", code, state);
            String portalId = contextHolder.getPortal();
            log.info("[OIDCCallback] portalId={}", portalId);
            String provider = null;
            String mode = null;
            String apiPrefix = null;
            String decodedState = URLDecoder.decode(state, "UTF-8");
            String[] stateParts = decodedState.split("\\|");
            if (decodedState.startsWith("BINDING|")) {
                String[] arr = decodedState.split("\\|");
                if (arr.length >= 4) {
                    provider = arr[2];
                    mode = "BINDING";
                }
            } else if (decodedState.startsWith("LOGIN|")) {
                String[] arr = decodedState.split("\\|");
                if (arr.length >= 2) {
                    provider = arr[1];
                    mode = "LOGIN";
                    
                    for (String part : arr) {
                        if (part.startsWith("API_PREFIX=")) {
                            apiPrefix = part.substring("API_PREFIX=".length());
                            break;
                        }
                    }
                }
            }
            log.info("[OIDCCallback] 解析结果: provider={}, mode={}, apiPrefix={}", provider, mode, apiPrefix);
            if (portalId == null) {
                response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("无法识别门户信息", "UTF-8"));
                return;
            }
            PortalResult portal = portalService.getPortal(portalId);
            PortalSettingConfig portalSetting = portal.getPortalSettingConfig();
            if (portalSetting == null) {
                log.error("[PortalSetting不存在] portalId={}", portalId);
                response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("门户配置不存在", "UTF-8"));
                return;
            }
            java.util.List<PortalSettingConfig> settings = java.util.Arrays.asList(portalSetting);
            OidcConfig config = null;
            for (PortalSettingConfig setting : settings) {
                if (setting.getOidcConfigs() != null) {
                    for (OidcConfig c : setting.getOidcConfigs()) {
                        if (provider.equals(c.getProvider())) {
                            config = c;
                            break;
                        }
                    }
                }
                if (config != null) break;
            }
            if (config == null || !config.isEnabled()) {
                log.error("[OIDC配置未启用] provider={}, configs={}", provider, settings);
                response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("OIDC配置未启用", "UTF-8"));
                return;
            }
            log.info("[OIDCCallback] 开始获取用户信息");
            Map<String, Object> userInfoMap = fetchUserInfoMap(code, config, request);
            log.info("[OIDCCallback] 获取到用户信息: {}", userInfoMap);
            if (userInfoMap == null) {
                response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("获取用户信息失败", "UTF-8"));
                return;
            }
            String providerSubject = String.valueOf(userInfoMap.get("id"));
            String displayName = (String) userInfoMap.get("name");
            
            if (providerSubject == null || "null".equals(providerSubject)) {
                Object subValue = userInfoMap.get("sub");
                if (subValue != null) {
                    providerSubject = String.valueOf(subValue);
                }
            }
            
            if (displayName == null || "null".equals(displayName)) {
                Object loginValue = userInfoMap.get("login");
                if (loginValue != null) {
                    displayName = String.valueOf(loginValue);
                }
            }
            
            String email = (String) userInfoMap.get("email");
            
            String rawInfoJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(userInfoMap);
            log.info("[OIDCCallback] 解析用户信息: providerSubject={}, displayName={}", providerSubject, displayName);
            if (providerSubject == null) {
                response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("用户信息解析失败", "UTF-8"));
                return;
            }
            if ("BINDING".equals(mode)) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String userId = (String) authentication.getPrincipal();
                developerService.bindExternalIdentity(userId, provider, providerSubject, displayName, rawInfoJson, portalId);
                response.sendRedirect("/settings/account?bind=success");
                return;
            } else {
                Optional<AuthResponseResult> loginResult = developerService.handleExternalLogin(provider, providerSubject, email, displayName, rawInfoJson);
                log.info("[OIDCCallback] handleExternalLogin结果: present={}", loginResult.isPresent());
                if (loginResult.isPresent()) {
                    String token = loginResult.get().getToken();
                    log.info("[OIDCCallback] 生成的token长度: {}", token != null ? token.length() : 0);
                    
                    Cookie tokenCookie = new Cookie("auth_token", token);
                    tokenCookie.setPath("/");
                    tokenCookie.setHttpOnly(false);
                    tokenCookie.setMaxAge(3600);
                    response.addCookie(tokenCookie);
                    log.info("[OIDCCallback] 已设置auth_token Cookie");
                    
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
                    log.error("[OIDCCallback] handleExternalLogin返回空结果");
                    response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("三方登录失败", "UTF-8"));
                }
            }
        } catch (Exception e) {
            log.error("[OIDCCallback] 处理OAuth回调时发生异常", e);
            response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("处理回调时发生异常: " + e.getMessage(), "UTF-8"));
        }
    }

    @Operation(summary = "查询当前用户所有外部身份绑定", description = "只返回provider、subject、displayName、rawInfoJson")
    @PostMapping("/list-identities")
    public List<Map<String, Object>> listIdentities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();
        Optional<Developer> devOpt = developerRepository.findByDeveloperId(userId);
        if (!devOpt.isPresent()) {
            throw new BusinessException(ErrorCode.DEVELOPER_UNAUTHORIZED);
        }
        List<DeveloperExternalIdentity> identities = developerExternalIdentityRepository.findByDeveloper_DeveloperId(devOpt.get().getDeveloperId());
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (DeveloperExternalIdentity ext : identities) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("provider", ext.getProvider());
            map.put("subject", ext.getSubject());
            map.put("displayName", ext.getDisplayName());
            map.put("rawInfoJson", ext.getRawInfoJson());
            result.add(map);
        }
        return result;
    }

    @Operation(summary = "查询指定门户下所有已启用的OIDC登录方式", description = "返回provider、displayName、icon、enabled等信息")
    @PostMapping("/providers")
    public List<Map<String, Object>> listOidcProviders() {
        String portalId = contextHolder.getPortal();
        PortalResult portal = portalService.getPortal(portalId);
        PortalSettingConfig portalSetting = portal.getPortalSettingConfig();
        if (portalSetting == null) {
            log.error("[PortalSetting不存在] portalId={}", portalId);
            return new java.util.ArrayList<>();
        }
        List<PortalSettingConfig> settings = java.util.Arrays.asList(portalSetting);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (PortalSettingConfig setting : settings) {
            if (setting.getOidcConfigs() != null) {
                for (OidcConfig config : setting.getOidcConfigs()) {
                    if (config.isEnabled()) {
                        Map<String, Object> map = new java.util.HashMap<>();
                        map.put("provider", config.getProvider());
                        map.put("displayName", config.getName());
                        map.put("icon", config.getLogoUrl());
                        map.put("enabled", config.isEnabled());
                        result.add(map);
                    }
                }
            }
        }
        return result;
    }

    private RestTemplate createRestTemplateWithSSLBypass() {
        try {
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                new javax.net.ssl.X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            RestTemplate restTemplate = new RestTemplate();
            org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(30000);
            factory.setReadTimeout(30000);
            restTemplate.setRequestFactory(factory);

            return restTemplate;
        } catch (Exception e) {
            return new RestTemplate();
        }
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
            throw e;
        }
    }
    
    private String generateRedirectUri(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        String baseUrl = scheme + "://" + serverName;
        if (serverPort != 80 && serverPort != 443) {
            baseUrl += ":" + serverPort;
        }
        
        String requestURI = request.getRequestURI();
        
        String redirectUri;
        if (requestURI.contains("/api/v1/")) {
            redirectUri = baseUrl + "/api/v1/developers/callback";
        } else {
            String state = request.getParameter("state");
            if (state != null && state.contains("API_PREFIX=/api/v1")) {
                redirectUri = baseUrl + "/api/v1/developers/callback";
            } else {
                redirectUri = baseUrl + "/developers/callback";
            }
        }
        
        return redirectUri;
    }
} 