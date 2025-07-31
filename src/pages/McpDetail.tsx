import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../lib/api';
import { Layout } from '../components/Layout';
import { Card, Typography, Tag, Space, Badge, Descriptions, Spin, Alert, Collapse, Button, message, Tabs } from 'antd';
import MonacoEditor from 'react-monaco-editor';
import { ProductType, ProductStatus, ProductCategory } from '../types';
import type { Product, McpServerConfig, McpServerProduct } from '../types';
import { processProductSpecs } from '../lib/utils';

const { Title, Paragraph } = Typography;

// 网关类型映射
const GatewayTypeMap: Record<string, string> = {
  API: '云原生API网关',
  AI: 'AI网关',
  Higress: 'Higress',
};

function McpDetail() {
  const { mcpName } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [data, setData] = useState<Product | null>(null);
  const [mcpConfig, setMcpConfig] = useState<McpServerConfig | null>(null);
  const [httpJson, setHttpJson] = useState('');
  const [sseJson, setSseJson] = useState('');

  console.log('McpDetail 组件渲染，mcpName:', mcpName);

  useEffect(() => {
    console.log('useEffect 触发，mcpName:', mcpName);
    if (!mcpName) {
      console.log('mcpName 为空，返回');
      return;
    }
    setLoading(true);
    setError('');
    console.log('开始请求 API:', `/products/${mcpName}`);
    api.get(`/products/${mcpName}`)
      .then((response: any) => {
        console.log('API 响应:', response);
        if (response.code === "SUCCESS" && response.data) {
          console.log('设置数据:', response.data);
          
          // 处理 mcpSpec 和 apiSpec 中的换行符转义
          const processedData = processProductSpecs(response.data);
          
          setData(processedData);
          
          // 解析MCP配置
          if (processedData.type === ProductType.MCP_SERVER && (processedData as McpServerProduct).mcpSpec) {
            try {
              // 先处理转义字符，将 \" 替换为 "
              // const cleanedMcpSpec = (processedData as McpServerProduct).mcpSpec.replace(/\\"/g, '"');
              const config = processedData.mcpSpec as McpServerConfig;
              setMcpConfig(config);
              
              // 生成连接配置
              if (config.domains && config.domains.length > 0) {
                const domain = config.domains[0];
                const baseUrl = `${domain.protocol}://${domain.domain}`;
                const httpConfig = `{
  "mcpServers": {
    "${mcpName}": {
      "url": "${baseUrl}/mcp-servers/${mcpName}"
    }
  }
}`;
                const sseConfig = `{
  "mcpServers": {
    "${mcpName}": {
      "url": "${baseUrl}/mcp-servers/${mcpName}/sse"
    }
  }
}`;
                setHttpJson(httpConfig);
                setSseJson(sseConfig);
              }
            } catch (error) {
              console.warn('解析MCP配置失败:', error);
            }
          }
        } else {
          console.log('API 响应失败:', response);
          setError(response.message || '数据加载失败');
        }
      })
      .catch((error) => {
        console.error('API请求失败:', error);
        setError('加载失败，请稍后重试');
      })
      .finally(() => {
        console.log('请求完成，设置 loading 为 false');
        setLoading(false);
      });
  }, [mcpName]);

  const handleCopy = async (text: string) => {
    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
      } else {
        // 非安全上下文降级处理
        const textarea = document.createElement('textarea');
        textarea.value = text;
        textarea.style.position = 'fixed';
        document.body.appendChild(textarea);
        textarea.focus();
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
      }
      message.success('已复制到剪贴板');
    } catch (e) {
      message.error('复制失败，请手动复制');
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex justify-center items-center min-h-[300px]">
          <Spin size="large" />
        </div>
      </Layout>
    );
  }
  if (error) {
    return (
      <Layout>
        <Alert message={error} type="error" showIcon className="my-8" />
      </Layout>
    );
  }
  if (!data) {
    return (
      <Layout>
        <Alert message="未找到相关数据" type="warning" showIcon className="my-8" />
      </Layout>
    );
  }

  const { name, description, status, category, productId, enableConsumerAuth } = data;

  const getStatusText = (status: ProductStatus) => {
    switch (status) {
      case ProductStatus.ENABLE:
        return '已启用'
      case ProductStatus.DISABLE:
        return '未启用'
      default:
        return status
    }
  };

  const getCategoryText = (category: ProductCategory) => {
    switch (category) {
      case ProductCategory.OFFICIAL:
        return '官方'
      case ProductCategory.COMMUNITY:
        return '社区'
      case ProductCategory.CUSTOM:
        return '自定义'
      default:
        return category
    }
  };

  return (
    <Layout>
      <div className="mb-8">
        <Title level={1} className="mb-2">{name}</Title>
        <Space className="mb-4">
          <Badge status={status === ProductStatus.ENABLE ? 'success' : 'default'} text={getStatusText(status)} />
          <Tag color="blue">{getCategoryText(category)}</Tag>
          <Tag color="purple">v1.0.0</Tag>
        </Space>
        <Paragraph className="text-gray-600">{description}</Paragraph>
      </div>

      <Card title="基本信息" className="mb-6">
        <Descriptions column={1} bordered size="small">
          <Descriptions.Item label="产品ID">{productId}</Descriptions.Item>
          <Descriptions.Item label="名称">{name}</Descriptions.Item>
          <Descriptions.Item label="描述">{description}</Descriptions.Item>
          <Descriptions.Item label="类型">{data.type}</Descriptions.Item>
          <Descriptions.Item label="状态">{getStatusText(status)}</Descriptions.Item>
          <Descriptions.Item label="分类">{category}</Descriptions.Item>
          <Descriptions.Item label="消费者认证">{enableConsumerAuth ? '启用' : '禁用'}</Descriptions.Item>
          <Descriptions.Item label="文档">{data.document ?? '无'}</Descriptions.Item>
          <Descriptions.Item label="图标">{data.icon ? <img src={data.icon} alt="icon" style={{ width: 32, height: 32 }} /> : '无'}</Descriptions.Item>
          <Descriptions.Item label="是否启用">{data.enabled ? '是' : '否'}</Descriptions.Item>
          {mcpConfig && (
            <>
              <Descriptions.Item label="fromType">{mcpConfig.fromType ?? '无'}</Descriptions.Item>
              <Descriptions.Item label="fromGatewayType">
                {GatewayTypeMap[mcpConfig.fromGatewayType ?? ''] || mcpConfig.fromGatewayType || '无'}
              </Descriptions.Item>
              <Descriptions.Item label="domains">
                {mcpConfig.domains && mcpConfig.domains.length > 0
                  ? mcpConfig.domains.map((d, i) => (
                      <div key={i}>{d.domain} ({d.protocol})</div>
                    ))
                  : '无'}
              </Descriptions.Item>
            </>
          )}
        </Descriptions>
      </Card>

      {/* MCP 配置信息 */}
      {mcpConfig && (
        <Card title="MCP 配置信息" className="mb-6">
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="MCP Route ID">{mcpConfig.mcpRouteId ?? '无'}</Descriptions.Item>
            <Descriptions.Item label="MCP Server Name">{mcpConfig.mcpServerName ?? '无'}</Descriptions.Item>
            <Descriptions.Item label="来源类型">{mcpConfig.fromType ?? '无'}</Descriptions.Item>
            <Descriptions.Item label="网关类型">
              {GatewayTypeMap[mcpConfig.fromGatewayType ?? ''] || mcpConfig.fromGatewayType || '无'}
            </Descriptions.Item>
            <Descriptions.Item label="域名配置">
              {mcpConfig.domains && mcpConfig.domains.length > 0
                ? mcpConfig.domains.map((d, i) => (
                    <div key={i} className="mb-2">
                      <div><strong>域名:</strong> {d.domain}</div>
                      <div><strong>协议:</strong> {d.protocol}</div>
                    </div>
                  ))
                : '无'}
            </Descriptions.Item>
          </Descriptions>
        </Card>
      )}

      {/* 连接 MCP 服务 */}
      {mcpConfig && mcpConfig.domains && mcpConfig.domains.length > 0 && (
        <Card title="连接 MCP 服务" className="mb-6">
          <div className="mb-4">
            <div className="font-bold mb-2">● Step 1. 生成 URL</div>
            <div className="mb-2">
              <strong>域名:</strong> {mcpConfig.domains[0].protocol}://{mcpConfig.domains[0].domain}
            </div>
            <Tabs
              defaultActiveKey="sse"
              items={[
                {
                  key: 'http',
                  label: 'Streamable HTTP',
                  children: (
                    <div className="relative">
                      <div className="absolute top-2 right-2">
                        <Button size="small" onClick={() => handleCopy(httpJson)}>复制</Button>
                      </div>
                      <div style={{ width: '100%', overflowX: 'auto', height: '200px' }}>
                        <MonacoEditor
                          language="json"
                          theme="vs-dark"
                          value={httpJson}
                          options={{
                            readOnly: true,
                            minimap: { enabled: false },
                            scrollBeyondLastLine: false,
                            scrollbar: { vertical: 'hidden', horizontal: 'hidden' },
                            wordWrap: 'on',
                            lineNumbers: 'on',
                            automaticLayout: true,
                            fontSize: 14,
                          }}
                          height="200"
                        />
                      </div>
                    </div>
                  ),
                },
                {
                  key: 'sse',
                  label: 'SSE',
                  children: (
                    <div className="relative">
                      <div className="absolute top-2 right-2">
                        <Button size="small" onClick={() => handleCopy(sseJson)}>复制</Button>
                      </div>
                      <div style={{ width: '100%', overflowX: 'auto', height: '200px' }}>
                        <MonacoEditor
                          language="json"
                          theme="vs-dark"
                          value={sseJson}
                          options={{
                            readOnly: true,
                            minimap: { enabled: false },
                            scrollBeyondLastLine: false,
                            scrollbar: { vertical: 'hidden', horizontal: 'hidden' },
                            wordWrap: 'on',
                            lineNumbers: 'on',
                            automaticLayout: true,
                            fontSize: 14,
                          }}
                          height="200"
                        />
                      </div>
                    </div>
                  ),
                },
              ]}
            />
          </div>

          <div>
            <div className="font-bold mb-2">● Step 2. 域名 DNS 映射</div>
            <div className="bg-gray-50 p-4 rounded">
              <div className="text-gray-600 mb-3">请确保您的客户端能够访问以下网关地址：</div>
              <div className="flex items-center justify-between">
                <span className="font-mono">{mcpConfig.domains[0].domain}</span>
                <Button type="link" onClick={() => handleCopy(mcpConfig.domains[0].domain)}>
                  复制
                </Button>
              </div>
            </div>
          </div>
        </Card>
      )}

      {/* 工具列表折叠展示 */}
      {mcpConfig?.tools && mcpConfig.tools.length > 0 && (
        <Card title="工具列表" className="mb-6">
          <Collapse accordion>
            {mcpConfig!.tools?.map((tool, idx) => (
              <Collapse.Panel header={tool.name} key={idx}>
                <div className="mb-2 text-gray-600">{tool.description}</div>
                <div className="mb-2 font-bold">输入参数：</div>
                <div className="space-y-2">
                  {tool.args?.map((arg, argIdx) => (
                    <div key={argIdx} className="flex flex-col mb-2">
                      <div className="flex items-center mb-1">
                        <span className="font-medium mr-2">{arg.name}</span>
                        <span className="text-xs text-gray-500 mr-2">({arg.type})</span>
                        {arg.required && <span className="text-red-500 text-xs">*</span>}
                      </div>
                      {arg.description && <div className="text-xs text-gray-500 mb-1">{arg.description}</div>}
                      <input disabled className="border rounded px-2 py-1 text-sm bg-gray-100 w-full max-w-md" placeholder={arg.default ?? ''} />
                    </div>
                  ))}
                  {(!tool.args || tool.args.length === 0) && <span className="text-gray-400">无参数</span>}
                </div>
              </Collapse.Panel>
            ))}
          </Collapse>
        </Card>
      )}

      {/* {(data as McpServerProduct).mcpSpec && (
        <Card title="MCP Server 配置" className="mb-6">
          <div className="mb-2 flex justify-end">
            <Button size="small" onClick={() => {
              navigator.clipboard.writeText((data as McpServerProduct).mcpSpec);
              message.success('已复制到剪贴板');
            }}>复制</Button>
          </div>
          <div style={{ width: '100%', overflowX: 'auto', height: '300px' }}>
            <MonacoEditor
              language="json"
              theme="vs"
              value={(data as McpServerProduct).mcpSpec}
              options={{
                readOnly: true,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                scrollbar: { vertical: 'hidden', horizontal: 'hidden' },
                wordWrap: 'on',
                lineNumbers: 'on',
                automaticLayout: true,
              }}
              height="300"
            />
          </div>
        </Card>
      )} */}
    </Layout>
  );
}

export default McpDetail;
