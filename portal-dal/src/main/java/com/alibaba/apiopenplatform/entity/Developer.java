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


package com.alibaba.apiopenplatform.entity;

import javax.persistence.*;
import java.util.Date;

import com.alibaba.apiopenplatform.support.enums.DeveloperStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.io.Serializable;

/**
 * 开发者实体类，映射开发者账号信息
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "developer", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"developerId"}),
        @UniqueConstraint(columnNames = {"portalId", "username"})  // 按portalId+username组合唯一
})
public class Developer extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String developerId;

    @Column(nullable = true, length = 64)
    private String username;

    @Column(nullable = true)
    private String passwordHash;

    @Column(nullable = true, length = 128)
    private String email;

    @Column(nullable = false, length = 64)
    private String portalId;

    @Column(nullable = true, length = 256)
    private String avatarUrl; // 头像url

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private DeveloperStatus status; // PENDING, APPROVED

    @Column(nullable = false, length = 16)
    private String authType; // BUILT, OIDC

} 