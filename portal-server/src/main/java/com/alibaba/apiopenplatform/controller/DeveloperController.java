package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.developer.DeveloperCreateDto;
import com.alibaba.apiopenplatform.dto.params.developer.DeveloperLoginDto;
import com.alibaba.apiopenplatform.dto.result.AuthResponseDto;
import com.alibaba.apiopenplatform.core.response.Response;
import com.alibaba.apiopenplatform.dto.result.DeveloperResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.service.DeveloperService;
import com.alibaba.apiopenplatform.core.security.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.validation.Valid;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * 开发者账号相关接口
 *
 * @author zxd
 */
@Tag(name = "开发者管理", description = "提供开发者认证、管理等功能")
@RestController
@RequestMapping("/api/developer")
@RequiredArgsConstructor
@Validated
public class DeveloperController {
    private final DeveloperService developerService;
    private final TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "开发者注册", description = "注册新开发者账号")
    @PostMapping("/register")
    public Response<String> register(
        @RequestBody(description = "开发者注册参数")
        @Valid @org.springframework.web.bind.annotation.RequestBody DeveloperCreateDto dto) {
        developerService.createDeveloper(dto);
        return Response.ok("注册成功，可直接登录");
    }

    @Operation(summary = "开发者登录", description = "开发者账号密码登录")
    @PostMapping("/login")
    public Response<AuthResponseDto> login(
        @RequestBody(description = "开发者登录参数")
        @Valid @org.springframework.web.bind.annotation.RequestBody DeveloperLoginDto dto) {
        return developerService.loginWithPassword(dto.getUsername(), dto.getPassword())
                .map(Response::ok)
                .orElseGet(() -> Response.fail("FAIL", "用户名或密码错误，或账号状态异常"));
    }

    @Operation(summary = "开发者登出", description = "将 token 加入黑名单，仅测试用")
    @PostMapping("/logout")
    public Response<Void> logout(
        @Parameter(description = "认证Token") @RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expireAt = System.currentTimeMillis() + 3600_000L;
            tokenBlacklistService.add(token, expireAt);
        }
        return Response.ok(null);
    }

    // @Operation(summary = "开发者个人信息", description = "受保护接口示例，仅测试用")
    // @GetMapping("/profile")
    // public Response<String> profile() {
    //     return Response.ok("开发者受保护信息");
    // }

    @Operation(summary = "解绑第三方登录", description = "解绑当前登录用户的指定第三方账号。providerName和providerSubject参数建议通过/identity/list-identities接口获取，前端自动传递，用户无需手动输入。")
    @DeleteMapping("/external-identity")
    public Response<Void> unbindExternalIdentity(
        @Parameter(description = "第三方平台名（如github、google、aliyun），建议通过绑定列表接口获取") @RequestParam String providerName,
        @Parameter(description = "第三方平台用户唯一标识，建议通过绑定列表接口获取") @RequestParam String providerSubject,
        @Parameter(description = "门户唯一标识，建议通过前端传递") @RequestParam String portalId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null ? authentication.getName() : null;
        if (userId == null || userId.isEmpty()) {
            return Response.fail("UNAUTHORIZED", "未登录，无法解绑");
        }
        developerService.unbindExternalIdentity(userId, providerName, providerSubject, portalId);
        return Response.ok(null);
    }

    @Operation(summary = "注销账号", description = "注销当前登录用户账号")
    @DeleteMapping("/account")
    public Response<Void> deleteAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication != null ? authentication.getName() : null;
        if (userId == null || userId.isEmpty()) {
            return Response.fail("UNAUTHORIZED", "未登录，无法注销");
        }
        developerService.deleteDeveloperAccount(userId);
        return Response.ok(null);
    }

    @Operation(summary = "获取门户的开发者列表")
    @GetMapping("/list")
    public PageResult<DeveloperResult> listDevelopers(@RequestParam String portalId,
                                                      @PageableDefault(sort = "gmt_create", direction = DESC) Pageable pageable) {
        return developerService.listDevelopers(portalId, pageable);
    }

    @Operation(summary = "设置开发者状态")
    @PostMapping("/status")
    public void setDeveloperStatus(@RequestParam String portalId,
                                   @RequestParam String developerId,
                                   @RequestParam String status) {
        developerService.setDeveloperStatus(portalId, developerId, status);
    }
}