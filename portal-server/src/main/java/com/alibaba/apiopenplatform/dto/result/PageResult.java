package com.alibaba.apiopenplatform.dto.result;

import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.map.MapUtil;
import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author zh
 */
@Data
public class PageResult<T> implements OutputConverter<PageResult<T>, Page<T>> {

    private List<T> content;

    private int pageNumber;

    private int pageSize;

    private long totalElements;

    private int totalPages;

    @Override
    public PageResult<T> convertFrom(Page<T> source) {
        OutputConverter.super.convertFrom(source);

        setPageSize(source.getSize());
        setPageNumber(source.getNumber());
        setTotalElements(source.getTotalElements());
        setTotalPages(source.getTotalPages());

        return this;
    }
}
