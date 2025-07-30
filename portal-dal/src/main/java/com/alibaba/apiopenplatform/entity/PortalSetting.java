//package com.alibaba.apiopenplatform.entity;
//
//import com.alibaba.apiopenplatform.support.portal.OidcConfig;
//import com.alibaba.apiopenplatform.converter.OidcConfigListConverter;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//
//import javax.persistence.*;
//import java.util.List;
//
///**
// * @author zh
// */
//@EqualsAndHashCode(callSuper = true)
//@Entity
//@Table(name = "portal_setting")
//@Data
//public class PortalSetting extends BaseEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne
//    @PrimaryKeyJoinColumn
//    private Portal portal;
//
//    @Column(name = "portal_id", length = 32)
//    private String portalId;
//
//    @Column(name = "provider", length = 32)
//    // OIDC provider 名，例如 github、google、aliyun
//    private String provider;
//
//    @Column(name = "builtin_auth_enabled")
//    private Boolean builtinAuthEnabled = true;
//
//    @Column(name = "oidc_auth_enabled")
//    private Boolean oidcAuthEnabled = false;
//
//    @Convert(converter = OidcConfigListConverter.class)
//    @Column(name = "oidc_configs", columnDefinition = "text")
//    private List<OidcConfig> oidcConfigs;
//
//    @Column(name = "auto_approve_developers")
//    private Boolean autoApproveDevelopers = false;
//
//    @Column(name = "auto_approve_subscriptions")
//    private Boolean autoApproveSubscriptions = false;
//
//    @Column(name = "frontend_redirect_url", length = 256)
//    private String frontendRedirectUrl;
//
//    public void setPortal(Portal portal) {
//        this.portal = portal;
//        if (portal != null) {
//            this.portalId = portal.getPortalId();
//        }
//    }
//}