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
import com.alibaba.apiopenplatform.entity.Portal;
//import com.alibaba.apiopenplatform.entity.PortalSetting;
import com.alibaba.apiopenplatform.repository.PortalRepository;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;

import java.net.URLDecoder;
import com.alibaba.apiopenplatform.dto.params.developer.OidcTokenRequestParam;
import com.alibaba.apiopenplatform.dto.params.developer.OidcProvidersRequestParam;
import com.alibaba.apiopenplatform.dto.params.developer.ListIdentitiesRequestParam;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import javax.servlet.http.Cookie;
import javax.annotation.PostConstruct;


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
//    private final PortalRepository portalRepository;
    private final PortalService portalService;
    private final JwtService jwtService;
    private final ContextHolder contextHolder;
    
    private RestTemplate restTemplate;
    
    @PostConstruct
    public void init() {
        // 配置RestTemplate绕过SSL验证
        this.restTemplate = createRestTemplateWithSSLBypass();
    }
    
    private RestTemplate createRestTemplateWithSSLBypass() {
        try {
            // 创建信任所有证书的TrustManager
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

            // 创建SSLContext
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // 设置默认SSL配置
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            // 创建简单的RestTemplate
            RestTemplate restTemplate = new RestTemplate();
            
            // 设置超时
            org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
                    new org.springframework.http.client.SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10000);
            factory.setReadTimeout(10000);
            restTemplate.setRequestFactory(factory);

            return restTemplate;
        } catch (Exception e) {
            log.warn("无法配置SSL绕过，使用默认RestTemplate: {}", e.getMessage());
            return new RestTemplate();
        }
    }

    /**
     * OIDC授权入口，支持多配置
     * @param provider OIDC provider 名（如 github、google、aliyun、自定义）
     * @param state 前端生成的state参数
     * @note portalId 已自动根据域名识别，无需传递
     */
    @Operation(summary = "OIDC授权入口", description = "重定向到第三方登录页面。state参数格式：BINDING|{随机串}|{provider}|{token} 或 LOGIN|{provider}。portalId已自动根据域名识别，无需传递。注意：需要先配置对应的OIDC配置。")
    @GetMapping("/authorize")
    public void universalAuthorize(@RequestParam String provider, @RequestParam String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String portalId = contextHolder.getPortal();
        // 不再支持 frontendRedirectUrl 参数，统一从 PortalSetting 读取
        String newState = state;
        // 通过portalId查询对应的Portal，然后获取PortalSetting
        PortalResult portal = portalService.getPortal(portalId);
        PortalSettingConfig portalSetting = portal.getPortalSettingConfig();
        if (portalSetting == null) {
            log.error("[PortalSetting不存在] portalId={}", portalId);
            throw new BusinessException(ErrorCode.PORTAL_SETTING_NOT_FOUND);
        }
        java.util.List<PortalSettingConfig> settings = Collections.singletonList(portalSetting);
        OidcConfig config = null;
        for (PortalSettingConfig setting : settings) {
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
        
        // 如果state中包含API_PREFIX，使用它来构建回调URL
        String decodedState = URLDecoder.decode(newState, "UTF-8");
        log.info("[OIDCAuthorize] 解析state: {}", decodedState);
        if (decodedState.contains("API_PREFIX=")) {
            log.info("[OIDCAuthorize] 发现API_PREFIX参数");
            String[] parts = decodedState.split("\\|");
            for (String part : parts) {
                if (part.startsWith("API_PREFIX=")) {
                    String apiPrefix = part.substring("API_PREFIX=".length());
                    log.info("[OIDCAuthorize] 提取到API_PREFIX: {}", apiPrefix);
                    if (apiPrefix.startsWith("/")) {
                        // 构建完整的前端URL作为回调地址
                        String protocol = request.getScheme();
                        // 优先使用Host头，如果没有则使用ServerName
                        String serverName = request.getHeader("Host");
                        if (serverName == null || serverName.isEmpty()) {
                            serverName = request.getServerName();
                        }
                        // 如果Host头包含端口，去掉端口号
                        if (serverName.contains(":")) {
                            serverName = serverName.split(":")[0];
                        }
                        redirectUri = protocol + "://" + serverName + apiPrefix + "/developers/callback";
                        log.info("[OIDCAuthorize] 构建的回调URL: {}", redirectUri);
                    }
                    break;
                }
            }
        } else {
            log.info("[OIDCAuthorize] 未发现API_PREFIX参数");
        }
        
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
     * state 推荐格式：BINDING|{随机串}|{provider}|{token} 或 LOGIN|{provider}
     * <br>前端示例：
     *   const stateRaw = `BINDING|${Math.random().toString(36).slice(2)}|${provider}|${token}`;
     *   const state = encodeURIComponent(stateRaw);
     *   // 跳转参数 ...&state=${state}
     * 后端解析：
     *   String decodedState = URLDecoder.decode(state, "UTF-8");
     *   String[] arr = decodedState.split("\\|");
     *   String provider = arr[2]; // BINDING模式下
     *   String token = arr.length > 3 ? arr[3] : null;
     * @note portalId 已自动根据域名识别，无需传递
     */
    @Operation(summary = "OIDC统一回调", description = "处理第三方登录回调。state参数格式：BINDING|{随机串}|{provider}|{token} 或 LOGIN|{provider}。portalId已自动根据域名识别，无需传递。注意：此接口由第三方平台调用，不能直接测试。")
    @GetMapping("/callback")
    public void oidcCallback(@RequestParam String code, @RequestParam String state, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            log.info("[OIDCCallback] code={}, state={}", code, state);
            String portalId = contextHolder.getPortal();
            log.info("[OIDCCallback] portalId={}", portalId);
            String provider = null;
            String tokenParam = null;
            String mode = null;
            String apiPrefix = null; // 提升到方法级别
            String decodedState = URLDecoder.decode(state, "UTF-8");
            String[] stateParts = decodedState.split("\\|");
            if (decodedState.startsWith("BINDING|")) {
                String[] arr = decodedState.split("\\|");
                if (arr.length >= 4) {
                    provider = arr[2]; // 第三段为provider
                    tokenParam = arr[3]; // 第四段为token
                    mode = "BINDING";
                }
            } else if (decodedState.startsWith("LOGIN|")) {
                String[] arr = decodedState.split("\\|");
                if (arr.length >= 2) {
                    provider = arr[1]; // 第二段为provider
                    mode = "LOGIN";
                    
                    // 解析API_PREFIX参数
                    for (String part : arr) {
                        if (part.startsWith("API_PREFIX=")) {
                            apiPrefix = part.substring("API_PREFIX=".length());
                            log.info("[OIDCCallback] 解析到API_PREFIX: {}", apiPrefix);
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
            // 通过portalId查询对应的Portal，然后获取PortalSetting
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
            
            // 对于阿里云，使用sub字段作为providerSubject
            if (providerSubject == null || "null".equals(providerSubject)) {
                Object subValue = userInfoMap.get("sub");
                if (subValue != null) {
                    providerSubject = String.valueOf(subValue);
                }
            }
            
            // 对于GitHub，使用login字段作为displayName
            if (displayName == null || "null".equals(displayName)) {
                Object loginValue = userInfoMap.get("login");
                if (loginValue != null) {
                    displayName = String.valueOf(loginValue);
                }
            }
            
            // 获取email字段
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
                    // 将token设置到Cookie中
                    Cookie tokenCookie = new Cookie("auth_token", token);
                    tokenCookie.setPath("/");
                    tokenCookie.setHttpOnly(false); // 允许JavaScript访问
                    tokenCookie.setMaxAge(3600); // 1小时过期
                    response.addCookie(tokenCookie);
                    log.info("[OIDCCallback] 已设置auth_token Cookie");
                    
                    // 使用API_PREFIX构建重定向URL
                    String redirectUrl;
                    if (apiPrefix != null && apiPrefix.startsWith("/")) {
                        // 如果是相对路径，构建完整的前端URL
                        String protocol = request.getScheme();
                        String serverName = request.getHeader("Host");
                        if (serverName == null || serverName.isEmpty()) {
                            serverName = request.getServerName();
                        }
                        if (serverName.contains(":")) {
                            serverName = serverName.split(":")[0];
                        }
                        // 重定向到前端根路径，而不是API路径
                        redirectUrl = protocol + "://" + serverName + "/?login=success&fromCookie=true";
                        log.info("[OIDCCallback] 重定向到: {}", redirectUrl);
                    } else {
                        // 如果没有API_PREFIX，使用默认路径
                        redirectUrl = "/?login=success&fromCookie=true";
                    }
                    response.sendRedirect(redirectUrl);
                    return;
                } else {
                    log.error("[OIDCCallback] handleExternalLogin返回空结果");
                    response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("三方登录失败", "UTF-8"));
                    return;
                }
            }
        } catch (Exception e) {
            log.error("[OIDCCallback] 处理OAuth回调时发生异常", e);
            response.sendRedirect("/?login=fail&msg=" + java.net.URLEncoder.encode("处理回调时发生异常: " + e.getMessage(), "UTF-8"));
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

    // --- 通用三方用户信息获取 ---
    private Map<String, Object> fetchUserInfoMap(String code, OidcConfig config, HttpServletRequest request) {
        log.info("[fetchUserInfoMap] 开始获取用户信息，clientId={}, tokenEndpoint={}, userInfoEndpoint={}", 
                config.getClientId(), config.getTokenEndpoint(), config.getUserInfoEndpoint());
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        String redirectUri = generateRedirectUri(request);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");
        
        log.info("[fetchUserInfoMap] 请求参数: client_id={}, redirect_uri={}, code={}", 
                config.getClientId(), redirectUri, code);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "Portal-Management/1.0");
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        
        try {
            log.info("[fetchUserInfoMap] 发送token请求到: {}", config.getTokenEndpoint());
            ResponseEntity<Map> tokenResp = restTemplate.postForEntity(config.getTokenEndpoint(), entity, Map.class);
            log.info("[fetchUserInfoMap] token响应状态: {}", tokenResp.getStatusCode());
            log.info("[fetchUserInfoMap] token响应体: {}", tokenResp.getBody());
            
            String accessToken = (String) tokenResp.getBody().get("access_token");
            if (accessToken == null) {
                log.error("[fetchUserInfoMap] 未获取到access_token，响应体: {}", tokenResp.getBody());
                return null;
            }
            log.info("[fetchUserInfoMap] 获取到access_token，长度: {}", accessToken.length());
            
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(accessToken);
            userHeaders.set("User-Agent", "Portal-Management/1.0");
            HttpEntity<Void> userEntity = new HttpEntity<>(userHeaders);
            
            log.info("[fetchUserInfoMap] 发送用户信息请求到: {}", config.getUserInfoEndpoint());
            ResponseEntity<Map> userResp = restTemplate.exchange(config.getUserInfoEndpoint(), HttpMethod.GET, userEntity, Map.class);
            log.info("[fetchUserInfoMap] 用户信息响应状态: {}", userResp.getStatusCode());
            log.info("[fetchUserInfoMap] 用户信息响应体: {}", userResp.getBody());
            
            return userResp.getBody();
        } catch (Exception e) {
            log.error("[fetchUserInfoMap] 获取用户信息时发生异常", e);
            throw e;
        }
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
        
        // 检查请求路径是否包含API前缀
        String requestURI = request.getRequestURI();
        log.info("[generateRedirectUri] 请求URI: {}", requestURI);
        
        // 检查是否包含API前缀，或者检查state参数中的API_PREFIX
        String redirectUri;
        if (requestURI.contains("/api/v1/")) {
            redirectUri = baseUrl + "/api/v1/developers/callback";
            log.info("[generateRedirectUri] 从请求URI检测到API前缀，使用: {}", redirectUri);
        } else {
            // 检查请求参数中的state
            String state = request.getParameter("state");
            if (state != null && state.contains("API_PREFIX=/api/v1")) {
                redirectUri = baseUrl + "/api/v1/developers/callback";
                log.info("[generateRedirectUri] 从state参数检测到API前缀，使用: {}", redirectUri);
            } else {
                redirectUri = baseUrl + "/developers/callback";
                log.info("[generateRedirectUri] 未检测到API前缀，使用默认: {}", redirectUri);
            }
        }
        
        return redirectUri;
    }
} 