package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.nacos.CreateNacosParam;
import com.alibaba.apiopenplatform.dto.params.nacos.UpdateNacosParam;
import com.alibaba.apiopenplatform.dto.result.NacosMCPServerResult;
import com.alibaba.apiopenplatform.dto.result.NacosResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.support.product.NacosRefConfig;
import org.springframework.data.domain.Pageable;

/**
 * @author zxd
 */
public interface NacosService {

    /**
     * 获取Nacos实例列表
     *
     * @param pageable
     * @return
     */
    PageResult<NacosResult> listNacosInstances(Pageable pageable);

    /**
     * 导入Nacos实例
     *
     * @param param
     */
    void createNacosInstance(CreateNacosParam param);

    /**
     * 更新Nacos实例
     *
     * @param nacosId
     * @param param
     */
    void updateNacosInstance(String nacosId, UpdateNacosParam param);

    /**
     * 删除Nacos实例
     *
     * @param nacosId
     */
    void deleteNacosInstance(String nacosId);


    /**
     * 获取Nacos实例详情
     *
     * @param nacosId
     * @return
     */
    NacosResult getNacosInstance(String nacosId);

    /**
     * 获取MCP Server列表
     *
     * @param nacosId
     * @param pageable
     * @return
     * @throws Exception
     */
    PageResult<NacosMCPServerResult> fetchMcpServers(String nacosId, Pageable pageable) throws Exception;


    /**
     * 获取MCP Server配置
     *
     * @param nacosId
     * @param nacosRefConfig
     * @return
     */
    String fetchMcpConfig(String nacosId, NacosRefConfig nacosRefConfig);
}