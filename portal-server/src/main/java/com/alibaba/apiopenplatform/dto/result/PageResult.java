package com.alibaba.apiopenplatform.dto.result;

import com.alibaba.apiopenplatform.dto.converter.OutputConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements OutputConverter<PageResult<T>, Page<T>> {

    private List<T> content;

    private int number;

    private int size;

    private long totalElements;

    @Override
    public PageResult<T> convertFrom(Page<T> source) {
        OutputConverter.super.convertFrom(source);

        setSize(source.getSize());
        setNumber(source.getNumber());
        setTotalElements(source.getTotalElements());

        return this;
    }

    public <S> PageResult<T> mapFrom(PageResult<S> source, Function<S, T> mapper) {
        setContent(source.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList()));
        setSize(source.getSize());
        setNumber(source.getNumber());
        setTotalElements(source.getTotalElements());
        return this;
    }

    public static <T> PageResult<T> empty(int pageNumber, int pageSize) {
        return PageResult.<T>builder()
                .content(new ArrayList<>())
                .number(pageNumber)
                .size(pageSize)
                .totalElements(0)
                .build();
    }

    public static <T> PageResult<T> of(List<T> content, int pageNumber, int pageSize, long total) {
        return PageResult.<T>builder()
                .content(content)
                .number(pageNumber)
                .size(pageSize)
                .totalElements(total)
                .build();
    }
}
