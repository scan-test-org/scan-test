package com.alibaba.apiopenplatform.entity;

import com.alibaba.apiopenplatform.support.enums.DomainType;
import com.alibaba.apiopenplatform.support.enums.ProtocolType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

/**
 * @author zh
 */
@Entity
@Table(name = "t_apim_portal_domain",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"domain"}, name = "uk_domain")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class PortalDomain extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "portal_id", referencedColumnName = "portal_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Portal portal;

    @Column(name = "domain", length = 128, nullable = false)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32, nullable = false)
    private DomainType type = DomainType.DEFAULT;

    @Column(name = "protocol", length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    private ProtocolType protocol = ProtocolType.HTTP;
}