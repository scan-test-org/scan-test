package com.alibaba.apiopenplatform.core.security;

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
     * 用户类型枚举
     */
    public enum UserType {
        /**
         * 管理员用户
         */
        ADMIN,

        /**
         * 开发者用户
         */
        DEVELOPER;

        /**
         * 从字符串转换为枚举值
         *
         * @param value 字符串值
         * @return 用户类型枚举
         * @throws IllegalArgumentException 如果转换失败
         */
        public static UserType fromString(String value) {
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unsupported user type: " + value, e);
            }
        }
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
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .map(UserType::fromString)
                .findFirst()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("User type not found in authentication"));
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return true如果是管理员，否则false
     */
    public boolean isAdministrator() {
        try {
            return getCurrentUserType() == UserType.ADMIN;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    /**
     * 判断当前用户是否为开发者
     *
     * @return true如果是开发者，否则false
     */
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
