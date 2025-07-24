package com.alibaba.apiopenplatform.dto.converter;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;

/**
 * @author zh
 */
public interface OutputConverter<Target extends OutputConverter<Target, Source>, Source> {

    /**
     * 以Source更新Target
     *
     * @param source
     * @return
     */
    @SuppressWarnings("unchecked")
    default Target convertFrom(Source source) {
        BeanUtil.copyProperties(source, this, configOptions());
        return (Target) this;
    }

    default CopyOptions configOptions() {
        return CopyOptions.create().ignoreNullValue().ignoreError();
    }
}

