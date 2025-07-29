package com.alibaba.apiopenplatform.support.converter;

import cn.hutool.json.JSONUtil;
import com.alibaba.apiopenplatform.support.product.RouteConfig;

import javax.persistence.Converter;
import java.util.List;

/**
 * @author zh
 */
@Converter(autoApply = true)
public class RouteConfigListConverter extends JsonConverter<List<RouteConfig>> {

    @Override
    public String convertToDatabaseColumn(List<RouteConfig> attribute) {
        return attribute == null ? null : JSONUtil.toJsonStr(attribute);
    }

    @Override
    public List<RouteConfig> convertToEntityAttribute(String dbData) {
        return dbData == null ? null : JSONUtil.toList(dbData, RouteConfig.class);
    }
}
