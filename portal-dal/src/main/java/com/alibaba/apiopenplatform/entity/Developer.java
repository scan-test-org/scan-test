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
 * @author zxd
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "developer", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"developerId"}),
        @UniqueConstraint(columnNames = {"username"})
})
public class Developer extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String developerId;

    @Column(nullable = true, unique = true, length = 64)
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