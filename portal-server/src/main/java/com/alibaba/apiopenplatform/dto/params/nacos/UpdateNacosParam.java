package com.alibaba.apiopenplatform.dto.params.nacos;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.NacosInstance;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 更新Nacos实例参数
 *
 * @author zxd
 */
@Data
public class UpdateNacosParam implements InputConverter<NacosInstance> {

    @NotBlank(message = "Nacos名称不能为空")
    @Size(max = 64, message = "Nacos名称长度不能超过64个字符")
    private String nacosName;

    @NotBlank(message = "服务器地址不能为空")
    @Size(max = 256, message = "服务器地址长度不能超过256个字符")
    private String serverUrl;

    @Size(max = 64, message = "命名空间长度不能超过64个字符")
    private String namespace;

    @Size(max = 64, message = "用户名长度不能超过64个字符")
    private String username;

    @Size(max = 128, message = "密码长度不能超过128个字符")
    private String password;

    @Size(max = 128, message = "AccessKey 长度不能超过128个字符")
    private String accessKey;

    @Size(max = 256, message = "SecretKey 长度不能超过256个字符")
    private String secretKey;

    @Size(max = 512, message = "描述长度不能超过512个字符")
    private String description;
    }
