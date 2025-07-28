package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.entity.Developer;
import com.alibaba.apiopenplatform.entity.DeveloperExternalIdentity;
import com.alibaba.apiopenplatform.repository.DeveloperExternalIdentityRepository;
import com.alibaba.apiopenplatform.repository.DeveloperRepository;
import com.alibaba.apiopenplatform.service.DeveloperService;
import com.alibaba.apiopenplatform.support.portal.OidcConfig;
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
import com.alibaba.apiopenplatform.entity.Portal;
import com.alibaba.apiopenplatform.entity.PortalSetting;
import com.alibaba.apiopenplatform.repository.PortalRepository;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;

import java.net.URLDecoder;
import com.alibaba.apiopenplatform.dto.params.developer.OidcTokenRequestParam;
import com.alibaba.apiopenplatform.dto.params.developer.OidcProvidersRequestParam;
import com.alibaba.apiopenplatform.dto.params.developer.ListIdentitiesRequestParam;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import javax.servlet.http.Cookie;


/**
 * 开发者 OAuth2 统一回调与外部身份绑定控制器
 * 支持多渠道 OAuth2 登录、外部身份绑定与解绑
 *
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
    private final PortalRepository portalRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtService jwtService;
    private final ContextHolder contextHolder;

    /**
     * OIDC授权入口，支持多配置
     * @param provider OIDC provider 名（如 github、google、aliyun、自定义）
     * @param state 前端生成的state参数
     * @note portalId 已自动根据域名识别，无需传递
     */
    @Operation(summary = "OIDC授权入口", description = "重定向到第三方登录页面。state参数格式：BINDING|{随机串}|{portalId}|{provider}|{token} 或 LOGIN|{portalId}|{provider}。注意：需要先配置对应的OIDC配置。")
    @GetMapping("/authorize")
    public void universalAuthorize(@RequestParam String provider, @RequestParam String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String portalId = contextHolder.getPortal();
        // 不再支持 frontendRedirectUrl 参数，统一从 PortalSetting 读取
        String newState = state;
        // 通过portalId查询对应的Portal，然后获取PortalSetting
        Optional<Portal> portalOpt = portalRepository.findByPortalId(portalId);
        if (!portalOpt.isPresent()) {
            log.error("[Portal不存在] portalId={}", portalId);
            throw new BusinessException(ErrorCode.PORTAL_NOT_FOUND, portalId);
        }
        Portal portal = portalOpt.get();
        PortalSetting portalSetting = portal.getPortalSetting();
        if (portalSetting == null) {
            log.error("[PortalSetting不存在] portalId={}", portalId);
            throw new BusinessException(ErrorCode.PORTAL_SETTING_NOT_FOUND);
        }
        java.util.List<PortalSetting> settings = java.util.Arrays.asList(portalSetting);
        OidcConfig config = null;
        for (PortalSetting setting : settings) {
            if (setting.getOidcConfigs() != null) {
                for (OidcConfig c : setting.getOidcConfigs()) {
                    log.info("[OIDC配置检查] provider={}, enabled={}, name={}, id={}", c.getProvider(), c.isEnabled(), c.getName(), c.getId());
                    if (provider.equals(c.getProvider())) {
                        config = c;
                        log.info("[OIDC配置匹配] 命中 provider={}, enabled={}", c.getProvider(), c.isEnabled());
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
        // 动态生成redirectUri，基于当前请求的域名
        String redirectUri = generateRedirectUri(request);
        
        String url = config.getAuthorizationEndpoint()
                + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8")
                + "&scope=" + URLEncoder.encode(config.getScopes(), "UTF-8")
                + "&response_type=code"
                + "&state=" + URLEncoder.encode(newState, "UTF-8");
        response.sendRedirect(url);
    }

    /**
     * OIDC统一回调接口，支持登录和绑定分流
     * state 推荐格式：BINDING|{随机串}|{portalId}|{provider}|{token} 或 LOGIN|{portalId}|{provider}
     * <br>前端示例：
     *   const stateRaw = `BINDING|${Math.random().toString(36).slice(2)}|${portalId}|${provider}|${token}`;
     *   const state = encodeURIComponent(stateRaw);
     *   // 跳转参数 ...&state=${state}
     * 后端解析：
     *   String decodedState = URLDecoder.decode(state, "UTF-8");
     *   String[] arr = decodedState.split("\\|");
     *   String portalId = arr[2];
     *   String provider = arr[3];
     *   String token = arr.length > 4 ? arr[4] : null;
     * @note portalId 已自动根据域名识别，无需传递
     */
    @Operation(summary = "OIDC统一回调", description = "处理第三方登录回调。state参数格式：BINDING|{随机串}|{portalId}|{provider}|{token} 或 LOGIN|{portalId}|{provider}。注意：此接口由第三方平台调用，不能直接测试。")
    @GetMapping("/callback")
    public void oidcCallback(@RequestParam String code, @RequestParam String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("[OIDCCallback] code={}, state={}", code, state);
        String portalId = contextHolder.getPortal();
        String provider = null;
        String tokenParam = null;
        String mode = null;
        String frontendRedirectUrl = null;
        String decodedState = URLDecoder.decode(state, "UTF-8");
        String[] stateParts = decodedState.split("\\|");
        if (decodedState.startsWith("BINDING|")) {
            String[] arr = decodedState.split("\\|");
            if (arr.length >= 5) {
                provider = arr[3];
                tokenParam = arr[4];
                mode = "BINDING";
            }
        } else if (decodedState.startsWith("LOGIN|")) {
            String[] arr = decodedState.split("\\|");
            if (arr.length >= 2) {
                provider = arr[1]; // 始终取第二段为 provider
                mode = "LOGIN";
            }
        }
        if (portalId == null || provider == null) {
            response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("未包含portalId/provider", "UTF-8"));
            return;
        }
        // 通过portalId查询对应的Portal，然后获取PortalSetting
        Optional<Portal> portalOpt = portalRepository.findByPortalId(portalId);
        if (!portalOpt.isPresent()) {
            log.error("[Portal不存在] portalId={}", portalId);
            response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("Portal不存在", "UTF-8"));
            return;
        }
        Portal portal = portalOpt.get();
        PortalSetting portalSetting = portal.getPortalSetting();
        if (portalSetting == null) {
            log.error("[PortalSetting不存在] portalId={}", portalId);
            response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("PortalSetting不存在", "UTF-8"));
            return;
        }
        
        // 从数据库配置中获取frontendRedirectUrl
        frontendRedirectUrl = portalSetting.getFrontendRedirectUrl();
        if (frontendRedirectUrl == null || frontendRedirectUrl.isEmpty()) {
            // 如果数据库没有配置，使用默认值
            frontendRedirectUrl = "/";
        }
        java.util.List<PortalSetting> settings = java.util.Arrays.asList(portalSetting);
        OidcConfig config = null;
        for (PortalSetting setting : settings) {
            if (setting.getOidcConfigs() != null) {
                for (OidcConfig c : setting.getOidcConfigs()) {
                    log.info("[OIDC配置检查] provider={}, enabled={}, name={}, id={}", c.getProvider(), c.isEnabled(), c.getName(), c.getId());
                    if (provider.equals(c.getProvider())) {
                        config = c;
                        log.info("[OIDC配置匹配] 命中 provider={}, enabled={}", c.getProvider(), c.isEnabled());
                        break;
                    }
                }
            }
            if (config != null) break;
        }
        if (config == null || !config.isEnabled()) {
            log.error("[OIDC配置未启用] provider={}, configs={}", provider, settings);
            response.sendRedirect(frontendRedirectUrl + "?login=fail&msg=" + java.net.URLEncoder.encode("OIDC配置未启用", "UTF-8"));
            return;
        }
        // --- 获取三方用户信息 ---
        String providerSubject = null;
        String displayName = null;
        String rawInfoJson = null;
        Map<String, Object> userInfoMap;
        try {
            userInfoMap = fetchUserInfoMap(code, config, request);
            Object idObj = userInfoMap.get("sub");
            if (idObj == null) idObj = userInfoMap.get("id");
            providerSubject = idObj != null ? String.valueOf(idObj) : null;
            Object nameObj = userInfoMap.get("name");
            if (nameObj == null) nameObj = userInfoMap.get("username");
            if (nameObj == null) nameObj = userInfoMap.get("login");
            displayName = nameObj != null ? String.valueOf(nameObj) : null;
            rawInfoJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(userInfoMap);
        } catch (Exception e) {
            response.sendRedirect(frontendRedirectUrl + "?login=fail&msg=" + java.net.URLEncoder.encode("获取三方用户信息失败", "UTF-8"));
            return;
        }
        if ("BINDING".equals(mode)) {
            Optional<DeveloperExternalIdentity> extOpt = developerExternalIdentityRepository.findByProviderAndSubject(provider, providerSubject);
            if (extOpt.isPresent()) {
                response.sendRedirect(frontendRedirectUrl + "?login=fail&msg=" + java.net.URLEncoder.encode("该外部账号已被其他用户绑定", "UTF-8"));
                return;
            }
            String userId = null;
            if (tokenParam != null) {
                try {
                    Map<String, Object> claims = jwtService.parseAndValidateClaims(tokenParam);
                    userId = (String) claims.get("userId");
                } catch (Exception e) {
                    response.sendRedirect(frontendRedirectUrl + "?login=fail&msg=" + java.net.URLEncoder.encode("token无效或已过期", "UTF-8"));
                    return;
                }
            }
            if (userId == null || userId.isEmpty()) {
                response.sendRedirect(frontendRedirectUrl + "?login=fail&msg=" + java.net.URLEncoder.encode("未登录，无法绑定", "UTF-8"));
                return;
            }
            Optional<Developer> devOpt = developerRepository.findByDeveloperId(userId);
            if (!devOpt.isPresent()) {
                response.sendRedirect(frontendRedirectUrl + "?login=fail&msg=" + java.net.URLEncoder.encode("用户不存在", "UTF-8"));
                return;
            }
            developerService.bindExternalIdentity(userId, provider, providerSubject, displayName, rawInfoJson, portalId);
            response.sendRedirect("/settings/account?bind=success");
            return;
        } else {
            Optional<AuthResponseResult> loginResult = developerService.handleExternalLogin(provider, providerSubject, null, displayName, rawInfoJson);
            if (loginResult.isPresent()) {
                String token = loginResult.get().getToken();
                // 设置到 cookie（非 HttpOnly）
                Cookie cookie = new Cookie("token", token);
                cookie.setPath("/");
                cookie.setMaxAge(3600); // 1小时
                response.addCookie(cookie);
                // 跳转到前端首页或指定页面
                if (frontendRedirectUrl != null) {
                    response.sendRedirect(frontendRedirectUrl);
                } else {
                    response.sendRedirect("/");
                }
                return;
            } else {
                response.sendRedirect(frontendRedirectUrl + "?login=fail&msg=" + java.net.URLEncoder.encode("三方登录失败", "UTF-8"));
                return;
            }
        }
    }

    /**
     * 查询当前用户所有外部身份绑定（只返回provider、subject、displayName、rawInfoJson）
     */
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

    /**
     * 查询指定门户下所有已启用的 OIDC provider
     * @note portalId 已自动根据域名识别，无需传递
     */
    @Operation(summary = "查询指定门户下所有已启用的OIDC登录方式", description = "返回 provider、displayName、icon、enabled 等信息，供前端动态渲染登录按钮。portalId已自动根据域名识别，无需传递。")
    @PostMapping("/providers")
    public List<Map<String, Object>> listOidcProviders() {
        String portalId = contextHolder.getPortal();
        // 通过portalId查询对应的Portal，然后获取PortalSetting
        Optional<Portal> portalOpt = portalRepository.findByPortalId(portalId);
        if (!portalOpt.isPresent()) {
            log.error("[Portal不存在] portalId={}", portalId);
            return new java.util.ArrayList<>();
        }
        Portal portal = portalOpt.get();
        PortalSetting portalSetting = portal.getPortalSetting();
        if (portalSetting == null) {
            log.error("[PortalSetting不存在] portalId={}", portalId);
            return new java.util.ArrayList<>();
        }
        List<PortalSetting> settings = java.util.Arrays.asList(portalSetting);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (PortalSetting setting : settings) {
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

    // --- 通用三方用户信息获取 ---
    private Map<String, Object> fetchUserInfoMap(String code, OidcConfig config, HttpServletRequest request) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", generateRedirectUri(request));
        params.add("grant_type", "authorization_code");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<Map> tokenResp = restTemplate.postForEntity(config.getTokenEndpoint(), entity, Map.class);
        String accessToken = (String) tokenResp.getBody().get("access_token");
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userEntity = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResp = restTemplate.exchange(config.getUserInfoEndpoint(), HttpMethod.GET, userEntity, Map.class);
        return userResp.getBody();
    }
    
    /**
     * 动态生成redirectUri，基于当前请求的域名
     */
    private String generateRedirectUri(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        // 构建基础URL
        String baseUrl = scheme + "://" + serverName;
        if (serverPort != 80 && serverPort != 443) {
            baseUrl += ":" + serverPort;
        }
        
        // 添加回调路径
        return baseUrl + "/developers/callback";
    }
} 