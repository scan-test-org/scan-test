package com.alibaba.apiopenplatform.entity;

import javax.persistence.*;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 管理员实体类，映射管理员账号信息
 *
 * @author zxd
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "administrator", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"adminId"}),
        @UniqueConstraint(columnNames = {"username"})
})
public class Administrator extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String adminId;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 64)
    private String portalId;

} 