package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Developer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zh
 */
@Data
public class DeveloperResult implements OutputConverter<DeveloperResult, Developer> {

    private String portalId;

    private String developerId;

    private String username;

    private String status;

    private String avatarUrl;

    private LocalDateTime createAt;
}
