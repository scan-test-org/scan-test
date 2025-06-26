package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.support.OidcConfig;
import com.alibaba.apiopenplatform.support.converter.OidcConfigConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_apim_portal_setting",
        indexes = {
                @Index(name = "idx_portal_id", columnList = "portal_id")
        })
@Data
public class PortalSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "portal_id", length = 32, nullable = false)
    private String portalId;

    @Column(name = "builtin_auth_enabled")
    private Boolean builtinAuthEnabled = true;

    @Column(name = "oidc_auth_enabled")
    private Boolean oidcAuthEnabled = false;

    @Convert(converter = OidcConfigConverter.class)
    @Column(name = "oidc_config", columnDefinition = "text")
    private OidcConfig oidcConfig;

    @Column(name = "auto_approve_developers")
    private Boolean autoApproveDevelopers = false;

    @Column(name = "auto_approve_subscriptions")
    private Boolean autoApproveSubscriptions = false;
}