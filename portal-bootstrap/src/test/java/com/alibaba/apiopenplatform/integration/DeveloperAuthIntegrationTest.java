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

import com.alibaba.apiopenplatform.dto.params.developer.DeveloperCreateParam;
import com.alibaba.apiopenplatform.dto.params.developer.DeveloperLoginParam;
import com.alibaba.apiopenplatform.core.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 开发者注册、登录分步集成测试
 *
 */
@SpringBootTest(classes = com.alibaba.apiopenplatform.PortalApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeveloperAuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testRegister() {
        DeveloperCreateParam createDto = new DeveloperCreateParam();
        createDto.setUsername("devtest001");
        createDto.setPassword("dev123456");
        ResponseEntity<Response> registerResp = restTemplate.postForEntity(
                "/api/developer/register", createDto, Response.class);
        System.out.println("注册响应：" + registerResp);
        assertThat(registerResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(registerResp.getBody().getCode()).isEqualTo("SUCCESS");
    }

    @Test
    void testLogin() {
        DeveloperLoginParam loginDto = new DeveloperLoginParam("devtest002", "123456");
        ResponseEntity<Response> loginResp = restTemplate.postForEntity(
                "/api/developer/login", loginDto, Response.class);
        System.out.println("登录响应：" + loginResp);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody().getCode()).isEqualTo("SUCCESS");
    }

    @Test
    void testProtectedApiWithValidToken() {
        // 先登录获取token
        DeveloperLoginParam loginDto = new DeveloperLoginParam("devtest002", "123456");
        ResponseEntity<Response> loginResp = restTemplate.postForEntity(
                "/api/developer/login", loginDto, Response.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody().getCode()).isEqualTo("SUCCESS");
        String token = ((Map<String, Object>)loginResp.getBody().getData()).get("token").toString();

        // 用token访问受保护接口
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> protectedResp = restTemplate.exchange(
                "/api/developer/profile", HttpMethod.GET, entity, String.class);
        System.out.println("带token访问受保护接口响应：" + protectedResp);
        assertThat(protectedResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(protectedResp.getBody()).contains("开发者受保护信息");
    }

    @Test
    void testProtectedApiWithoutToken() {
        // 不带token访问受保护接口
        ResponseEntity<String> protectedResp = restTemplate.getForEntity(
                "/api/developer/profile", String.class);
        System.out.println("不带token访问受保护接口响应：" + protectedResp);
        assertThat(protectedResp.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void testTokenBlacklist() {
        // 1. 登录获取token
        DeveloperLoginParam loginDto = new DeveloperLoginParam("devtest002", "123456");
        ResponseEntity<Response> loginResp = restTemplate.postForEntity(
                "/api/developer/login", loginDto, Response.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody().getCode()).isEqualTo("SUCCESS");
        String token = ((Map<String, Object>)loginResp.getBody().getData()).get("token").toString();

        // 2. 调用登出接口，将token加入黑名单
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Response> logoutResp = restTemplate.postForEntity(
                "/api/developer/logout", entity, Response.class);
        System.out.println("登出响应：" + logoutResp);
        assertThat(logoutResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3. 再次用该token访问受保护接口，预期401
        ResponseEntity<String> protectedResp = restTemplate.exchange(
                "/api/developer/profile", HttpMethod.GET, entity, String.class);
        System.out.println("黑名单token访问受保护接口响应：" + protectedResp);
        assertThat(protectedResp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testDeleteAccount() {
        // 注册
        DeveloperCreateParam createDto = new DeveloperCreateParam();
        createDto.setUsername("devtest003");
        createDto.setPassword("123456");
        restTemplate.postForEntity("/api/developer/register", createDto, Response.class);

        // 登录获取token
        DeveloperLoginParam loginDto = new DeveloperLoginParam("devtest003", "123456");
        ResponseEntity<Response> loginResp = restTemplate.postForEntity("/api/developer/login", loginDto, Response.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody().getCode()).isEqualTo("SUCCESS");
        String token = ((Map<String, Object>)loginResp.getBody().getData()).get("token").toString();

        // 注销（带token）
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Response> deleteResp = restTemplate.exchange(
                "/api/developer/account?userId=devtest003",
                HttpMethod.DELETE,
                entity,
                Response.class);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        // 再次登录，预期失败
        ResponseEntity<Response> loginResp2 = restTemplate.postForEntity("/api/developer/login", loginDto, Response.class);
        assertThat(loginResp2.getBody().getCode()).isNotEqualTo("SUCCESS");
    }
} 