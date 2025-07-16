package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Product;
import lombok.Data;

import java.util.List;

/**
 * @author zh
 */
@Data
public class ProductResult implements OutputConverter<ProductResult, Product> {

    private String productId;

    private String name;

    private String description;

    private String ownerId;

    private String adminId;

    private String status;

    private String enableConsumerAuth;

    private String type;

    private String document;

    private String icon;

    private String category;

    private ProductSettingConfig productSettingConfig;

    private List<APIRefResult> apiRefs;

    @Override
    public ProductResult convertFrom(Product source) {
        OutputConverter.super.convertFrom(source);
        productSettingConfig = new ProductSettingConfig().convertFrom(source.getProductSetting());
        
        // 转换API引用列表
        if (source.getApiRefs() != null) {
            apiRefs = source.getApiRefs().stream()
                    .map(apiRef -> new APIRefResult().convertFrom(apiRef))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return this;
    }
}
