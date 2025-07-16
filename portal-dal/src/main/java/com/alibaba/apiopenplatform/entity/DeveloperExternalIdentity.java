package com.alibaba.apiopenplatform.entity;

import javax.persistence.*;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 开发者外部身份实体类，映射第三方认证信息
 *
 * @author zxd
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "developer_external_identity", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "subject"})
})
public class DeveloperExternalIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联开发者主键（developerId），类型为 varchar(64)
     */
    @ManyToOne
    @JoinColumn(name = "developer_id", referencedColumnName = "developerId", nullable = false)
    private Developer developer;

    /**
     * 外部身份提供方（如 github、google、aliyun）
     */
    @Column(nullable = false, length = 32)
    private String provider;

    /**
     * 外部身份唯一标识（如sub、id等）
     */
    @Column(nullable = false, length = 128)
    private String subject;

    /**
     * 统一存储第三方返回的"用户名/昵称/显示名"
     */
    @Column(nullable = true, length = 128)
    private String displayName;

    /**
     * 第三方原始信息，JSON字符串
     */
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawInfoJson;
} 