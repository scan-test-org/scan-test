package com.alibaba.apiopenplatform.support.enums;

/**
 * Nacos实例状态枚举
 * @author zxd
 */
public enum NacosStatus {
    RUNNING("运行中"),
    STOPPED("已停止"),
    ERROR("错误"),
    CONNECTING("连接中");

    private final String description;

    NacosStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 