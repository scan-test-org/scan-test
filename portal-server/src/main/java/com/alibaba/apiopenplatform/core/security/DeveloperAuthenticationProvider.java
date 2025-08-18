package com.alibaba.apiopenplatform.core.security;

import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import com.alibaba.apiopenplatform.service.DeveloperService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

/**
 * 开发者用户名密码认证提供者，实现 Spring Security 的 AuthenticationProvider
 * 负责开发者本地账号的认证逻辑
 *
 * @author zxd
 */
@Component
@RequiredArgsConstructor
public class DeveloperAuthenticationProvider implements AuthenticationProvider {

    private final DeveloperService developerService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        try {
            developerService.loginWithPassword(username, password);
            GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_DEVELOPER");
            return new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
        } catch (Exception e) {
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
} 