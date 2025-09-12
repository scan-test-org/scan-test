package com.alibaba.apiopenplatform.support.portal;

import lombok.Data;

import java.util.Map;

/**
 * @author zh
 */
@Data
public class IdentityMapping {

    /**
     * 映射DeveloperId
     */
    private String userIdField;

    /**
     * 映射Developer Name
     */
    private String userNameField;

    /**
     * 自定义字段映射（预留扩展）
     */
    private Map<String, String> customFields;
}
