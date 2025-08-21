package com.alibaba.apiopenplatform.config;

import com.alibaba.apiopenplatform.core.security.JwtAuthenticationFilter;
import com.alibaba.apiopenplatform.core.utils.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
 
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
 

import java.util.*;

import com.alibaba.apiopenplatform.core.security.DeveloperAuthenticationProvider;
import org.springframework.http.HttpMethod;

/**
 * Spring Security安全配置，集成JWT认证与接口权限控制，支持管理员和开发者多用户体系
 *
 * @author zxd
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final DeveloperAuthenticationProvider developerAuthenticationProvider;

    // Auth相关
    private static final String[] AUTH_WHITELIST = {
            "/admins/init",
            "/admins/need-init",
            "/admins/login",
            "/developers",
            "/developers/login",
            "/developers/authorize",
            "/developers/callback",
            "/developers/providers"
    };

    // Swagger API文档相关
    private static final String[] SWAGGER_WHITELIST = {
            "/portal/swagger-ui.html",
            "/portal/swagger-ui/**",
            "/portal/v3/api-docs/**"
    };

    // 系统路径白名单
    private static final String[] SYSTEM_WHITELIST = {
            "/favicon.ico",
            "/error"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .authorizeRequests()
                // OPTIONS请求放行
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 认证相关接口放行
                .antMatchers(AUTH_WHITELIST).permitAll()
                // Swagger相关接口放行
                .antMatchers(SWAGGER_WHITELIST).permitAll()
                // 系统路径放行
                .antMatchers(SYSTEM_WHITELIST).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(developerAuthenticationProvider);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
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