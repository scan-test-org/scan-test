package com.alibaba.apiopenplatform.dto.params;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Portal;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


/**
 * @author zh
 */
@Data
public class PortalParam implements InputConverter<Portal> {

    @NotBlank(message = "门户名称不能为空")
    @Size(max = 50, message = "门户名称长度不能超过50个字符")
    private String name;

    private String description;

    @NotBlank(message = "管理员ID不能为空")
    private String adminId;
}
