package com.alibaba.apiopenplatform.dto.params.nacos;

import lombok.Data;

import javax.validation.constraints.NotBlank;

import com.aliyun.teaopenapi.models.Config;

/**
 * 查询Nacos集群参数
 *
 * @author zxd
 */
@Data
public class QueryNacosParam {

    @NotBlank(message = "地域不能为空")
    private String regionId;

    @NotBlank(message = "accessKey不能为空")
    private String accessKey;

    @NotBlank(message = "secretKey不能为空")
    private String secretKey;

    public Config toClientConfig() {
        Config config = new Config()
                .setAccessKeyId(this.accessKey)
                .setAccessKeySecret(this.secretKey)
                .setRegionId(this.regionId);
        return config;
    }
}
