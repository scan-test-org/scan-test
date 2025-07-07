package com.alibaba.apiopenplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author zh
 */
@SpringBootApplication
@EnableJpaAuditing
//@ComponentScan(basePackages = "com.alibaba.apiopenplatform")
public class PortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}
