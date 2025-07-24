package com.alibaba.apiopenplatform.dto.params.developer;

public class UnbindExternalIdentityDto {
    private String providerName;
    private String providerSubject;
    private String portalId;
    // getter/setter
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderSubject() { return providerSubject; }
    public void setProviderSubject(String providerSubject) { this.providerSubject = providerSubject; }
    public String getPortalId() { return portalId; }
    public void setPortalId(String portalId) { this.portalId = portalId; }
} 