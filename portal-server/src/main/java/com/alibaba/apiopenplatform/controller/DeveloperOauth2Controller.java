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
import com.alibaba.apiopenplatform.repository.PortalSettingRepository;
import java.net.URLDecoder;
import com.alibaba.apiopenplatform.dto.params.developer.OidcTokenRequestParam;
import com.alibaba.apiopenplatform.dto.params.developer.OidcProvidersRequestParam;
import com.alibaba.apiopenplatform.dto.params.developer.ListIdentitiesRequestParam;
import com.alibaba.apiopenplatform.core.security.ContextHolder;

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
    private final PortalSettingRepository portalSettingRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtService jwtService;
    private final ContextHolder contextHolder;

    /**
     * OIDC授权入口，支持多配置
     * @param portalId 门户唯一标识
     * @param provider OIDC provider 名（如 github、google、aliyun、自定义）
     * @param state 前端生成的state参数
     */
    @Operation(summary = "OIDC授权入口", description = "前端需拼接state参数，格式为：BINDING|{随机串}|{portalId}|{provider}|{token} 或 LOGIN|{portalId}|{provider}。整体encodeURIComponent。")
    @GetMapping("/authorize")
    public void universalAuthorize(@RequestParam String portalId, @RequestParam String provider, @RequestParam String state, HttpServletResponse response) throws IOException {
        // 不再支持 frontendRedirectUrl 参数，统一从 PortalSetting 读取
        String newState = state;
        java.util.List<PortalSetting> settings = portalSettingRepository.findByPortal_PortalId(portalId);
        OidcConfig config = null;
        for (PortalSetting setting : settings) {
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
            throw new IllegalArgumentException("OIDC配置未启用");
        }
        String url = config.getAuthorizationEndpoint()
                + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + URLEncoder.encode(config.getRedirectUri(), "UTF-8")
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
     */
    @Operation(summary = "OIDC统一回调", description = "state 推荐格式：BINDING|{随机串}|{portalId}|{provider}|{token} 或 LOGIN|{portalId}|{provider}。整体encodeURIComponent。")
    @GetMapping("/callback")
    public void oidcCallback(@RequestParam String code, @RequestParam String state, HttpServletResponse response) throws IOException {
        log.info("[OIDCCallback] code={}, state={}", code, state);
        // 先 URL 解码
        String decodedState = URLDecoder.decode(state, "UTF-8");
        String portalId = null;
        String provider = null;
        String token = null;
        String mode = null;
        String frontendRedirectUrl = null;
        // 解析state，支持带frontendRedirectUrl
        String[] stateParts = decodedState.split("\\|");
        for (String part : stateParts) {
            if (part.startsWith("FRONTENDURL=")) {
                frontendRedirectUrl = java.net.URLDecoder.decode(part.substring("FRONTENDURL=".length()), "UTF-8");
            }
        }
        if (decodedState.startsWith("BINDING|")) {
            // BINDING|随机串|portalId|provider|token
            String[] arr = decodedState.split("\\|");
            if (arr.length >= 5) {
                final String parsedPortalId = arr[2];
                final String parsedProvider = arr[3];
                portalId = parsedPortalId;
                provider = parsedProvider;
                token = arr[4];
                mode = "BINDING";
            }
        } else if (decodedState.startsWith("LOGIN|")) {
            // LOGIN|portalId|provider
            String[] arr = decodedState.split("\\|");
            if (arr.length >= 3) {
                final String parsedPortalId = arr[1];
                final String parsedProvider = arr[2];
                portalId = parsedPortalId;
                provider = parsedProvider;
                mode = "LOGIN";
            }
        }
        if (portalId == null || provider == null) {
            response.sendRedirect("/?login=fail&msg=" + URLEncoder.encode("state参数错误，未包含portalId/provider", "UTF-8"));
            return;
        }
        // --- 只重定向到前端回调页，带 code 和 state，不带 token ---
        String redirectUrl = frontendRedirectUrl;
        java.util.List<PortalSetting> settings = portalSettingRepository.findByPortal_PortalId(portalId);
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            for (PortalSetting s : settings) {
                if (s.getFrontendRedirectUrl() != null && !s.getFrontendRedirectUrl().isEmpty()) {
                    redirectUrl = s.getFrontendRedirectUrl();
                    break;
                }
            }
        }
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            redirectUrl = "http://localhost:5173/callback";
        }
        if (!redirectUrl.contains("?")) {
            redirectUrl += "?code=" + URLEncoder.encode(code, "UTF-8") + "&state=" + URLEncoder.encode(state, "UTF-8");
        } else {
            redirectUrl += "&code=" + URLEncoder.encode(code, "UTF-8") + "&state=" + URLEncoder.encode(state, "UTF-8");
        }
        response.sendRedirect(redirectUrl);
    }

    /**
     * OIDC code 换取 token 标准接口
     */
    @Operation(summary = "OIDC code换token", description = "前端回调页用code和state换取JWT token，token只在响应体返回")
    @PostMapping("/token")
    public Map<String, Object> exchangeCodeForToken(@RequestBody OidcTokenRequestParam param) {
        // 解析state，获取portalId、provider等
        String decodedState;
        try {
            decodedState = java.net.URLDecoder.decode(param.getState(), "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("state参数解码失败: " + e.getMessage());
        }
        String portalId = null;
        String provider = null;
        String mode = null;
        String tokenParam = null;
        if (decodedState.startsWith("BINDING|")) {
            String[] arr = decodedState.split("\\|");
            if (arr.length >= 5) {
                portalId = arr[2];
                provider = arr[3];
                tokenParam = arr[4];
                mode = "BINDING";
            }
        } else if (decodedState.startsWith("LOGIN|")) {
            String[] arr = decodedState.split("\\|");
            if (arr.length >= 3) {
                portalId = arr[1];
                provider = arr[2];
                mode = "LOGIN";
            }
        }
        if (portalId == null || provider == null) {
            throw new RuntimeException("state参数错误，未包含portalId/provider");
        }
        java.util.List<PortalSetting> settings = portalSettingRepository.findByPortal_PortalId(portalId);
        OidcConfig config = null;
        for (PortalSetting setting : settings) {
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
            throw new RuntimeException("OIDC配置未启用");
        }
        // --- 获取三方用户信息 ---
        String providerSubject = null;
        String displayName = null;
        String rawInfoJson = null;
        Map<String, Object> userInfoMap;
        try {
            userInfoMap = fetchUserInfoMap(param.getCode(), config);
            Object idObj = userInfoMap.get("sub");
            if (idObj == null) idObj = userInfoMap.get("id");
            providerSubject = idObj != null ? String.valueOf(idObj) : null;
            Object nameObj = userInfoMap.get("name");
            if (nameObj == null) nameObj = userInfoMap.get("username");
            if (nameObj == null) nameObj = userInfoMap.get("login");
            displayName = nameObj != null ? String.valueOf(nameObj) : null;
            rawInfoJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(userInfoMap);
        } catch (Exception e) {
            throw new RuntimeException("获取三方用户信息失败: " + e.getMessage());
        }
        if ("BINDING".equals(mode)) {
            // 绑定流程
            Optional<DeveloperExternalIdentity> extOpt = developerExternalIdentityRepository.findByProviderAndSubject(provider, providerSubject);
            if (extOpt.isPresent()) {
                throw new RuntimeException("该外部账号已被其他用户绑定");
            }
            // 通过 token 识别当前用户
            String userId = null;
            if (tokenParam != null) {
                try {
                    Map<String, Object> claims = jwtService.parseAndValidateClaims(tokenParam);
                    userId = (String) claims.get("userId");
                } catch (Exception e) {
                    throw new RuntimeException("token无效或已过期");
                }
            }
            if (userId == null || userId.isEmpty()) {
                throw new RuntimeException("未登录，无法绑定");
            }
            Optional<Developer> devOpt = developerRepository.findByDeveloperId(userId);
            if (!devOpt.isPresent()) {
                throw new RuntimeException("用户不存在");
            }
            developerService.bindExternalIdentity(userId, provider, providerSubject, displayName, rawInfoJson, portalId);
            return Collections.singletonMap("result", "success");
        } else {
            // 登录流程
            Optional<AuthResponseResult> loginResult = developerService.handleExternalLogin(provider, providerSubject, null, displayName, rawInfoJson);
            if (loginResult.isPresent()) {
                return Collections.singletonMap("token", loginResult.get().getToken());
            } else {
                throw new RuntimeException("三方登录失败");
            }
        }
    }

    /**
     * 查询当前用户所有外部身份绑定（只返回provider、subject、displayName、rawInfoJson）
     */
    @Operation(summary = "查询当前用户所有外部身份绑定", description = "只返回provider、subject、displayName、rawInfoJson")
    @PostMapping("/list-identities")
    public List<Map<String, Object>> listIdentities(@RequestBody ListIdentitiesRequestParam param) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();
        Optional<Developer> devOpt = developerRepository.findByDeveloperId(userId);
        if (!devOpt.isPresent()) {
            throw new RuntimeException("UNAUTHORIZED");
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
     */
    @Operation(summary = "查询指定门户下所有已启用的OIDC登录方式", description = "返回 provider、displayName、icon、enabled 等信息，供前端动态渲染登录按钮")
    @PostMapping("/providers")
    public List<Map<String, Object>> listOidcProviders() {
        String portalId = contextHolder.getPortal();
        if (portalId == null || portalId.isEmpty()) {
            throw new RuntimeException("该域名未被绑定或未注册到管理后台，请联系管理员或检查域名配置。");
        }
        List<PortalSetting> settings = portalSettingRepository.findByPortal_PortalId(portalId);
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
    private Map<String, Object> fetchUserInfoMap(String code, OidcConfig config) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", config.getRedirectUri());
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
} 