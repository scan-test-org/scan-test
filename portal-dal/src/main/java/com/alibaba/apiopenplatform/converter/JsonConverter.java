package com.alibaba.apiopenplatform.converter;

import cn.hutool.json.JSONUtil;

import javax.persistence.AttributeConverter;

/**
 * @author zh
 */
public abstract class JsonConverter<T> implements AttributeConverter<T, String> {

    private Class<T> type;

    protected JsonConverter() {
    }

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