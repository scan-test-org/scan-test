package com.alibaba.apiopenplatform.dto.params.nacos;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.NacosInstance;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建Nacos实例参数
 *
 * @author zxd
 */
@Data
public class CreateNacosParam implements InputConverter<NacosInstance> {

    @NotBlank(message = "Nacos名称不能为空")
    private String nacosName;

    @NotBlank(message = "服务器地址不能为空")
    private String serverUrl;

    private String namespace;

    private String username;

    private String password;

    private String description;
} 