package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.portal.OidcConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

/**
 * OIDC配置数组与JSON字符串互转的JPA属性转换器
 * 用于PortalSetting.oidcConfigs字段
 *
 * @author zxd
 */
@Converter
public class OidcConfigListConverter implements AttributeConverter<List<OidcConfig>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<OidcConfig> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("序列化oidcConfigs失败", e);
        }
    }

    @Override
    public List<OidcConfig> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, new TypeReference<List<OidcConfig>>(){});
        } catch (Exception e) {
            throw new RuntimeException("反序列化oidcConfigs失败", e);
        }
    }
} 