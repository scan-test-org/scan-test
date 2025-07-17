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
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 管理员控制器，提供注册和登录等API接口
 *
 * @author zxd
 */
@Tag(name = "管理员管理", description = "管理员初始化、登录、修改密码等相关接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdministratorController {
    private final AdministratorService administratorService;
    private final TokenBlacklistService tokenBlacklistService;



    @Operation(summary = "管理员登录", description = "管理员登录，只需用户名和密码。前端只需传username和password，后端自动校验，无需portalId。")
    @PostMapping("/login")
    public Response<AuthResponseDto> login(
        @RequestBody(description = "管理员登录参数")
        @Valid @org.springframework.web.bind.annotation.RequestBody AdminLoginDto dto) {
        return administratorService.loginWithPassword(dto.getUsername(), dto.getPassword())
                .map(Response::ok)
                .orElseGet(() -> Response.fail("AUTH_FAILED", "用户名或密码错误"));
    }

    // @Operation(summary = "管理员受保护接口", description = "仅测试用，返回管理员受保护信息")
    // @GetMapping("/profile")
    // public Response<String> profile() {
    //     return Response.ok("管理员受保护信息");
    // }

    @Operation(summary = "管理员登出", description = "将 token 加入黑名单，前端自动传递Authorization请求头，无需用户手动输入。")
    @PostMapping("/logout")
    public Response<Void> logout(
        @Parameter(description = "认证Token，前端自动传递Authorization请求头") @RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expireAt = System.currentTimeMillis() + 3600_000L;
            tokenBlacklistService.add(token, expireAt);
        }
        return Response.ok(null);
    }

    @Operation(summary = "检查是否需要初始化管理员", description = "检查系统是否需要初始化管理员（全表无记录时返回true），前端无需传递portalId。")
    @GetMapping("/need-init")
    public Response<Boolean> needInit() {
        return Response.ok(administratorService.needInit());
    }

    @Operation(summary = "初始化管理员", description = "仅允许首次调用（全表无记录时），前端只需传username和password，无需portalId。")
    @PostMapping("/init")
    public Response<String> initAdmin(
        @RequestBody(description = "初始化管理员参数")
        @org.springframework.web.bind.annotation.RequestBody AdminCreateDto dto) {
        administratorService.initAdmin(dto.getUsername(), dto.getPassword());
        return Response.ok("初始化成功");
    }

    @Operation(summary = "管理员修改密码", description = "需传递adminId、oldPassword、newPassword，前端自动传递token，后端校验当前登录管理员和adminId是否一致，防止越权。")
    @PostMapping("/change-password")
    public Response<String> changePassword(
        @Parameter(description = "管理员ID") @RequestParam("adminId") String adminId,
        @Parameter(description = "旧密码") @RequestParam("oldPassword") String oldPassword,
        @Parameter(description = "新密码") @RequestParam("newPassword") String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication != null ? authentication.getName() : null;
        if (currentUserId == null || !currentUserId.equals(adminId)) {
            return Response.fail("UNAUTHORIZED", "无权修改他人密码");
        }
        administratorService.changePassword(adminId, oldPassword, newPassword);
        return Response.ok("修改密码成功");
    }
} 