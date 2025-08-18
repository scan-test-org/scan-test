package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Administrator;
import lombok.Data;

/**
 * 管理员信息返回DTO
 * @author zxd
 */
@Data
public class AdminResult implements OutputConverter<AdminResult, Administrator> {
    private String adminId;
    private String username;
    private String createAt;
    private String updatedAt;
}