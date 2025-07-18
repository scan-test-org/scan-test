package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.ProductSetting;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class ProductSettingConfig implements OutputConverter<ProductSettingConfig, ProductSetting> {

    private String apiList;

    private String authType;

    private String rateLimit;
}
