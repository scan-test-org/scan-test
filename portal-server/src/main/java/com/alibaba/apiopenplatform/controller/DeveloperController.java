package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.developer.DeveloperCreateDto;
import com.alibaba.apiopenplatform.dto.params.developer.DeveloperLoginDto;
import com.alibaba.apiopenplatform.dto.result.AuthResponseDto;
import com.alibaba.apiopenplatform.core.response.Response;
import com.alibaba.apiopenplatform.service.DeveloperService;
import com.alibaba.apiopenplatform.core.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.alibaba.apiopenplatform.dto.params.developer.DeveloperBindExternalDto;

import javax.validation.Valid;

/**
 * 开发者账号相关接口
 *
 * @author zxd
 */
@RestController
@RequestMapping("/api/developer")
@RequiredArgsConstructor
@Validated
public class DeveloperController {
    private final DeveloperService developerService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 开发者注册
     */
    @PostMapping("/register")
    public Response<String> register(@Valid @RequestBody DeveloperCreateDto dto) {
        developerService.createDeveloper(dto);
        return Response.ok("注册成功，可直接登录");
    }

    /**
     * 开发者登录
     */
    @PostMapping("/login")
    public Response<AuthResponseDto> login(@Valid @RequestBody DeveloperLoginDto dto) {
        return developerService.loginWithPassword(dto.getUsername(), dto.getPassword())
                .map(Response::ok)
                .orElseGet(() -> Response.fail("FAIL", "用户名或密码错误，或账号状态异常"));
    }

    /**
     * 开发者登出，将token加入黑名单
     * 纯测试使用
     * 可忽略
     */
    @PostMapping("/logout")
    public Response<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // 这里简单设为当前时间+1小时
            long expireAt = System.currentTimeMillis() + 3600_000L;
            tokenBlacklistService.add(token, expireAt);
        }
        return Response.ok(null);
    }

    /**
     * 受保护接口示例：开发者个人信息
     * 纯测试使用
     * 可忽略
     */
    @GetMapping("/profile")
    public Response<String> profile() {
        // 实际可返回当前开发者信息，这里仅作演示
        return Response.ok("开发者受保护信息");
    }

    /**
     * 解绑第三方登录
     */
    @DeleteMapping("/external-identity")
    public Response<Void> unbindExternalIdentity(@RequestParam String providerName, @RequestParam String providerSubject, @RequestParam String userId) {
        developerService.unbindExternalIdentity(userId, providerName, providerSubject);
        return Response.ok(null);
    }

    /**
     * 注销账号
     */
    @DeleteMapping("/account")
    public Response<Void> deleteAccount(@RequestParam String userId) {
        developerService.deleteDeveloperAccount(userId);
        return Response.ok(null);
    }

    /**
     * 绑定第三方账号（已登录用户专用）
     */
    @PostMapping("/external-identity")
    public Response<Void> bindExternalIdentity(@Valid @RequestBody DeveloperBindExternalDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null ? authentication.getName() : null;
        if (userId == null || userId.isEmpty()) {
            return Response.fail("UNAUTHORIZED", "未登录，无法绑定");
        }
        developerService.bindExternalIdentity(userId, dto.getProviderName(), dto.getProviderSubject(), dto.getDisplayName(), dto.getRawInfoJson());
        return Response.ok(null);
    }
} 