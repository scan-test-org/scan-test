package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.entity.Developer;
import com.alibaba.apiopenplatform.entity.DeveloperExternalIdentity;
import com.alibaba.apiopenplatform.repository.DeveloperExternalIdentityRepository;
import com.alibaba.apiopenplatform.repository.DeveloperRepository;
import com.alibaba.apiopenplatform.service.DeveloperService;
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
import com.alibaba.apiopenplatform.dto.result.AuthResponseDto;

/**
 * 开发者 OAuth2 统一回调与外部身份绑定控制器
 * 支持多渠道 OAuth2 登录、外部身份绑定与解绑
 *
 * @author zxd
 */
@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class DeveloperOauth2Controller {
    private final DeveloperRepository developerRepository;
    private final DeveloperExternalIdentityRepository developerExternalIdentityRepository;
    private final DeveloperService developerService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;
    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String githubClientSecret;
    @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
    private String githubRedirectUri;
    @Value("${spring.security.oauth2.client.provider.github.token-uri}")
    private String githubTokenUri;
    @Value("${spring.security.oauth2.client.provider.github.user-info-uri}")
    private String githubUserInfoUri;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;
    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String googleTokenUri;
    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${spring.security.oauth2.client.registration.aliyun.client-id}")
    private String aliyunClientId;
    @Value("${spring.security.oauth2.client.registration.aliyun.client-secret}")
    private String aliyunClientSecret;
    @Value("${spring.security.oauth2.client.registration.aliyun.redirect-uri}")
    private String aliyunRedirectUri;
    @Value("${spring.security.oauth2.client.provider.aliyun.token-uri}")
    private String aliyunTokenUri;
    @Value("${spring.security.oauth2.client.provider.aliyun.user-info-uri}")
    private String aliyunUserInfoUri;

    /**
     * OAuth2统一回调接口，支持登录和绑定分流
     * @param code 授权码
     * @param state 区分用途
     */
    @GetMapping("/callback")
    public void oauth2Callback(@RequestParam("code") String code,
                               @RequestParam("state") String state,
                               HttpServletRequest request,
                               HttpServletResponse response) throws IOException {
        log.info("[OAuth2Callback] code={}, state={}", code, state);
        if (state != null && (state.startsWith("BINDING_") || state.startsWith("BIND_"))) {
            // 绑定流程：只做绑定，不切换登录态
            String[] arr = state.replaceFirst("BINDING_", "").replaceFirst("BIND_", "").split("_");
            if (arr.length < 2) {
                response.sendRedirect("/bind-callback?result=fail&msg=" + URLEncoder.encode("state参数错误", "UTF-8"));
                return;
            }
            String userId = arr[0];
            String provider = arr[1];
            // --- 真实三方用户信息获取 ---
            String providerSubject = null;
            String displayName = null;
            String rawInfoJson = null;
            try {
                Map<String, Object> userInfoMap = null;
                if ("github".equals(provider)) {
                    userInfoMap = fetchGithubUserInfoMap(code);
                } else if ("google".equals(provider)) {
                    userInfoMap = fetchGoogleUserInfoMap(code);
                } else if ("aliyun".equals(provider) || "aliyun-oidc".equals(provider)) {
                    userInfoMap = fetchAliyunUserInfoMap(code);
                } else {
                    response.sendRedirect("/bind-callback?result=fail&msg=" + URLEncoder.encode("不支持的provider", "UTF-8"));
                    return;
                }
                Object idObj = userInfoMap.get("id");
                if (idObj == null) idObj = userInfoMap.get("sub"); // 兼容 Google OIDC
                providerSubject = idObj != null ? String.valueOf(idObj) : null;
                Object nameObj = userInfoMap.get("username");
                if (nameObj == null) nameObj = userInfoMap.get("name");
                if (nameObj == null) nameObj = userInfoMap.get("login");
                displayName = nameObj != null ? String.valueOf(nameObj) : null;
                rawInfoJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(userInfoMap);
            } catch (Exception e) {
                response.sendRedirect("/bind-callback?result=fail&msg=" + URLEncoder.encode("获取三方用户信息失败:" + e.getMessage(), "UTF-8"));
                return;
            }
            // 检查外部身份是否已被绑定
            Optional<DeveloperExternalIdentity> extOpt = developerExternalIdentityRepository.findByProviderAndSubject(provider, providerSubject);
            if (extOpt.isPresent()) {
                response.sendRedirect("/bind-callback?result=fail&msg=" + URLEncoder.encode("该外部账号已被其他用户绑定", "UTF-8"));
                return;
            }
            // 绑定到当前用户
            Optional<Developer> devOpt = developerRepository.findByDeveloperId(userId);
            if (!devOpt.isPresent()) {
                response.sendRedirect("/bind-callback?result=fail&msg=" + URLEncoder.encode("用户不存在", "UTF-8"));
                return;
            }
            developerService.bindExternalIdentity(userId, provider, providerSubject, displayName, rawInfoJson);
            response.sendRedirect("/bind-callback?result=success");
        } else if (state != null && state.startsWith("LOGIN_")) {
            // 登录流程，完成三方登录/注册，生成token，重定向到前端
            String[] arr = state.split("_");
            if (arr.length < 2) {
                response.sendRedirect("/?login=fail&msg=" + URLEncoder.encode("state参数错误", "UTF-8"));
                return;
            }
            String provider = arr[1];
            // --- 获取三方用户信息 ---
            String providerSubject = null;
            String displayName = null;
            String rawInfoJson = null;
            try {
                Map<String, Object> userInfoMap = null;
                if ("github".equals(provider)) {
                    userInfoMap = fetchGithubUserInfoMap(code);
                } else if ("google".equals(provider)) {
                    userInfoMap = fetchGoogleUserInfoMap(code);
                } else if ("aliyun".equals(provider) || "aliyun-oidc".equals(provider)) {
                    userInfoMap = fetchAliyunUserInfoMap(code);
                } else {
                    response.sendRedirect("/?login=fail&msg=" + URLEncoder.encode("不支持的provider", "UTF-8"));
                    return;
                }
                Object idObj = userInfoMap.get("id");
                if (idObj == null) idObj = userInfoMap.get("sub"); // 兼容 Google OIDC
                providerSubject = idObj != null ? String.valueOf(idObj) : null;
                Object nameObj = userInfoMap.get("username");
                if (nameObj == null) nameObj = userInfoMap.get("name");
                if (nameObj == null) nameObj = userInfoMap.get("login");
                displayName = nameObj != null ? String.valueOf(nameObj) : null;
                rawInfoJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(userInfoMap);
            } catch (Exception e) {
                response.sendRedirect("/?login=fail&msg=" + URLEncoder.encode("获取三方用户信息失败:" + e.getMessage(), "UTF-8"));
                return;
            }
            // 统一用 handleExternalLogin 处理三方登录/注册
            Optional<AuthResponseDto> loginResult = developerService.handleExternalLogin(provider, providerSubject, null, displayName, rawInfoJson);
            if (loginResult.isPresent()) {
                String token = loginResult.get().getToken();
                response.sendRedirect("http://localhost:5173/?token=" + token);
            } else {
                response.sendRedirect("/?login=fail&msg=" + URLEncoder.encode("三方登录失败", "UTF-8"));
            }
        } else {
            // 兜底跳首页
            response.sendRedirect("/");
        }
    }

    // --- 三方用户信息结构体 ---
    private static class ThirdPartyUserInfo {
        public String id;
        public String email;
        public String username;
        public ThirdPartyUserInfo(String id, String email, String username) {
            this.id = id;
            this.email = email;
            this.username = username;
        }
    }

    // --- GitHub ---
    private Map<String, Object> fetchGithubUserInfoMap(String code) {
        // 1. 用 code 换 access_token
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", githubClientId);
        params.add("client_secret", githubClientSecret);
        params.add("code", code);
        params.add("redirect_uri", githubRedirectUri);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<Map> tokenResp = restTemplate.postForEntity(githubTokenUri, entity, Map.class);
        String accessToken = (String) tokenResp.getBody().get("access_token");
        // 2. 用 access_token 获取用户信息
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userEntity = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResp = restTemplate.exchange(githubUserInfoUri, HttpMethod.GET, userEntity, Map.class);
        Map userMap = userResp.getBody();
        return userMap;
    }

    // --- Google ---
    private Map<String, Object> fetchGoogleUserInfoMap(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("code", code);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<Map> tokenResp = restTemplate.postForEntity(googleTokenUri, entity, Map.class);
        String accessToken = (String) tokenResp.getBody().get("access_token");
        // 获取用户信息
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userEntity = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResp = restTemplate.exchange(googleUserInfoUri, HttpMethod.GET, userEntity, Map.class);
        Map userMap = userResp.getBody();
        return userMap;
    }

    // --- Aliyun ---
    private Map<String, Object> fetchAliyunUserInfoMap(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", aliyunClientId);
        params.add("client_secret", aliyunClientSecret);
        params.add("redirect_uri", aliyunRedirectUri);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        ResponseEntity<Map> tokenResp = restTemplate.postForEntity(aliyunTokenUri, entity, Map.class);
        String accessToken = (String) tokenResp.getBody().get("access_token");
        // 获取用户信息
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userEntity = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResp = restTemplate.exchange(aliyunUserInfoUri, HttpMethod.GET, userEntity, Map.class);
        Map userMap = userResp.getBody();
        return userMap;
    }

    /**
     * 解绑外部身份接口
     * @param body 需要解绑的 provider 和 subject
     */
    @PostMapping("/unbind-identity")
    public ResponseEntity<?> unbindIdentity(@RequestBody Map<String, String> body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();
        Optional<Developer> devOpt = developerRepository.findByDeveloperId(userId);
        if (!devOpt.isPresent()) {
            Map<String, String> resp = new java.util.HashMap<>();
            resp.put("code", "UNAUTHORIZED");
            resp.put("message", "用户未登录");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
        Developer developer = devOpt.get();
        // 查询当前用户所有绑定
        List<DeveloperExternalIdentity> identities = developerExternalIdentityRepository.findByDeveloper_DeveloperId(developer.getDeveloperId());
        if (identities.size() <= 1) {
            Map<String, String> resp = new java.util.HashMap<>();
            resp.put("code", "AT_LEAST_ONE");
            resp.put("message", "至少保留一个绑定账号，不能全部解绑");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
        String provider = body.get("provider");
        String subject = body.get("subject");
        Optional<DeveloperExternalIdentity> extOpt = developerExternalIdentityRepository.findByProviderAndSubject(provider, subject);
        if (!extOpt.isPresent() || !extOpt.get().getDeveloper().getDeveloperId().equals(developer.getDeveloperId())) {
            Map<String, String> resp = new java.util.HashMap<>();
            resp.put("code", "NOT_FOUND");
            resp.put("message", "未找到该绑定关系");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resp);
        }
        developerExternalIdentityRepository.delete(extOpt.get());
        Map<String, String> resp = new java.util.HashMap<>();
        resp.put("code", "SUCCESS");
        resp.put("message", "解绑成功");
        return ResponseEntity.ok(resp);
    }

    /**
     * 查询当前用户所有外部身份绑定（只返回provider、subject、displayName、rawInfoJson）
     */
    @GetMapping("/list-identities")
    public ResponseEntity<?> listIdentities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal();
        Optional<Developer> devOpt = developerRepository.findByDeveloperId(userId);
        if (!devOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
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
        return ResponseEntity.ok(result);
    }

    /**
     * 统一三方/oidc授权入口，支持github、google、aliyun、oidc等，便于扩展
     */
    @GetMapping("/api/oauth/authorize")
    public void universalAuthorize(@RequestParam("provider") String provider,
                                   @RequestParam("state") String state,
                                   HttpServletResponse response) throws IOException {
        String clientId, redirectUri, scope, authUrl;
        switch (provider) {
            case "github":
                clientId = githubClientId;
                redirectUri = URLEncoder.encode(githubRedirectUri, "UTF-8");
                scope = "user:email";
                authUrl = "https://github.com/login/oauth/authorize";
                break;
            case "google":
                clientId = googleClientId;
                redirectUri = URLEncoder.encode(googleRedirectUri, "UTF-8");
                scope = "openid profile email";
                authUrl = "https://accounts.google.com/o/oauth2/v2/auth";
                break;
            case "aliyun":
                clientId = aliyunClientId;
                redirectUri = URLEncoder.encode(aliyunRedirectUri, "UTF-8");
                scope = "openid profile";
                authUrl = "https://signin.aliyun.com/oauth2/v1/auth";
                break;
            case "oidc":
                // 假设通用OIDC参数由配置或前端传递，示例写死
                clientId = "your-oidc-client-id";
                redirectUri = URLEncoder.encode("http://localhost:8080/oauth2/callback", "UTF-8");
                scope = "openid profile email";
                authUrl = "https://your-oidc-provider.com/authorize";
                break;
            default:
                throw new IllegalArgumentException("不支持的provider: " + provider);
        }
        String url = authUrl + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=" + URLEncoder.encode(scope, "UTF-8")
                + "&response_type=code"
                + "&state=" + URLEncoder.encode(state, "UTF-8");
        response.sendRedirect(url);
    }
} 