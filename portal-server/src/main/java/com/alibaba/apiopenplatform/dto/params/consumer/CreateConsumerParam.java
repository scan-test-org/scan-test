package com.alibaba.apiopenplatform.dto.params.consumer;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Consumer;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author zh
 */
@Data
public class CreateConsumerParam implements InputConverter<Consumer> {

    @NotBlank(message = "门户ID不能为空")
    private String portalId;

    @NotBlank(message = "Consumer名称不能为空")
    @Size(max = 50, message = "Consumer名称长度不能超过50个字符")
    private String name;

    @Size(max = 256, message = "Consumer描述长度不能超过256个字符")
    private String description;
}
