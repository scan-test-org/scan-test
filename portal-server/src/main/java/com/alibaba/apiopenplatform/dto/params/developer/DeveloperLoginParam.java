package com.alibaba.apiopenplatform.dto.params.developer;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;

/**
 * 开发者登录参数DTO
 *
 * @author zxd
 */
@Data
@NoArgsConstructor
public class DeveloperLoginParam {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    public DeveloperLoginParam(String username, String password) {
        this.username = username;
        this.password = password;
    }
} 