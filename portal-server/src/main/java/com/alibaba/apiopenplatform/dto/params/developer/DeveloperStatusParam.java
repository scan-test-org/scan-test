package com.alibaba.apiopenplatform.dto.params.developer;

import com.alibaba.apiopenplatform.support.enums.DeveloperStatus;
import lombok.Data;

/**
 * 开发者状态参数
 *
 * @author zxd
 */
@Data
public class DeveloperStatusParam {

    private String portalId;

    private DeveloperStatus status;
} 