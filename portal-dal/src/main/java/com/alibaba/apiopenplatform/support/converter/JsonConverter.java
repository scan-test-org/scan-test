package com.alibaba.apiopenplatform.support.converter;

import cn.hutool.json.JSONUtil;

import javax.persistence.AttributeConverter;

/**
 * @author zh
 */
public abstract class JsonConverter<T> implements AttributeConverter<T, String> {

    private final Class<T> type;

    protected JsonConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        return attribute == null ? null : JSONUtil.toJsonStr(attribute);
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        return dbData == null ? null : JSONUtil.toBean(dbData, type);
    }
}