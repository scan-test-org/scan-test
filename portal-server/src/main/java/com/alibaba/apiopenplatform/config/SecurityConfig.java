package com.alibaba.apiopenplatform.config;

import com.alibaba.apiopenplatform.auth.JwtService;
import com.alibaba.apiopenplatform.core.security.JwtAuthenticationFilter;
import com.alibaba.apiopenplatform.core.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import cn.hutool.jwt.JWT;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import javax.servlet.http.HttpServletRequest;
import com.alibaba.apiopenplatform.core.security.DeveloperAuthenticationProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import java.util.Collection;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Spring Security安全配置，集成JWT认证与接口权限控制，支持管理员和开发者多用户体系
 *
 * @author zxd
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private static final org.slf4j.Logger OIDC_LOG = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final com.alibaba.apiopenplatform.service.DeveloperService developerService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final DeveloperAuthenticationProvider developerAuthenticationProvider;
    @Value("${frontend.oauth2.success-url:http://localhost:3000/oauth2/success}")
    private String frontendSuccessUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .and()
            .authorizeRequests()
                // 管理员初始化、检测、登录、改密接口全部放行
                .antMatchers("/api/admin/init", "/api/admin/need-init", "/api/admin/login", "/api/admin/change-password").permitAll()
                .antMatchers("/api/developer/register", "/api/developer/login").permitAll()
                .antMatchers("/api/oauth/authorize").permitAll()
                .antMatchers("/login/oauth2/**", "/oauth2/**").permitAll()
                .antMatchers("/bind-callback").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/api/developer/**").hasRole("DEVELOPER")
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .oauth2Login()
                .userInfoEndpoint()
                    .oidcUserService(customOidcUserService())
                    .userService(customOAuth2UserService())
                .and()
                .successHandler(customAuthenticationSuccessHandler())
                .failureHandler(customAuthenticationFailureHandler())
            .and()
            .authenticationProvider(developerAuthenticationProvider);
        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, tokenBlacklistService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService() {
        return userRequest -> {
            DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
            OAuth2User oauth2User = delegate.loadUser(userRequest);

            String providerName = userRequest.getClientRegistration().getRegistrationId();
            String providerSubject = oauth2User.getName();
            String email = oauth2User.getAttribute("email");
            String displayName = oauth2User.getAttribute("login");
            if (displayName == null) displayName = oauth2User.getAttribute("name");
            if (displayName == null) displayName = oauth2User.getAttribute("nickname");
            String rawInfoJson;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                rawInfoJson = objectMapper.writeValueAsString(oauth2User.getAttributes());
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.warn("序列化oauth2User attributes为JSON失败", e);
                rawInfoJson = "{}";
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isBinding = false;
            // 新增：获取当前请求token
            String token = null;
            try {
                org.springframework.web.context.request.ServletRequestAttributes attrs = (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    javax.servlet.http.HttpServletRequest req = attrs.getRequest();
                    String authHeader = req.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                    }
                }
            } catch (Exception e) {
                log.warn("获取token失败", e);
            }
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String name = authentication.getName();
                if (name != null && name.startsWith("dev-")) {
                    // 新增：token有效性校验
                    boolean tokenValid = false;
                    if (token != null) {
                        try {
                            jwtService.parseAndValidateClaims(token);
                            tokenValid = true;
                        } catch (Exception e) {
                            log.info("token无效或过期: {}", e.getMessage());
                        }
                    }
                    if (tokenValid && !tokenBlacklistService.isBlacklisted(token)) {
                        isBinding = true;
                    }
                }
            }
            if (isBinding) {
                // 绑定流程，查出当前登录用户的本地developerId
                String currentUserId = authentication.getName();
                Optional<com.alibaba.apiopenplatform.entity.Developer> devOpt = developerService.findByDeveloperId(currentUserId);
                String developerId = devOpt.map(com.alibaba.apiopenplatform.entity.Developer::getDeveloperId).orElse(currentUserId);
                developerService.bindExternalIdentity(developerId, providerName, providerSubject, displayName, rawInfoJson);
                // 刷新SecurityContext，确保getName()返回developerId
                OAuth2User newPrincipal = new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_DEVELOPER")),
                    oauth2User.getAttributes(),
                    "login"
                ) {
                    @Override
                    public String getName() {
                        return developerId;
                    }
                };
                Authentication newAuth = new OAuth2AuthenticationToken(newPrincipal, newPrincipal.getAuthorities(), providerName);
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                return newPrincipal;
            } else {
                Optional<com.alibaba.apiopenplatform.dto.result.AuthResponseDto> authResult = developerService.handleExternalLogin(
                        providerName, providerSubject, email, displayName, rawInfoJson);
                String myJwt = authResult.map(com.alibaba.apiopenplatform.dto.result.AuthResponseDto::getToken).orElse(null);
                String developerId = authResult.map(com.alibaba.apiopenplatform.dto.result.AuthResponseDto::getUserId).orElse(null);
                Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
                attributes.put("token", myJwt);
                Set<GrantedAuthority> authorities = Collections.singleton(
                    new SimpleGrantedAuthority("ROLE_DEVELOPER")
                );
                String nameAttributeKey = userRequest.getClientRegistration()
                    .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
                OAuth2User newPrincipal = new DefaultOAuth2User(authorities, attributes, nameAttributeKey) {
                    @Override
                    public String getName() {
                        return developerId;
                    }
                };
                Authentication newAuth = new OAuth2AuthenticationToken(newPrincipal, newPrincipal.getAuthorities(), providerName);
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                return newPrincipal;
            }
        };
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String token = (String) oauth2User.getAttribute("token");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"SUCCESS\",\"token\":\"" + token + "\"}");
        };
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            // 无前端时直接返回 JSON 错误
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"OAUTH2_FAIL\",\"message\":\"OAuth2认证失败: " + exception.getMessage() + "\"}");
        };
    }

    // 自定义OidcUser，合并自定义attributes（含token）
    public static class CustomOidcUser extends DefaultOidcUser {
        private final Map<String, Object> customAttributes;
        public CustomOidcUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken, OidcUserInfo userInfo, String nameAttributeKey, Map<String, Object> customAttributes) {
            super(authorities, idToken, userInfo, nameAttributeKey);
            this.customAttributes = customAttributes;
        }
        @Override
        public Map<String, Object> getAttributes() {
            return customAttributes != null ? customAttributes : super.getAttributes();
        }
    }

    @Bean
    public OidcUserService customOidcUserService() {
        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                OIDC_LOG.info("【customOidcUserService】被调用，registrationId={}, state={}",
                        userRequest.getClientRegistration().getRegistrationId(),
                        userRequest.getAdditionalParameters().get("state")
                );
                OidcUser oidcUser = super.loadUser(userRequest);
                OIDC_LOG.info("oidcUser attributes: {}", oidcUser.getAttributes());
                String providerName = userRequest.getClientRegistration().getRegistrationId();
                String providerSubject = oidcUser.getName();
                String email = oidcUser.getAttribute("email");
                String displayName = oidcUser.getAttribute("login");
                if (displayName == null) displayName = oidcUser.getAttribute("name");
                if (displayName == null) displayName = oidcUser.getAttribute("nickname");
                String rawInfoJson;
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    rawInfoJson = objectMapper.writeValueAsString(oidcUser.getAttributes());
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    OIDC_LOG.warn("序列化oidcUser attributes为JSON失败", e);
                    rawInfoJson = "{}";
                }
                String nameAttributeKey = "sub";
                Map<String, Object> attributes = new HashMap<>(oidcUser.getAttributes());
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isBinding = false;
                // 新增：获取当前请求token
                String token = null;
                try {
                    org.springframework.web.context.request.ServletRequestAttributes attrs = (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
                    if (attrs != null) {
                        javax.servlet.http.HttpServletRequest req = attrs.getRequest();
                        String authHeader = req.getHeader("Authorization");
                        if (authHeader != null && authHeader.startsWith("Bearer ")) {
                            token = authHeader.substring(7);
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取token失败", e);
                }
                if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                    String name = authentication.getName();
                    if (name != null && name.startsWith("dev-")) {
                        // 新增：token有效性校验
                        boolean tokenValid = false;
                        if (token != null) {
                            try {
                                jwtService.parseAndValidateClaims(token);
                                tokenValid = true;
                            } catch (Exception e) {
                                log.info("token无效或过期: {}", e.getMessage());
                            }
                        }
                        if (tokenValid && !tokenBlacklistService.isBlacklisted(token)) {
                            isBinding = true;
                        }
                    }
                }
                if (isBinding) {
                    String currentUserId = authentication.getName();
                    Optional<com.alibaba.apiopenplatform.entity.Developer> devOpt = developerService.findByDeveloperId(currentUserId);
                    String developerId = devOpt.map(com.alibaba.apiopenplatform.entity.Developer::getDeveloperId).orElse(currentUserId);
                    developerService.bindExternalIdentity(developerId, providerName, providerSubject, displayName, rawInfoJson);
                    // 刷新SecurityContext，确保getName()返回developerId
                    OidcUser newPrincipal = new CustomOidcUser(
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_DEVELOPER")),
                        oidcUser.getIdToken(),
                        oidcUser.getUserInfo(),
                        nameAttributeKey,
                        attributes
                    ) {
                        @Override
                        public String getName() {
                            return developerId;
                        }
                    };
                    Authentication newAuth = new OAuth2AuthenticationToken(newPrincipal, newPrincipal.getAuthorities(), providerName);
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                    return newPrincipal;
                } else {
                    Optional<com.alibaba.apiopenplatform.dto.result.AuthResponseDto> authResult = developerService.handleExternalLogin(
                            providerName, providerSubject, email, displayName, rawInfoJson);
                    String myJwt = authResult.map(com.alibaba.apiopenplatform.dto.result.AuthResponseDto::getToken).orElse(null);
                    String developerId = authResult.map(com.alibaba.apiopenplatform.dto.result.AuthResponseDto::getUserId).orElse(null);
                    attributes.put("token", myJwt);
                    OidcUser newPrincipal = new CustomOidcUser(
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_DEVELOPER")),
                        oidcUser.getIdToken(),
                        oidcUser.getUserInfo(),
                        nameAttributeKey,
                        attributes
                    ) {
                        @Override
                        public String getName() {
                            return developerId;
                        }
                    };
                    Authentication newAuth = new OAuth2AuthenticationToken(newPrincipal, newPrincipal.getAuthorities(), providerName);
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                    return newPrincipal;
                }
            }
        };
    }
} 