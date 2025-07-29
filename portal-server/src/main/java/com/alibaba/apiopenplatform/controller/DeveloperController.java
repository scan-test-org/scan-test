package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.dto.params.developer.*;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import com.alibaba.apiopenplatform.core.response.Response;
import com.alibaba.apiopenplatform.dto.result.DeveloperResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.dto.result.AuthResponseResult;
import com.alibaba.apiopenplatform.service.DeveloperService;
import com.alibaba.apiopenplatform.core.security.TokenBlacklistService;
import com.alibaba.apiopenplatform.core.security.ContextHolder;
import com.alibaba.apiopenplatform.entity.Developer;
import com.alibaba.apiopenplatform.entity.PortalSetting;
import com.alibaba.apiopenplatform.entity.Portal;
import com.alibaba.apiopenplatform.repository.PortalRepository;

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

import javax.validation.Valid;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;
import com.alibaba.apiopenplatform.dto.params.admin.ChangePasswordParam;
import com.alibaba.apiopenplatform.core.exception.BusinessException;
import com.alibaba.apiopenplatform.core.exception.ErrorCode;

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
    private final ContextHolder contextHolder;
    private final PortalRepository portalRepository;

    @Operation(summary = "开发者注册", description = "注册新开发者账号")
    @PostMapping
    public void register(@Valid @RequestBody DeveloperCreateParam param, HttpServletResponse response) {
        // 注册开发者
        Developer developer = developerService.createDeveloper(param);
        
        // 根据门户配置决定是否自动登录
        String portalId = contextHolder.getPortal();
        // 通过portalId查询对应的Portal，然后获取PortalSetting
        Optional<Portal> portalOpt = portalRepository.findByPortalId(portalId);
        PortalSetting setting = null;
        if (portalOpt.isPresent()) {
            Portal portal = portalOpt.get();
            setting = portal.getPortalSetting();
        }
        boolean autoApprove = setting != null && Boolean.TRUE.equals(setting.getAutoApproveDevelopers());
        
        if (autoApprove) {
            // 如果自动审批，则自动登录
            AuthResponseResult authResult = developerService.generateAuthResult(developer);
            // 设置SameSite属性，避免浏览器警告
            response.setHeader("Set-Cookie", "token=" + authResult.getToken() + "; Path=/; Max-Age=3600; SameSite=Lax; HttpOnly=false");
        }
        // 如果不自动审批，则注册成功但不登录，需要等待管理员审批
    }

    @Operation(summary = "开发者登录", description = "开发者账号密码登录")
    @PostMapping("/login")
    public void login(@Valid @RequestBody DeveloperLoginParam param, HttpServletResponse response) {
        AuthResponseResult authResult = developerService.loginWithPassword(param.getUsername(), param.getPassword());
        
        // 设置cookie而不是返回token，设置SameSite属性，避免浏览器警告
        response.setHeader("Set-Cookie", "token=" + authResult.getToken() + "; Path=/; Max-Age=3600; SameSite=Lax; HttpOnly=false");
    }

    @Operation(summary = "开发者登出", description = "登出")
    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 从cookie中获取token并加入黑名单
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    long expireAt = System.currentTimeMillis() + 3600_000L;
                    tokenBlacklistService.add(token, expireAt);
                    break;
                }
            }
        }
        
        // 清除cookie
        response.setHeader("Set-Cookie", "token=; Path=/; Max-Age=0; SameSite=Lax");
    }

    // @Operation(summary = "开发者个人信息", description = "受保护接口示例，仅测试用")
    // @GetMapping("/profile")
    // public Response<String> profile() {
    //     return Response.ok("开发者受保护信息");
    // }

    @Operation(summary = "解绑第三方登录", description = "解绑当前登录用户的指定第三方账号。providerName和providerSubject参数建议通过/list-identities接口获取。")
    @DeleteMapping("/{developerId}/identity")
    public void unbindExternalIdentity(@PathVariable("developerId") String developerId,
                                       @RequestBody UnbindExternalIdentityParam param) {
        String portalId = contextHolder.getPortal();
        developerService.unbindExternalIdentity(developerId, param.getProviderName(), param.getProviderSubject(), portalId);
    }

    @Operation(summary = "注销账号", description = "注销当前登录用户账号")
    @DeleteMapping("/{developerId}")
    public void deleteAccount(@PathVariable("developerId") String developerId) {
        developerService.deleteDeveloperAccount(developerId);
    }

    @Operation(summary = "获取门户的开发者列表", description = "管理员功能：获取当前门户下所有开发者的分页列表")
    @GetMapping
    public PageResult<DeveloperResult> listDevelopers(@PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String portalId = contextHolder.getPortal();
        return developerService.listDevelopers(portalId, pageable);
    }

    @Operation(summary = "设置开发者状态", description = "管理员审核开发者账号，status为APPROVED/PENDING等")
    @PatchMapping("/{developerId}/status")
    public void setDeveloperStatus(@PathVariable("developerId") String developerId,
                                   @RequestBody DeveloperStatusParam param) {
        String portalId = contextHolder.getPortal();
        developerService.setDeveloperStatus(portalId, developerId, param.getStatus());
    }

    @Operation(summary = "获取当前开发者信息", description = "开发者功能：获取当前登录开发者的个人信息")
    @GetMapping("/profile")
    public Map<String, Object> getCurrentDeveloperInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication != null ? authentication.getName() : null;
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        
        Optional<Developer> devOpt = developerService.findByDeveloperId(currentUserId);
        if (!devOpt.isPresent()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "developer", currentUserId);
        }
        
        Developer developer = devOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("developerId", developer.getDeveloperId());
        result.put("username", developer.getUsername());
        result.put("email", developer.getEmail());
        result.put("avatarUrl", developer.getAvatarUrl());
        result.put("status", developer.getStatus());
        result.put("authType", developer.getAuthType());
        result.put("createAt", developer.getCreateAt());
        result.put("updatedAt", developer.getUpdatedAt());
        return result;
    }

    @Operation(summary = "开发者修改密码", description = "修改当前登录开发者的密码")
    @PatchMapping("/password")
    public String changePassword(@RequestBody ChangePasswordParam param) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication != null ? authentication.getName() : null;
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        developerService.changePassword(currentUserId, param.getOldPassword(), param.getNewPassword());
        return "修改密码成功";
    }

    @Operation(summary = "开发者更新个人信息", description = "开发者功能：更新当前登录开发者的个人信息，包括用户名、邮箱、头像等")
    @PutMapping("/profile")
    public String updateProfile(@Valid @RequestBody UpdateDeveloperProfileParam param) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication != null ? authentication.getName() : null;
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        developerService.updateProfile(currentUserId, param.getUsername(), param.getEmail(), param.getAvatarUrl());
        return "更新个人信息成功";
    }
}