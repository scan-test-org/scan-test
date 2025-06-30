package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.repository.BaseRepository;

/**
 * @author zh
 */
public class BaseService<D, L> {

    private BaseRepository<D, L> repository;

    public BaseService(BaseRepository<D, L> repository) {
        this.repository = repository;
    }

    protected void checkExist() {

    }


}
