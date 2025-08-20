package com.alibaba.apiopenplatform.core.security;

import cn.hutool.core.util.EnumUtil;
import com.alibaba.apiopenplatform.core.constant.Common;
import com.alibaba.apiopenplatform.support.enums.UserType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author zh
 */
@Component
public class ContextHolder {

    private final ThreadLocal<String> portalContext = new ThreadLocal<>();

    public String getPortal() {
        return portalContext.get();
    }

    public void savePortal(String portalId) {
        portalContext.set(portalId);
    }

    public void clearPortal() {
        portalContext.remove();
    }

    /**
     * 获取当前认证用户ID
     *
     * @return
     */
    public String getUser() {
        Authentication authentication = getAuthenticationFromContext();
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        throw new AuthenticationCredentialsNotFoundException("User ID not found in authentication");
    }

    /**
     * 获取当前认证用户类型
     *
     * @return 用户类型
     * @throws AuthenticationException 如果用户未认证或类型无效
     */
    private UserType getCurrentUserType() {
        Authentication authentication = getAuthenticationFromContext();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(Common.ROLE_PREFIX))
                .map(authority -> authority.substring(5))
                .map(role -> EnumUtil.likeValueOf(UserType.class, role))
                .findFirst()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("User type not found in authentication"));
    }

    public boolean isAdministrator() {
        try {
            return getCurrentUserType() == UserType.ADMIN;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    public boolean isDeveloper() {
        try {
            return getCurrentUserType() == UserType.DEVELOPER;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    /**
     * 获取当前认证信息
     *
     * @return
     */
    private Authentication getAuthenticationFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user found");
        }
        return authentication;
    }
}
