package com.alibaba.apiopenplatform.dto.params.developer;

import com.alibaba.apiopenplatform.support.enums.DeveloperStatus;
import lombok.Data;

/**
 * @author zh
 */
@Data
public class QueryDeveloperParam {

    private String portalId;

    private String username;

    private DeveloperStatus status;
}
