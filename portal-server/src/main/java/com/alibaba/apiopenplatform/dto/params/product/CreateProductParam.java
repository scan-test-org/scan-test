package com.alibaba.apiopenplatform.dto.params.product;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Product;
import com.alibaba.apiopenplatform.support.enums.ProductType;
import com.alibaba.apiopenplatform.support.enums.SourceType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


/**
 * @author zh
 */
@Data
public class CreateProductParam implements InputConverter<Product> {

    @NotBlank(message = "API产品名称不能为空")
    @Size(max = 50, message = "API产品名称长度不能超过50个字符")
    private String name;

    private String description;

    @NotNull(message = "API产品类型不能为空")
    private ProductType type;

    private String document;

    private String icon;

    private String category;

    // 当type为MCP_SERVER时，用于指定数据源类型
    private SourceType sourceType;

    // 当type为MCP_SERVER且sourceType为GATEWAY时，用于指定Gateway ID
    private String gatewayId;

    // 当type为MCP_SERVER且sourceType为NACOS时，用于指定Nacos ID
    private String nacosId;

    // 当type为MCP_SERVER时，用于指定MCP Server名称
    private String mcpServerName;
}
