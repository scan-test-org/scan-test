package com.alibaba.apiopenplatform.dto.converter;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.lang.NonNull;

/**
 * @author zh
 */
public interface OutputConverter<Target extends OutputConverter<Target, Source>, Source> {

    /**
     * 以Source的属性填充Target
     *
     * @param source
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    @NonNull
    default <T extends Target> T convertFrom(Source source) {

        BeanUtil.copyProperties(source, this);
        return (T) this;
    }
}

