package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.support.enums.NacosStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * Nacos实例实体
 * @author zxd
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "nacos_instance",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"nacos_id"}, name = "uk_nacos_id"),
        })
@Data
public class NacosInstance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nacos_name", length = 64, nullable = false)
    private String nacosName;

    @Column(name = "nacos_id", length = 32, nullable = false)
    private String nacosId;

    @Column(name = "admin_id", length = 64, nullable = false)
    private String adminId;

    @Column(name = "server_url", length = 256, nullable = false)
    private String serverUrl;

    @Column(name = "namespace", length = 64)
    private String namespace;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "password", length = 128)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    private NacosStatus status;

    @Column(name = "description", length = 512)
    private String description;
} 