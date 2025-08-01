package com.alibaba.apiopenplatform.dto.converter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 一个通用的转换器，用于将DTO转换为领域对象
 * <p>
 * 例如：CreatePortalRequest -> Portal
 *
 * @param <D> 目标领域对象类型
 * @author zh
 */
public interface InputConverter<D> {

    /**
     * 将当前对象转换为目标领域对象
     *
     * @return
     */
    default D convertTo() {
        ParameterizedType currentType = parameterizedType();

        @SuppressWarnings("unchecked")
        Class<D> clazz = (Class<D>) currentType.getActualTypeArguments()[0];
        return BeanUtil.copyProperties(this, clazz);
    }

    /**
     * 获取当前接口的参数化类型
     *
     * @return
     */
    default ParameterizedType parameterizedType() {
        Type[] interfaces = this.getClass().getGenericInterfaces();

        // 遍历查找InputConvertor接口的参数化类型
        for (Type type : interfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (parameterizedType.getRawType().equals(InputConverter.class)) {
                    return parameterizedType;
                }
            }
        }

        // 如果当前接口没找到，递归查找父类
        Class<?> superclass = this.getClass().getSuperclass();
        if (superclass == null || superclass.equals(Object.class)) {
            return null;
        }

        Type superType = superclass.getGenericSuperclass();
        if (superType instanceof ParameterizedType) {
            return (ParameterizedType) superType;
        }

        return null;
    }

    /**
     * 将当前对象更新到目标领域对象中
     *
     * @param domain
     */
    default void update(D domain) {
        BeanUtil.copyProperties(this, domain, CopyOptions.create().setIgnoreNullValue(true));
    }
}
