package com.alibaba.apiopenplatform.core.utils;

import cn.hutool.core.lang.ObjectId;

/**
 * ID生成器
 * <p>
 * 格式为: prefix + 24位字符串
 * <p>
 * 支持的ID类型:
 * - 门户ID: portal-xxxxxx
 * - API产品ID: api-xxxxxx
 * - 开发者ID: dev-xxxxxx
 * - 管理员ID: admin-xxxxxx
 * <p>
 * 注意:
 * - Consumer ID由网关同步，不在此生成
 * - API ID由网关同步，不在此生成
 *
 * @author zh
 */
public class IdGenerator {

    private static final String PORTAL_PREFIX = "portal-";
    private static final String API_PRODUCT_PREFIX = "product-";
    private static final String DEVELOPER_PREFIX = "dev-";
    private static final String ADMINISTRATOR_PREFIX = "admin-";
    private static final String NACOS_PREFIX = "nacos-";

    public static String genPortalId() {
        return PORTAL_PREFIX + ObjectId.next();
    }

    public static String genApiProductId() {
        return API_PRODUCT_PREFIX + ObjectId.next();
    }

    public static String genDeveloperId() {
        return DEVELOPER_PREFIX + ObjectId.next();
    }

    public static String genAdministratorId() {
        return ADMINISTRATOR_PREFIX + ObjectId.next();
    }

    public static String genNacosId() {
        return NACOS_PREFIX + ObjectId.next();
    }

    public static String genIdWithPrefix(String prefix) {
        return prefix + ObjectId.next();
    }
}
