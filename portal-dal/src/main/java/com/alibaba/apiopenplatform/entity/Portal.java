package com.alibaba.apiopenplatform.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_apim_portal",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"portal_id"}, name = "uk_portal_id"),
                @UniqueConstraint(columnNames = {"name", "admin_id"}, name = "uk_name_admin_id")
        })
@Data
public class Portal extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "portal_id", length = 32, nullable = false)
    private String portalId;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "admin_id", length = 32)
    private String adminId;
}