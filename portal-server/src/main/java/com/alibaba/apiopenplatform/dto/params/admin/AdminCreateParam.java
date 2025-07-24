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
public class AdminCreateParam {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
} 