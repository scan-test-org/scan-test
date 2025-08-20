package com.alibaba.apiopenplatform.dto.params.admin;

import lombok.Data;

/**
 * 修改密码参数
 *
 * @author zxd
 */
@Data
public class ResetPasswordParam {

    private String oldPassword;

    private String newPassword;
} 