package com.alibaba.apiopenplatform.dto.params.portal;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Portal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


/**
 * @author zh
 */
@Schema(description = "创建门户参数")
@Data
public class CreatePortalParam implements InputConverter<Portal> {

    @NotBlank(message = "门户名称不能为空")
    @Size(max = 50, message = "门户名称长度不能超过50个字符")
    private String name;

    @NotBlank(message = "门户标题不能为空")
    @Size(max = 50, message = "门户标题长度不能超过50个字符")
    private String title;

    private String description;
}
