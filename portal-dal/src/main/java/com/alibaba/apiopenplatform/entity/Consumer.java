package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.support.enums.ConsumerStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

/**
 * @author zh
 */
@Entity
@Table(name = "consumer",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"consumer_id"}, name = "uk_consumer_id"),
                @UniqueConstraint(columnNames = {"name", "portal_id", "developer_id"},
                        name = "uk_name_portal_developer")
        })
@Data
@EqualsAndHashCode(callSuper = true)
public class Consumer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_id", length = 64, nullable = false)
    private String consumerId;

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "description", length = 256)
    private String description;

//    @Enumerated(EnumType.STRING)
//    @Column(name = "status", length = 32, nullable = false)
//    private ConsumerStatus status;

    @Column(name = "portal_id", length = 64, nullable = false)
    private String portalId;

    @Column(name = "developer_id", length = 64, nullable = false)
    private String developerId;
}
