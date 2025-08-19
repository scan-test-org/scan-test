package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.nacos.CreateNacosParam;
import com.alibaba.apiopenplatform.dto.params.nacos.UpdateNacosParam;
import com.alibaba.apiopenplatform.dto.result.NacosMCPServerResult;
import com.alibaba.apiopenplatform.dto.result.NacosResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.support.product.NacosRefConfig;
import org.springframework.data.domain.Pageable;

/**
 * Nacos服务接口，定义Nacos实例管理和MCP服务器配置相关操作
 *
 * @author zxd
 */
public interface NacosService {

    /**
     * 获取Nacos实例列表
     *
     * @param pageable 分页参数
     * @return 分页的Nacos实例列表
     */
    PageResult<NacosResult> listNacosInstances(Pageable pageable);

    /**
     * 获取Nacos实例详情
     *
     * @param nacosId Nacos实例唯一标识
     * @return Nacos实例详细信息
     */
    NacosResult getNacosInstance(String nacosId);

    /**
     * 导入Nacos实例
     *
     * @param param Nacos实例创建参数
     */
    void createNacosInstance(CreateNacosParam param);

    /**
     * 更新Nacos实例
     *
     * @param nacosId Nacos实例唯一标识
     * @param param Nacos实例更新参数
     */
    void updateNacosInstance(String nacosId, UpdateNacosParam param);

    /**
     * 删除Nacos实例
     *
     * @param nacosId Nacos实例唯一标识
     */
    void deleteNacosInstance(String nacosId);

    /**
     * 获取MCP Server列表
     *
     * @param nacosId Nacos实例唯一标识
     * @param pageable 分页参数
     * @return 分页的MCP Server列表
     * @throws Exception 获取MCP Server列表时可能抛出的异常
     */
    PageResult<NacosMCPServerResult> fetchMcpServers(String nacosId, Pageable pageable) throws Exception;

    /**
     * 获取MCP Server配置
     *
     * @param nacosId Nacos实例唯一标识
     * @param nacosRefConfig Nacos引用配置
     * @return MCP Server配置信息
     */
    String fetchMcpConfig(String nacosId, NacosRefConfig nacosRefConfig);
}