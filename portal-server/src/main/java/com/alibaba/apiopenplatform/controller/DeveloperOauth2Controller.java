/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.alibaba.apiopenplatform.controller;

import com.alibaba.apiopenplatform.service.DeveloperOAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 开发者OAuth2登录管理Controller
 * 提供OAuth2授权、回调等接口
 *
 */
@Slf4j
@Tag(name = "开发者OAuth2登录管理", description = "开发者OAuth2统一回调与外部身份绑定相关接口")
@RestController
@RequestMapping("/developers")
@RequiredArgsConstructor
public class DeveloperOauth2Controller {

    private final DeveloperOAuth2Service developerOAuth2Service;

    @Operation(summary = "OIDC授权入口", description = "重定向到第三方登录页面")
    @GetMapping("/authorize")
    public void universalAuthorize(
            @Parameter(description = "提供商名称") @RequestParam String provider, 
            @Parameter(description = "状态参数") @RequestParam String state, 
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        developerOAuth2Service.handleAuthorize(provider, state, request, response);
    }

    @Operation(summary = "OIDC统一回调", description = "处理第三方登录回调")
    @GetMapping("/callback")
    public void oidcCallback(
            @Parameter(description = "授权码") @RequestParam String code, 
            @Parameter(description = "状态参数") @RequestParam String state, 
            HttpServletRequest request, 
            HttpServletResponse response) throws IOException {
        developerOAuth2Service.handleCallback(code, state, request, response);
    }

    @Operation(summary = "查询当前用户所有外部身份绑定", description = "只返回provider、subject、displayName、rawInfoJson")
    @PostMapping("/list-identities")
    public List<Map<String, Object>> listIdentities() {
        return developerOAuth2Service.listCurrentUserIdentities();
    }

    @Operation(summary = "查询指定门户下所有已启用的OIDC登录方式", description = "返回provider、displayName、icon、enabled等信息")
    @PostMapping("/providers")
    public List<Map<String, Object>> listOidcProviders() {
        return developerOAuth2Service.listOidcProviders();
    }
} 