package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import com.alibaba.apiopenplatform.entity.Consumer;
import com.alibaba.apiopenplatform.support.consumer.ConsumerCredentials;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author zh
 */
@Data
public class ConsumerResult implements OutputConverter<ConsumerResult, Consumer> {

    private String consumerId;

    private String name;

    private String description;

    private String portalId;

    private String developerId;

    private ConsumerCredentials consumerCredentials;

    private LocalDateTime gmtCreate;
}
