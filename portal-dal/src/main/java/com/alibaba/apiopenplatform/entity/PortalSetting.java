package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.support.OidcConfig;
import com.alibaba.apiopenplatform.support.converter.OidcConfigConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_apim_portal_setting",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_portal_provider", columnNames = {"portal_id", "provider"})
    },
        indexes = {
        @Index(name = "idx_portal_id", columnList = "portal_id"),
        @Index(name = "idx_provider", columnList = "provider")
    }
)
@Data
public class PortalSetting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "portal_id", length = 32, nullable = false)
    // 门户唯一标识，例如 openapi、companyA、tenant2024
    private String portalId;

    @Column(name = "provider", length = 32, nullable = false)
    // OIDC provider 名，例如 github、google、aliyun
    private String provider;

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