package com.alibaba.apiopenplatform.service;

import com.alibaba.apiopenplatform.dto.params.nacos.CreateNacosParam;
import com.alibaba.apiopenplatform.dto.params.nacos.UpdateNacosParam;
import com.alibaba.apiopenplatform.dto.result.NacosMCPServerResult;
import com.alibaba.apiopenplatform.dto.result.NacosResult;
import com.alibaba.apiopenplatform.dto.result.PageResult;
import com.alibaba.apiopenplatform.entity.NacosInstance;
import com.alibaba.apiopenplatform.dto.params.mcp.McpMarketCardParam;
import com.alibaba.apiopenplatform.dto.params.mcp.McpMarketDetailParam;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Nacos服务接口 - 统一管理Nacos实例和MCP市场功能
 * @author zxd
 */
public interface NacosService {



    /**
     * 获取指定Nacos实例中的MCP Server详情
     */
    McpMarketDetailParam getMcpServerDetail(String nacosId, String mcpName, String namespaceId, String version);

    /**
     * 获取Nacos实例列表
     */
    PageResult<NacosResult> listNacosInstances(Pageable pageable);

    /**
     * 创建Nacos实例
     */
    void createNacosInstance(CreateNacosParam param);

    /**
     * 更新Nacos实例
     */
    void updateNacosInstance(String nacosId, UpdateNacosParam param);

    /**
     * 删除Nacos实例
     */
    void deleteNacosInstance(String nacosId);

    /**
     * 获取Nacos实例详情
     */
    NacosResult getNacosInstance(String nacosId);

    /**
     * 获取指定Nacos实例中的MCP Server列表
     */
    PageResult<NacosMCPServerResult> fetchMcpServers(String nacosId, Pageable pageable) throws Exception;

    /**
     * 获取Nacos MCP Server配置
     */
    String fetchMcpConfig(String nacosId, Object conf) throws Exception;

    /**
     * 查找Nacos实例
     */
    NacosInstance findNacosInstance(String nacosId);


}