package com.alibaba.apiopenplatform.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

/**
 * @author zh
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_apim_portal_ui")
@Data
public class PortalUi extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(name = "portal_id", length = 32)
//    private String portalId;

    @OneToOne
    @PrimaryKeyJoinColumn
    private Portal portal;

    @Column(name = "logo", length = 256)
    private String logo;

    @Column(name = "icon", length = 256)
    private String icon;
}