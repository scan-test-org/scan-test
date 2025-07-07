package com.alibaba.apiopenplatform.dto.params.admin;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Administrator;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 管理员注册/创建参数DTO
 *
 * @author zxd
 */
@Data
@NoArgsConstructor
public class AdminCreateDto implements InputConverter<Administrator> {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过64个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度应为6-32位")
    private String password;

    @Size(max = 64, message = "portalId长度不能超过64个字符")
    private String portalId;
} 