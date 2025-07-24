package com.alibaba.apiopenplatform.support.converter;

import cn.hutool.json.JSONUtil;

import javax.persistence.Converter;
import java.util.List;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class StringListConverter extends JsonConverter<List<String>> {

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return attribute == null ? null : JSONUtil.toJsonStr(attribute);
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return dbData == null ? null : JSONUtil.toList(dbData, String.class);
    }
}
