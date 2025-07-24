package com.alibaba.apiopenplatform.dto.params.developer;

import com.alibaba.apiopenplatform.dto.converter.InputConverter;
import com.alibaba.apiopenplatform.entity.Developer;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 开发者注册/创建参数DTO
 *
 * @author zxd
 */
@Data
@NoArgsConstructor
public class DeveloperCreateParam implements InputConverter<Developer> {
    @NotBlank(message = "用户名不能为空")
    @Size(max = 64, message = "用户名长度不能超过64个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度应为6-32位")
    private String password;

    @Size(max = 64, message = "portalId长度不能超过64个字符")
    private String portalId;

    @Size(max = 256, message = "头像url长度不能超过256个字符")
    private String avatarUrl;
} 