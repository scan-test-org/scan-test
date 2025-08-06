package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.admin.AdminCreateParam;
import com.alibaba.apiopenplatform.dto.params.admin.AdminLoginParam;
import com.alibaba.apiopenplatform.dto.params.admin.ChangePasswordParam;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import com.alibaba.apiopenplatform.dto.result.AdminResult;
import com.alibaba.apiopenplatform.service.AdministratorService;
import com.alibaba.apiopenplatform.core.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import com.alibaba.apiopenplatform.entity.Administrator;

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

    @Operation(summary = "管理员登出",
            description = "管理员登出，将当前token加入黑名单。前端需要清除localStorage中的token。")
    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        // 从Authorization头获取token并加入黑名单
        String authHeader = request.getHeader("Authorization");
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

    @Operation(summary = "管理员修改密码", description = "修改当前登录管理员的密码，需传递oldPassword、newPassword")
    @PatchMapping("/password")
    public String changePassword(@RequestBody ChangePasswordParam param) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication != null ? authentication.getName() : null;
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        administratorService.changePassword(currentUserId, param.getOldPassword(), param.getNewPassword());
        return "修改密码成功";
    }

    @Operation(summary = "获取当前登录管理员信息", description = "根据token自动获取当前登录管理员的详细信息")
    @GetMapping
    public AdminResult getCurrentAdminInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication != null ? authentication.getName() : null;
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        Optional<Administrator> adminOpt = administratorService.findByAdminId(currentUserId);
        if (!adminOpt.isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "admin", currentUserId);
        }
        Administrator admin = adminOpt.get();
        return new AdminResult().convertFrom(admin);
    }

    @Operation(summary = "管理员删除开发者", description = "管理员删除指定开发者账号")
    @DeleteMapping("/{developerId}")
    public String deleteDeveloper(@PathVariable("developerId") String developerId) {
        administratorService.deleteDeveloper(developerId);
        return "删除开发者成功";
    }
} 