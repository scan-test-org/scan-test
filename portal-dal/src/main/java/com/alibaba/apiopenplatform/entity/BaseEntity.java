package com.alibaba.apiopenplatform.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author zh
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public class BaseEntity implements Serializable {

    @CreatedDate
    @Column(name = "gmt_create", updatable = false, columnDefinition = "datetime(3)")
    private LocalDateTime gmtCreate;

    @LastModifiedDate
    @Column(name = "gmt_modified", columnDefinition = "datetime(3)")
    private LocalDateTime gmtModified;
}