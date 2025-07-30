package com.alibaba.apiopenplatform.converter;

import com.alibaba.apiopenplatform.support.product.NacosRefConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * NacosRefConfig JPA 转换器
 * @author zxd
 */
@Converter
public class NacosRefConfigConverter implements AttributeConverter<NacosRefConfig, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(NacosRefConfig nacosRefConfig) {
        if (nacosRefConfig == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(nacosRefConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert NacosRefConfig to JSON", e);
        }
    }

    @Override
    public NacosRefConfig convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, NacosRefConfig.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to NacosRefConfig", e);
        }
    }
} 