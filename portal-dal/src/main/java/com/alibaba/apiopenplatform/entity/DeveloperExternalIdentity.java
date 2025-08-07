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

    @ManyToOne
    @JoinColumn(name = "developer_id", referencedColumnName = "developerId", nullable = false)
    private Developer developer;

    @Column(nullable = false, length = 32)
    private String provider;

    @Column(nullable = false, length = 128)
    private String subject;

    @Column(nullable = true, length = 128)
    private String displayName;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawInfoJson;
} 