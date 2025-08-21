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


package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.support.enums.UserType;
import lombok.Builder;
import lombok.Data;

/**
 */
@Data
@Builder
public class AuthResponseResult {

    private String userId;

    private String username;

    private UserType userType;

    private String token;

    public static AuthResponseResult fromDeveloper(String developerId, String username, String token) {
        return of(developerId, username, UserType.DEVELOPER, token);
    }

    public static AuthResponseResult fromAdmin(String adminId, String username, String token) {
        return of(adminId, username, UserType.ADMIN, token);
    }

    private static AuthResponseResult of(String userId, String username, UserType userType, String token) {
        return AuthResponseResult.builder()
                .userId(userId)
                .username(username)
                .userType(userType)
                .token(token)
                .build();
    }
} 