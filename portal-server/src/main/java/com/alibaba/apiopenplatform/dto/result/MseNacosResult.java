package com.alibaba.apiopenplatform.dto.result;

import com.aliyun.mse20190531.models.ListClustersResponseBody.ListClustersResponseBodyData;

import lombok.Data;

@Data
public class MseNacosResult {
    private String instanceId;

    private String name;

    private String serverIntranetEndpoint;

    private String serverInternetEndpoint;

    private String version;

    public static MseNacosResult fromListClustersResponseBodyData(ListClustersResponseBodyData cluster) {
        MseNacosResult nacosResult = new MseNacosResult();
        nacosResult.setName(cluster.getClusterAliasName());
        nacosResult.setVersion(cluster.getVersionCode());
        nacosResult.setInstanceId(cluster.getInstanceId());
        nacosResult.setServerIntranetEndpoint(cluster.getIntranetDomain());
        nacosResult.setServerInternetEndpoint(cluster.getInternetDomain());
        return nacosResult;
    }
}
