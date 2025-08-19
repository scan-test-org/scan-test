package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.NacosInstance;
import lombok.Data;

/**
 * @author zxd
 */
@Data
public class NacosResult implements OutputConverter<NacosResult, NacosInstance> {

    private String nacosId;

    private String nacosName;

    private String serverUrl;

    private String namespace;

    private String username;

    private String description;

    private String adminId;
} 