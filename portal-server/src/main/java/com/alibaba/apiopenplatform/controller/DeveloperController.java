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
import com.alibaba.apiopenplatform.dto.params.developer.UnbindExternalIdentityDto;
import com.alibaba.apiopenplatform.dto.params.developer.DeveloperStatusDto;

/**
 * 开发者账号相关接口
 *
 * @author zxd
 */
@Tag(name = "开发者管理", description = "提供开发者认证、管理等功能")
@RestController
@RequestMapping("/developer")
@RequiredArgsConstructor
@Validated
public class DeveloperController {
    private final DeveloperService developerService;
    private final TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "开发者注册", description = "注册新开发者账号")
    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody DeveloperCreateDto dto) {
        developerService.createDeveloper(dto);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "开发者登录", description = "开发者账号密码登录")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody DeveloperLoginDto dto) {
        Optional<AuthResponseDto> result = developerService.loginWithPassword(dto.getUsername(), dto.getPassword());
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "AUTH_FAILED"));
        }
    }

    @Operation(summary = "开发者登出", description = "将 token 加入黑名单，仅测试用")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expireAt = System.currentTimeMillis() + 3600_000L;
            tokenBlacklistService.add(token, expireAt);
        }
        return ResponseEntity.ok().build();
    }

    // @Operation(summary = "开发者个人信息", description = "受保护接口示例，仅测试用")
    // @GetMapping("/profile")
    // public Response<String> profile() {
    //     return Response.ok("开发者受保护信息");
    // }

    @Operation(summary = "解绑第三方登录", description = "解绑当前登录用户的指定第三方账号。providerName和providerSubject参数建议通过/identity/list-identities接口获取，前端自动传递，用户无需手动输入。")
    @DeleteMapping("/{id}/external-identity")
    public ResponseEntity<?> unbindExternalIdentity(@PathVariable String id,
                                                @RequestBody UnbindExternalIdentityDto dto) {
        developerService.unbindExternalIdentity(id, dto.getProviderName(), dto.getProviderSubject(), dto.getPortalId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "注销账号", description = "注销当前登录用户账号")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable String id) {
        developerService.deleteDeveloperAccount(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "获取门户的开发者列表")
    @GetMapping
    public ResponseEntity<?> listDevelopers(@RequestParam String portalId,
                                        @PageableDefault(sort = "gmtCreate", direction = DESC) Pageable pageable) {
        return ResponseEntity.ok(developerService.listDevelopers(portalId, pageable));
    }

    @Operation(summary = "设置开发者状态", description = "管理员审核开发者账号，status可为ACTIVE/APPROVED/REJECTED等")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> setDeveloperStatus(@PathVariable String id,
                                            @RequestBody DeveloperStatusDto dto) {
        developerService.setDeveloperStatus(dto.getPortalId(), id, dto.getStatus());
        return ResponseEntity.ok().build();
    }
}