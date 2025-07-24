package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.aliyun.sdk.service.apig20240327.models.HttpRoute;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class MCPServerResult implements OutputConverter<MCPServerResult, HttpRoute> {

    private String apiId;

    private String routeId;

    private String name;
}
