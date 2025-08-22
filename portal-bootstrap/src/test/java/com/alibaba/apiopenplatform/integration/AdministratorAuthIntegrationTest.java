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

package com.alibaba.apiopenplatform.integration;

import com.alibaba.apiopenplatform.dto.params.admin.AdminCreateParam;
import com.alibaba.apiopenplatform.dto.params.admin.AdminLoginParam;
import com.alibaba.apiopenplatform.core.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 管理员初始化、登录、token认证、权限保护、黑名单集成测试
 *
 */
@SpringBootTest(classes = com.alibaba.apiopenplatform.PortalApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdministratorAuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testAdminRegister() {
        AdminCreateParam createDto = new AdminCreateParam();
        createDto.setUsername("admintest001");
        createDto.setPassword("admin123456");
        ResponseEntity<Response> registerResp = restTemplate.postForEntity(
                "/api/admin/init", createDto, Response.class);
        System.out.println("管理员初始化响应：" + registerResp);
        assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registerResp.getBody().getCode()).isEqualTo("Success");
    }

    @Test
    void testAdminLogin() {
        AdminLoginParam loginDto = new AdminLoginParam();
        loginDto.setUsername("admintest001");
        loginDto.setPassword("admin123456");
        ResponseEntity<Response> loginResp = restTemplate.postForEntity(
                "/api/admin/login", loginDto, Response.class);
        System.out.println("管理员登录响应：" + loginResp);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody().getCode()).isEqualTo("Success");
    }

    @Test
    void testAdminProtectedApiWithValidToken() {
        // 登录获取token
        AdminLoginParam loginDto = new AdminLoginParam();
        loginDto.setUsername("admintest001");
        loginDto.setPassword("admin123456");
        ResponseEntity<Response> loginResp = restTemplate.postForEntity(
                "/api/admin/login", loginDto, Response.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody().getCode()).isEqualTo("Success");
        String token = ((Map<String, Object>)loginResp.getBody().getData()).get("token").toString();

        // 用token访问受保护接口
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        // 你需要在管理员Controller实现 /api/admin/profile 受保护接口
        ResponseEntity<String> protectedResp = restTemplate.exchange(
                "/api/admin/profile", HttpMethod.GET, entity, String.class);
        System.out.println("管理员带token访问受保护接口响应：" + protectedResp);
        assertThat(protectedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(protectedResp.getBody()).contains("管理员受保护信息");
    }

    @Test
    void testAdminProtectedApiWithoutToken() {
        // 不带token访问受保护接口
        ResponseEntity<String> protectedResp = restTemplate.getForEntity(
                "/api/admin/profile", String.class);
        System.out.println("管理员不带token访问受保护接口响应：" + protectedResp);
        assertThat(protectedResp.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void testAdminTokenBlacklist() {
        // 登录获取token
        AdminLoginParam loginDto = new AdminLoginParam();
        loginDto.setUsername("admintest001");
        loginDto.setPassword("admin123456");
        ResponseEntity<Response> loginResp = restTemplate.postForEntity(
                "/api/admin/login", loginDto, Response.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody().getCode()).isEqualTo("Success");
        String token = ((Map<String, Object>)loginResp.getBody().getData()).get("token").toString();

        // 调用登出接口，将token加入黑名单
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        // 修正：带上portalId参数
        ResponseEntity<Response> logoutResp = restTemplate.postForEntity(
                "/api/admin/logout?portalId=default", entity, Response.class);
        System.out.println("管理员登出响应：" + logoutResp);
        assertThat(logoutResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 再次用该token访问受保护接口，预期401或403
        ResponseEntity<String> protectedResp = restTemplate.exchange(
                "/api/admin/profile", HttpMethod.GET, entity, String.class);
        System.out.println("管理员黑名单token访问受保护接口响应：" + protectedResp);
        assertThat(protectedResp.getStatusCode().value()).isIn(401, 403);
    }

    @Test
    void testNeedInitBeforeAndAfterInit() {
        // 初始化前，need-init 应为 true
        ResponseEntity<Response> respBefore = restTemplate.getForEntity(
                "/api/admin/need-init?portalId=default", Response.class);
        assertThat(respBefore.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respBefore.getBody()).isNotNull();
        assertThat(respBefore.getBody().getCode()).isEqualTo("SUCCESS");
        assertThat(respBefore.getBody().getData()).isEqualTo(true);
        assertThat(respBefore.getBody().getMessage()).isNotNull();

        // 初始化
        AdminCreateParam createDto = new AdminCreateParam();
        createDto.setUsername("admintest002");
        createDto.setPassword("admin123456");
        ResponseEntity<Response> initResp = restTemplate.postForEntity(
                "/api/admin/init", createDto, Response.class);
        assertThat(initResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(initResp.getBody()).isNotNull();
        assertThat(initResp.getBody().getCode()).isEqualTo("SUCCESS");
        assertThat(initResp.getBody().getMessage()).isNotNull();

        // 初始化后，need-init 应为 false
        ResponseEntity<Response<Boolean>> respAfter = restTemplate.exchange(
                "/api/admin/need-init?portalId=default",
                HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<Response<Boolean>>() {}
        );
        assertThat(respAfter.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respAfter.getBody()).isNotNull();
        assertThat(respAfter.getBody().getCode()).isEqualTo("SUCCESS");
        assertThat(respAfter.getBody().getData()).isEqualTo(false);
        assertThat(respAfter.getBody().getMessage()).isNotNull();

    }

    @Test
    void testChangePasswordSuccessAndFail() {
        // 初始化并登录
        AdminCreateParam createDto = new AdminCreateParam();
        createDto.setUsername("admintest004");
        createDto.setPassword("admin123456");
        restTemplate.postForEntity("/api/admin/init", createDto, Response.class);
        AdminLoginParam loginDto = new AdminLoginParam();
        loginDto.setUsername("admintest004");
        loginDto.setPassword("admin123456");
        ResponseEntity<Response> loginResp = restTemplate.postForEntity(
                "/api/admin/login", loginDto, Response.class);
        String token = ((Map<String, Object>)loginResp.getBody().getData()).get("token").toString();
        String adminId = ((Map<String, Object>)loginResp.getBody().getData()).get("userId").toString();

        // 正确修改密码
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> emptyBody = new LinkedMultiValueMap<>();
        String changeUrl = String.format("/api/admin/change-password?portalId=%s&adminId=%s&oldPassword=%s&newPassword=%s",
                "default", adminId, "admin123456", "admin654321");
        HttpEntity<MultiValueMap<String, String>> changeEntity = new HttpEntity<>(emptyBody, headers);
        ResponseEntity<Response> changeResp = restTemplate.postForEntity(
                changeUrl, changeEntity, Response.class);
        assertThat(changeResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(changeResp.getBody()).isNotNull();
        assertThat(changeResp.getBody().getCode()).isEqualTo("SUCCESS");
        assertThat(changeResp.getBody().getMessage()).isNotNull();

        // 原密码错误
        String wrongUrl = String.format("/api/admin/change-password?portalId=%s&adminId=%s&oldPassword=%s&newPassword=%s",
                "default", adminId, "wrongpass", "admin654321");
        HttpEntity<MultiValueMap<String, String>> wrongEntity = new HttpEntity<>(emptyBody, headers);
        ResponseEntity<Response> failResp = restTemplate.postForEntity(
                wrongUrl, wrongEntity, Response.class);
        assertThat(failResp.getStatusCode().value()).isIn(401, 400, 409, 403);
        assertThat(failResp.getBody()).isNotNull();
        assertThat(failResp.getBody().getCode()).isIn("ADMIN_PASSWORD_INCORRECT", "INVALID_PARAMETER");
        assertThat(failResp.getBody().getMessage()).isNotNull();
    }
} 