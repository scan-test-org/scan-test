package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.developer.DeveloperCreateParam;
import com.alibaba.apiopenplatform.dto.params.developer.DeveloperLoginParam;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import com.alibaba.apiopenplatform.core.response.Response;
import com.alibaba.apiopenplatform.dto.result.DeveloperResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.service.DeveloperService;
import com.alibaba.apiopenplatform.core.security.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import javax.validation.Valid;


import java.util.Collections;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import com.alibaba.apiopenplatform.dto.params.developer.UnbindExternalIdentityParam;
import com.alibaba.apiopenplatform.dto.params.developer.DeveloperStatusParam;

/**
 * 开发者账号相关接口
 *
 * @author zxd
 */
@Tag(name = "开发者管理", description = "提供开发者认证、管理等功能")
@RestController
@RequestMapping("/developers")
@RequiredArgsConstructor
@Validated
public class DeveloperController {
    private final DeveloperService developerService;
    private final TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "开发者注册", description = "注册新开发者账号")
    @PostMapping
    public void register(@Valid @RequestBody DeveloperCreateParam param) {
        developerService.createDeveloper(param);
    }

    @Operation(summary = "开发者登录", description = "开发者账号密码登录")
    @PostMapping("/login")
    public AuthResponseResult login(@Valid @RequestBody DeveloperLoginParam param) {
        return developerService.loginWithPassword(param.getUsername(), param.getPassword())
                .orElseThrow(() -> new RuntimeException("AUTH_FAILED"));
    }

    @Operation(summary = "开发者登出", description = "登出")
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expireAt = System.currentTimeMillis() + 3600_000L;
            tokenBlacklistService.add(token, expireAt);
        }
    }

    // @Operation(summary = "开发者个人信息", description = "受保护接口示例，仅测试用")
    // @GetMapping("/profile")
    // public Response<String> profile() {
    //     return Response.ok("开发者受保护信息");
    // }

    @Operation(summary = "解绑第三方登录", description = "解绑当前登录用户的指定第三方账号。providerName和providerSubject参数建议通过/list-identities接口获取。")
    @DeleteMapping("/{developerId}/external-identity")
    public void unbindExternalIdentity(@PathVariable("developerId") String developerId,
                                       @RequestBody UnbindExternalIdentityParam param) {
        developerService.unbindExternalIdentity(developerId, param.getProviderName(), param.getProviderSubject(), param.getPortalId());
    }

    @Operation(summary = "注销账号", description = "注销当前登录用户账号")
    @DeleteMapping("/{developerId}")
    public void deleteAccount(@PathVariable("developerId") String developerId) {
        developerService.deleteDeveloperAccount(developerId);
    }

    @Operation(summary = "获取门户的开发者列表")
    @GetMapping
    public PageResult<DeveloperResult> listDevelopers(@RequestParam String portalId,
                                        @PageableDefault(sort = "gmtCreate", direction = Sort.Direction.DESC) Pageable pageable) {
        return developerService.listDevelopers(portalId, pageable);
    }

    @Operation(summary = "设置开发者状态", description = "管理员审核开发者账号，status为APPROVED/PENDING等")
    @PatchMapping("/{developerId}/status")
    public void setDeveloperStatus(@PathVariable("developerId") String developerId,
                                   @RequestBody DeveloperStatusParam param) {
        developerService.setDeveloperStatus(param.getPortalId(), developerId, param.getStatus());
    }
}