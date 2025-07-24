package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.admin.AdminCreateParam;
import com.alibaba.apiopenplatform.dto.params.admin.AdminLoginParam;
import com.alibaba.apiopenplatform.dto.params.admin.ChangePasswordParam;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
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

import java.util.Map;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

/**
 * 管理员控制器，提供注册和登录等API接口
 *
 * @author zxd
 */
@Tag(name = "管理员管理", description = "管理员初始化、登录、修改密码等相关接口")
@RestController
@RequestMapping("/admins")
@RequiredArgsConstructor
@Validated
public class AdministratorController {
    private final AdministratorService administratorService;
    private final TokenBlacklistService tokenBlacklistService;



    @Operation(summary = "管理员登录", description = "管理员登录，只需用户名和密码。前端只需传username和password，后端自动校验。")
    @PostMapping("/login")
    public AuthResponseResult login(@Valid @RequestBody AdminLoginParam param) {
        return administratorService.loginWithPassword(param.getUsername(), param.getPassword())
                .orElseThrow(() -> new RuntimeException("AUTH_FAILED"));
    }

    // @Operation(summary = "管理员受保护接口", description = "仅测试用，返回管理员受保护信息")
    // @GetMapping("/profile")
    // public Response<String> profile() {
    //     return Response.ok("管理员受保护信息");
    // }

    @Operation(summary = "管理员登出", description = "将 token 加入黑名单，前端自动传递Authorization请求头。")
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expireAt = System.currentTimeMillis() + 3600_000L;
            tokenBlacklistService.add(token, expireAt);
        }
    }

    @Operation(summary = "检查是否需要初始化管理员", description = "检查系统是否需要初始化管理员（全表无记录时返回true）。")
    @GetMapping("/need-init")
    public Boolean needInit() {
        return administratorService.needInit();
    }

    @Operation(summary = "初始化管理员", description = "仅允许首次调用（全表无记录时），前端需传username和password。")
    @PostMapping("/init")
    public String initAdmin(@Valid @RequestBody AdminCreateParam param) {
        administratorService.initAdmin(param.getUsername(), param.getPassword());
        return "初始化成功";
    }

    @Operation(summary = "管理员修改密码", description = "需传递adminId、oldPassword、newPassword，前端自动传递token，后端校验当前登录管理员和adminId是否一致，防止越权。")
    @PatchMapping("/{adminId}/password")
    public String changePassword(@PathVariable("adminId") String adminId,
                                 @RequestBody ChangePasswordParam param) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication != null ? authentication.getName() : null;
        if (currentUserId == null || !currentUserId.equals(adminId)) {
            throw new RuntimeException("UNAUTHORIZED");
        }
        administratorService.changePassword(adminId, param.getOldPassword(), param.getNewPassword());
        return "修改密码成功";
    }
} 