package com.alibaba.apiopenplatform.repository;

import com.alibaba.apiopenplatform.entity.ProductPublication;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;

/**
 * 基础数据访问接口，提供通用的数据库操作方法
 *
 * @param <D> 实体类型(Domain/Entity)
 * @param <I> 主键类型(ID)
 * @author zh
 */
@NoRepositoryBean
public interface BaseRepository<D, I> extends JpaRepository<D, I>, JpaSpecificationExecutor<D> {

    /**
     * 根据ID集合批量查询实体列表
     *
     * @param ids
     * @param sort
     * @return
     */
    List<D> findAllByIdIn(@NonNull Collection<I> ids, @NonNull Sort sort);
}

