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


package com.alibaba.apiopenplatform.support.portal;

import lombok.Data;

/**
 * 通用三方登录/自定义OIDC配置实体
 * 支持所有三方登录方式统一配置
 */
@Data
public class OidcConfig {
    private String id; // 唯一标识
    private String provider; // 平台标识，如 github、google、aliyun、oidc
    private String name; // 显示名，如“GitHub登录”、“公司SSO”
    private String logoUrl; // 登录按钮logo
    private String clientId;
    private String clientSecret;
    private String scopes;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String userInfoEndpoint;
    private String jwkSetUri;
    private String redirectUri;
    private boolean enabled; // 是否启用
}
