package com.alibaba.apiopenplatform.config;

import com.alibaba.apiopenplatform.auth.JwtService;
import com.alibaba.apiopenplatform.core.security.JwtAuthenticationFilter;
import com.alibaba.apiopenplatform.core.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import com.alibaba.apiopenplatform.core.security.DeveloperAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpMethod;

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
    @Value("${frontend.oauth2.success-url:http://localhost:3000/oauth2/success}")
    private String frontendSuccessUrl;

    @Autowired
    private DeveloperAuthenticationProvider developerAuthenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .and()
            .authorizeRequests()
//                .antMatchers("/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 管理员初始化、检测、登录接口（无需认证）
                .antMatchers("/admins/init", "/admins/need-init", "/admins/login").permitAll()
                // 开发者注册、登录接口（无需认证）
                .antMatchers("/developers", "/developers/login").permitAll()
                // OAuth2相关接口（无需认证）
                .antMatchers("/developers/authorize", "/developers/callback").permitAll()
                // 获取OIDC提供商列表（无需认证，登录前需要）
                .antMatchers("/developers/providers").permitAll()
                // 管理员接口（需要ADMIN角色）
                .antMatchers("/admins/**").hasRole("ADMIN")
                // 开发者接口（需要DEVELOPER角色）
                .antMatchers("/developers/profile", "/developers/password", "/developers/list-identities").hasRole("DEVELOPER")
                // 门户管理接口（需要ADMIN角色）
                .antMatchers("/portals/**").hasRole("ADMIN")
                // 产品管理接口（需要认证，但开发者和管理员都可以访问）
                .antMatchers("/products").authenticated()  // GET /products 需要认证，开发者和管理员都可以访问
                .antMatchers("/products/*").authenticated()  // GET /products/{productId} 需要认证，开发者和管理员都可以访问
                .antMatchers("/products/**").hasRole("ADMIN")  // 其他产品管理接口需要ADMIN角色
                // 消费者管理接口
                .antMatchers(HttpMethod.GET, "/consumers").authenticated()  // GET /consumers 需要认证，开发者和管理员都可以访问
                .antMatchers(HttpMethod.GET, "/consumers/*").authenticated()  // GET /consumers/{consumerId} 需要认证，开发者和管理员都可以访问
                .antMatchers(HttpMethod.POST, "/consumers").authenticated()  // POST /consumers 需要认证，开发者和管理员都可以访问
                .antMatchers(HttpMethod.DELETE, "/consumers/*").authenticated()  // DELETE /consumers/{consumerId} 需要认证，开发者和管理员都可以访问
                .antMatchers("/consumers/**").hasRole("ADMIN")  // 其他消费者管理接口需要ADMIN角色（审批等）
                // Nacos管理接口（需要ADMIN角色）
                .antMatchers("/nacos/**").hasRole("ADMIN")
                // 网关管理接口（需要ADMIN角色）
                .antMatchers("/gateways/**").hasRole("ADMIN")
                // MCP市场接口（需要ADMIN角色）
                .antMatchers("/api/mcpmarket/**").hasRole("ADMIN")
                // Swagger文档（开发环境可访问）
                .antMatchers("/portal/swagger-ui.html", "/portal/swagger-ui/**", "/portal/v3/api-docs/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
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

    // 移除 customOAuth2UserService、customOidcUserService、customAuthenticationSuccessHandler、customAuthenticationFailureHandler 等 Bean

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
                String state = (String) userRequest.getAdditionalParameters().get("state");
                String portalId = null;
                if (state != null) {
                    try {
                        String decodedState = java.net.URLDecoder.decode(state, "UTF-8");
                        String[] arr = decodedState.split("\\|");
                        if (arr.length >= 3) {
                            if (decodedState.startsWith("BINDING|")) {
                                portalId = arr[2];
                            } else if (decodedState.startsWith("LOGIN|")) {
                                portalId = arr[1];
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析state获取portalId失败: {}", e.getMessage());
                    }
                }
                if (isBinding) {
                    String currentUserId = authentication.getName();
                    Optional<com.alibaba.apiopenplatform.entity.Developer> devOpt = developerService.findByDeveloperId(currentUserId);
                    String developerId = devOpt.map(com.alibaba.apiopenplatform.entity.Developer::getDeveloperId).orElse(currentUserId);
                    developerService.bindExternalIdentity(developerId, providerName, providerSubject, displayName, rawInfoJson, portalId);
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
                    Optional<com.alibaba.apiopenplatform.dto.result.AuthResponseResult> authResult = developerService.handleExternalLogin(
                            providerName, providerSubject, email, displayName, rawInfoJson);
                    String myJwt = authResult.map(com.alibaba.apiopenplatform.dto.result.AuthResponseResult::getToken).orElse(null);
                    String developerId = authResult.map(com.alibaba.apiopenplatform.dto.result.AuthResponseResult::getUserId).orElse(null);
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Collections.singletonList("*"));
        corsConfig.setAllowedMethods(Collections.singletonList("*"));
        corsConfig.setAllowedHeaders(Collections.singletonList("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}