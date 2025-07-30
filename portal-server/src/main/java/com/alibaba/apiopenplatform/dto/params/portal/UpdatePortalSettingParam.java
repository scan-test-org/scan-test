//package com.alibaba.apiopenplatform.dto.params.portal;
//
//import com.alibaba.apiopenplatform.dto.converter.InputConverter;
//import com.alibaba.apiopenplatform.entity.PortalSetting;
//import com.alibaba.apiopenplatform.support.portal.OidcConfig;
//import lombok.Data;
//
//import javax.validation.Valid;
//import java.util.List;
//
///**
// * @author zh
// */
//@Data
//public class UpdatePortalSettingParam implements InputConverter<PortalSetting> {
//
//    private Boolean builtinAuthEnabled;
//
//    private Boolean oidcAuthEnabled;
//
//    private String frontendRedirectUrl;
//
//    private Boolean autoApproveDevelopers;
//
//    private Boolean autoApproveSubscriptions;
//
//    private List<OidcConfig> oidcConfigs;
//}
