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


package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.support.portal.OidcConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 开发者OAuth2服务接口
 * 处理OAuth2授权、回调、用户信息获取等逻辑
 *
 */
public interface DeveloperOAuth2Service {

    /**
     * OIDC授权入口
     *
     * @param provider 提供商名称
     * @param state 状态参数
     * @param request HTTP请求
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void handleAuthorize(String provider, String state, HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * OIDC统一回调处理
     *
     * @param code 授权码
     * @param state 状态参数
     * @param request HTTP请求
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void handleCallback(String code, String state, HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * 查询当前用户所有外部身份绑定
     *
     * @return 外部身份列表
     */
    List<Map<String, Object>> listCurrentUserIdentities();

    /**
     * 查询指定门户下所有已启用的OIDC登录方式
     *
     * @return OIDC提供商列表
     */
    List<Map<String, Object>> listOidcProviders();

    /**
     * 生成重定向URI
     *
     * @param request HTTP请求
     * @return 重定向URI
     */
    String generateRedirectUri(HttpServletRequest request);
}
