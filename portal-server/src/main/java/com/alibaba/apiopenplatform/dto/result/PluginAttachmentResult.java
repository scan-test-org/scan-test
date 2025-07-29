package com.alibaba.apiopenplatform.dto.result;

import com.aliyun.sdk.service.apig20240327.models.PluginClassInfo;
import lombok.Builder;
import lombok.Data;

/**
 * @author zh
 */
@Data
@Builder
public class PluginAttachmentResult {

    private PluginClassInfo pluginClassInfo;

    private String pluginConfig;
}
