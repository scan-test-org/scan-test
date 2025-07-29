package com.alibaba.apiopenplatform.dto.params.developer;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

/**
 * 开发者更新个人信息请求参数
 *
 * @author zxd
 */
@Data
public class UpdateDeveloperProfileParam {
    
    @Size(max = 64, message = "用户名长度不能超过64个字符")
    private String username;
    
    @Email(message = "邮箱格式不正确")
    @Size(max = 128, message = "邮箱长度不能超过128个字符")
    private String email;
    
    @Size(max = 256, message = "头像URL长度不能超过256个字符")
    private String avatarUrl;
} 