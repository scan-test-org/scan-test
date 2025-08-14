package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.converter.PortalSettingConfigConverter;
import com.alibaba.apiopenplatform.converter.PortalUiConfigConverter;
import com.alibaba.apiopenplatform.support.portal.PortalSettingConfig;
import com.alibaba.apiopenplatform.support.portal.PortalUiConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "portal",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"portal_id"}, name = "uk_portal_id"),
                @UniqueConstraint(columnNames = {"name", "admin_id"}, name = "uk_name_admin_id")
        })
@Data
public class Portal extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "portal_id", length =64, nullable = false)
    private String portalId;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "description", length = 256)
    private String description;

    @Column(name = "admin_id", length = 64)
    private String adminId;

    @Column(name = "portal_setting_config", columnDefinition = "text")
    @Convert(converter = PortalSettingConfigConverter.class)
    private PortalSettingConfig portalSettingConfig;

    @Column(name = "portal_ui_config", columnDefinition = "text")
    @Convert(converter = PortalUiConfigConverter.class)
    private PortalUiConfig portalUiConfig;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "portal_id", referencedColumnName = "portal_id")
    private List<PortalDomain> portalDomains = new ArrayList<>();
}