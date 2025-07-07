package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.admin.AdminCreateDto;
import com.alibaba.apiopenplatform.dto.params.admin.AdminLoginDto;
import com.alibaba.apiopenplatform.dto.result.AuthResponseDto;
import com.alibaba.apiopenplatform.service.AdministratorService;
import com.alibaba.apiopenplatform.core.response.Response;
import com.alibaba.apiopenplatform.core.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理员控制器，提供注册和登录等API接口
 *
 * @author zxd
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdministratorController {
    private final AdministratorService administratorService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 管理员注册（需传递portalId）
     */
    @PostMapping("/register")
    public Response<String> register(@Valid @RequestBody AdminCreateDto dto) {
        administratorService.createAdministrator(dto);
        return Response.ok("注册成功");
    }

    /**
     * 管理员登录（需传递portalId）
     */
    @PostMapping("/login")
    public Response<AuthResponseDto> login(@Valid @RequestBody AdminLoginDto dto) {
        return administratorService.loginWithPassword(dto.getPortalId(), dto.getUsername(), dto.getPassword())
                .map(Response::ok)
                .orElseGet(() -> Response.fail("AUTH_FAILED", "用户名或密码错误"));
    }

    /**
     * 管理员受保护接口，仅测试用
     */
    @GetMapping("/profile")
    public Response<String> profile() {
        return Response.ok("管理员受保护信息");
    }

    /**
     * 管理员登出，将token加入黑名单，支持portalId参数
     */
    @PostMapping("/logout")
    public Response<Void> logout(@RequestParam("portalId") String portalId,
                                 @RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expireAt = System.currentTimeMillis() + 3600_000L;
            tokenBlacklistService.add(token, expireAt);
        }
        return Response.ok(null);
    }

    /**
     * 检查指定portalId下是否需要初始化管理员
     */
    @GetMapping("/need-init")
    public Response<Boolean> needInit(@RequestParam("portalId") String portalId) {
        return Response.ok(administratorService.needInit(portalId));
    }

    /**
     * 初始化管理员，仅允许首次调用
     */
    @PostMapping("/init")
    public Response<String> initAdmin(@RequestBody AdminCreateDto dto) {
        administratorService.initAdmin(dto.getPortalId(), dto.getUsername(), dto.getPassword());
        return Response.ok("初始化成功");
    }

    /**
     * 管理员修改密码（需传递portalId、adminId、oldPassword、newPassword）
     */
    @PostMapping("/change-password")
    public Response<String> changePassword(@RequestParam("portalId") String portalId,
                                           @RequestParam("adminId") String adminId,
                                           @RequestParam("oldPassword") String oldPassword,
                                           @RequestParam("newPassword") String newPassword) {
        administratorService.changePassword(portalId, adminId, oldPassword, newPassword);
        return Response.ok("修改密码成功");
    }
} 