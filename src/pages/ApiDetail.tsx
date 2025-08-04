import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Card, Badge, Table, Typography, Space, Tag, Spin, Alert, Descriptions, Button, Modal } from "antd";
import { FileTextOutlined } from "@ant-design/icons";
import { Layout } from "../components/Layout";
import api from "../lib/api";
import { ProductStatus, ProductCategory } from "../types";
import type { Product, ApiResponse } from "../types";
import MonacoEditor from "react-monaco-editor";

const { Title, Paragraph } = Typography;

interface ApiEndpoint {
  key: string;
  method: string;
  path: string;
  description: string;
  operationId?: string;
  spec?: string;
}

interface ApiConfig {
  spec: string;
  meta: {
    source: string;
    type: string;
  };
}

interface UpdatedProduct extends Omit<Product, 'apiSpec'> {
  apiConfig?: ApiConfig;
  createAt: string;
  enabled: boolean;
}

function ApiDetailPage() {
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [apiData, setApiData] = useState<UpdatedProduct | null>(null);
  const [endpoints, setEndpoints] = useState<ApiEndpoint[]>([]);
  const [isSpecModalVisible, setIsSpecModalVisible] = useState(false);
  const [currentSpec, setCurrentSpec] = useState('');

  useEffect(() => {
    if (!id) return;
    fetchApiDetail();
  }, [id]);

  const fetchApiDetail = async () => {
    setLoading(true);
    setError('');
    try {
      const response: ApiResponse<UpdatedProduct> = await api.get(`/products/${id}`);
      if (response.code === "SUCCESS" && response.data) {
        setApiData(response.data);
        
        // 尝试从apiConfig.spec中解析端点信息
        if (response.data.apiConfig?.spec) {
          try {
            // 解析OpenAPI规范
            const spec = response.data.apiConfig.spec;
            const endpointsList: ApiEndpoint[] = [];
            
            // 改进的OpenAPI解析逻辑
            const lines = spec.split('\n');
            let currentPath = '';
            let inPaths = false;
            
            for (let i = 0; i < lines.length; i++) {
              const line = lines[i];
              const trimmedLine = line.trim();
              const indentLevel = line.length - line.trimStart().length;
              
              // 检测是否进入paths部分
              if (trimmedLine === 'paths:' || trimmedLine.startsWith('paths:')) {
                inPaths = true;
                continue;
              }
              
              // 如果不在paths部分，跳过
              if (!inPaths) continue;
              
              // 检测路径 - 必须是顶级缩进（通常是2个空格）
              if (inPaths && indentLevel === 2 && trimmedLine.startsWith('/') && trimmedLine.endsWith(':')) {
                currentPath = trimmedLine.slice(0, -1); // 移除末尾的冒号
                continue;
              }
              
              // 检测HTTP方法 - 必须是路径下的子级（通常是4个空格）
              if (inPaths && indentLevel === 4) {
                const httpMethods = ['get:', 'post:', 'put:', 'delete:', 'patch:', 'head:', 'options:'];
                for (const method of httpMethods) {
                  if (trimmedLine.startsWith(method)) {
                    const methodName = method.replace(':', '').toUpperCase();
                    const operationId = extractOperationId(lines, i);
                    
                    endpointsList.push({
                      key: `${methodName}-${currentPath}`,
                      method: methodName,
                      path: currentPath,
                      description: operationId || `${methodName} ${currentPath}`,
                      operationId: operationId,
                      spec: spec
                    });
                    break;
                  }
                }
              }
            }
            
            console.log('解析到的端点:', endpointsList);
            if (endpointsList.length > 0) {
              setEndpoints(endpointsList);
            } else {
              console.log('未解析到端点，使用默认端点');
              setDefaultEndpoints();
            }
          } catch (error) {
            console.warn('解析API规范失败:', error);
            setDefaultEndpoints();
          }
        } else {
          setDefaultEndpoints();
        }
      }
    } catch (error) {
      console.error('获取API详情失败:', error);
      setError('加载失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const extractOperationId = (lines: string[], startIndex: number): string => {
    // 从当前行开始向后查找operationId
    const currentIndent = lines[startIndex].length - lines[startIndex].trimStart().length;
    
    for (let i = startIndex + 1; i < Math.min(startIndex + 20, lines.length); i++) {
      const line = lines[i];
      const trimmedLine = line.trim();
      const lineIndent = line.length - line.trimStart().length;
      
      // 如果缩进级别小于等于当前级别，说明已经离开了当前方法块
      if (lineIndent <= currentIndent && trimmedLine !== '') {
        break;
      }
      
      // 查找operationId
      if (trimmedLine.startsWith('operationId:')) {
        return trimmedLine.replace('operationId:', '').trim();
      }
    }
    return '';
  };

  const setDefaultEndpoints = () => {
    setEndpoints([
      {
        key: "1",
        method: "GET",
        path: "/api/endpoints",
        description: "获取端点列表"
      }
    ]);
  };

  const showSpecModal = (spec: string) => {
    setCurrentSpec(spec);
    setIsSpecModalVisible(true);
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PUBLISHED':
        return '已发布'
      case 'DRAFT':
        return '草稿'
      case 'DEPRECATED':
        return '已弃用'
      case ProductStatus.ENABLE:
        return '活跃'
      case ProductStatus.DISABLE:
        return '非活跃'
      default:
        return status
    }
  };

  const getStatusColor = (status: string): "success" | "processing" | "default" => {
    switch (status) {
      case 'PUBLISHED':
        return 'success'
      case 'DRAFT':
        return 'processing'
      case 'DEPRECATED':
        return 'default'
      case ProductStatus.ENABLE:
        return 'success'
      case ProductStatus.DISABLE:
        return 'default'
      default:
        return 'default'
    }
  };

  const getCategoryText = (category: string) => {
    switch (category) {
      case ProductCategory.OFFICIAL:
      case 'official2':
        return '官方'
      case ProductCategory.COMMUNITY:
        return '社区'
      case ProductCategory.CUSTOM:
        return '自定义'
      default:
        return category
    }
  };

  const getCategoryColor = (category: string) => {
    switch (category) {
      case ProductCategory.OFFICIAL:
      case 'official2':
        return 'blue'
      case ProductCategory.COMMUNITY:
        return 'green'
      case ProductCategory.CUSTOM:
        return 'orange'
      default:
        return 'default'
    }
  };

  const formatDate = (dateString: string) => {
    try {
      return new Date(dateString).toLocaleString('zh-CN');
    } catch {
      return dateString;
    }
  };

  const columns = [
    {
      title: '方法',
      dataIndex: 'method',
      key: 'method',
      width: 100,
      render: (method: string) => (
        <Tag color={method === 'GET' ? 'green' : method === 'POST' ? 'blue' : method === 'PUT' ? 'orange' : method === 'DELETE' ? 'red' : 'default'}>
          {method}
        </Tag>
      )
    },
    {
      title: '路径',
      dataIndex: 'path',
      key: 'path',
      width: 200,
      render: (path: string) => (
        <code className="text-sm bg-gray-100 px-2 py-1 rounded">{path}</code>
      )
    },
    {
      title: '操作ID',
      dataIndex: 'operationId',
      key: 'operationId',
      width: 150,
      render: (operationId: string) => (
        <span className="text-sm text-gray-600">{operationId || '-'}</span>
      )
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    }
  ];

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

  if (!apiData) {
    return (
      <Layout>
        <Alert message="未找到API信息" type="warning" showIcon className="my-8" />
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="mb-8">
        <Title level={1} className="mb-2">
          {apiData.name}
        </Title>
        <Space className="mb-4">
          <Badge 
            status={getStatusColor(apiData.status)} 
            text={getStatusText(apiData.status)} 
          />
          <Tag color="blue">v1.0.0</Tag>
          <Tag color={getCategoryColor(apiData.category)}>{getCategoryText(apiData.category)}</Tag>
          {apiData.enabled && <Tag color="green">已启用</Tag>}
        </Space>
        <Paragraph className="text-gray-600">
          {apiData.description}
        </Paragraph>
      </div>

      <Card title="基本信息" className="mb-6">
        <Descriptions column={2} bordered size="small">
          <Descriptions.Item label="产品ID">{apiData.productId}</Descriptions.Item>
          <Descriptions.Item label="类型">{apiData.type}</Descriptions.Item>
          <Descriptions.Item label="状态">{getStatusText(apiData.status)}</Descriptions.Item>
          <Descriptions.Item label="分类">{getCategoryText(apiData.category)}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{formatDate(apiData.createAt)}</Descriptions.Item>
          <Descriptions.Item label="启用状态">
            {apiData.enabled ? '已启用' : '未启用'}
          </Descriptions.Item>
          {apiData.apiConfig && (
            <>
              <Descriptions.Item label="API源">{apiData.apiConfig.meta.source}</Descriptions.Item>
              <Descriptions.Item label="API类型">{apiData.apiConfig.meta.type}</Descriptions.Item>
            </>
          )}
        </Descriptions>
      </Card>

      <Card 
        title={
          <div className="flex items-center justify-between">
            <span>API 端点</span>
            {apiData.apiConfig && (
              <Button 
                type="text" 
                icon={<FileTextOutlined />} 
                onClick={() => showSpecModal(apiData.apiConfig!.spec)}
                className="text-blue-500 hover:text-blue-700"
              >
                查看 OpenAPI 规范
              </Button>
            )}
          </div>
        } 
        className="mb-8"
      >
        <Table 
          columns={columns} 
          dataSource={endpoints}
          rowKey="key"
          pagination={false}
          size="small"
        />
      </Card>

      <Card title="API 绑定关系">
        <div className="p-4">
          <Paragraph className="text-gray-600">
            这里展示API的绑定关系、消费者等信息
          </Paragraph>
        </div>
      </Card>

      {/* OpenAPI 规范模态框 */}
      <Modal
        title="OpenAPI 规范"
        open={isSpecModalVisible}
        onCancel={() => setIsSpecModalVisible(false)}
        footer={null}
        width="80%"
        style={{ top: 20 }}
        bodyStyle={{ height: '70vh', padding: 0 }}
      >
        <div style={{ height: '100%' }}>
          <MonacoEditor
            language="yaml"
            theme="vs-dark"
            value={currentSpec}
            options={{
              readOnly: true,
              minimap: { enabled: true },
              scrollBeyondLastLine: false,
              scrollbar: {
                vertical: "visible",
                horizontal: "visible",
              },
              wordWrap: "off",
              lineNumbers: "on",
              automaticLayout: true,
              fontSize: 14,
              copyWithSyntaxHighlighting: true,
              contextmenu: true,
            }}
            height="100%"
          />
        </div>
      </Modal>
    </Layout>
  );
}

export default ApiDetailPage; 