package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.ProductPublication;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zh
 */
@Data
public class ProductPublicationResult implements OutputConverter<ProductPublicationResult, ProductPublication> {

    private String portalId;

    private String portalName;

    private Boolean autoApproveSubscription = false;

    private LocalDateTime createAt;
}
