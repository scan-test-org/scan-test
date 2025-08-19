package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Administrator;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zxd
 */
@Data
public class AdminResult implements OutputConverter<AdminResult, Administrator> {

    private String adminId;

    private String username;

    private LocalDateTime createAt;

    private LocalDateTime updatedAt;
}