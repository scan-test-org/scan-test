package com.alibaba.apiopenplatform.dto.params.admin;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;

/**
 * 管理员登录参数DTO
 *
 * @author zxd
 */
@Data
@NoArgsConstructor
public class AdminLoginDto {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "portalId不能为空")
    private String portalId;
} 