package com.alibaba.apiopenplatform.service.gateway.client;

/**
 * @author zh
 */
public abstract class GatewayClient {

    abstract String getGatewayId();

    abstract boolean tryConnection();

    public void close() {

    }
}
