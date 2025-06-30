package com.alibaba.apiopenplatform.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_apim_portal_ui",
        indexes = {
                @Index(name = "idx_portal_id", columnList = "portal_id")
        })
@Data
public class PortalUi extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "portal_id", length = 32)
    private String portalId;

    @Column(name = "logo", length = 256)
    private String logo;

    @Column(name = "icon", length = 256)
    private String icon;
}