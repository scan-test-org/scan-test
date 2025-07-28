package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.NacosInstance;
import com.alibaba.apiopenplatform.support.enums.NacosStatus;
import lombok.Data;

/**
 * Nacos实例结果
 * @author zxd
 */
@Data
public class NacosResult implements OutputConverter<NacosResult, NacosInstance> {

    private String nacosId;

    private String nacosName;

    private String serverUrl;

    private String namespace;

    private String username;

    private NacosStatus status;

    private String description;

    private String adminId;

    @Override
    public NacosResult convertFrom(NacosInstance nacosInstance) {
        this.nacosId = nacosInstance.getNacosId();
        this.nacosName = nacosInstance.getNacosName();
        this.serverUrl = nacosInstance.getServerUrl();
        this.namespace = nacosInstance.getNamespace();
        this.username = nacosInstance.getUsername();
        this.status = nacosInstance.getStatus();
        this.description = nacosInstance.getDescription();
        this.adminId = nacosInstance.getAdminId();
        return this;
    }
} 