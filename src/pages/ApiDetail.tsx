import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Card, Badge, Table, Typography, Space, Tag, Spin, Alert, Descriptions } from "antd";
import { Layout } from "../components/Layout";
import api from "../lib/api";
import { ProductStatus, ProductCategory, ProductType } from "../types";
import type { Product, ApiResponse, RestApiProduct } from "../types";
import { processProductSpecs } from "../lib/utils";

const { Title, Paragraph } = Typography;

interface ApiEndpoint {
  key: string;
  method: string;
  path: string;
  description: string;
}

function ApiDetailPage() {
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [apiData, setApiData] = useState<Product | null>(null);
  const [endpoints, setEndpoints] = useState<ApiEndpoint[]>([]);

  useEffect(() => {
    if (!id) return;
    fetchApiDetail();
  }, [id]);

  const fetchApiDetail = async () => {
    setLoading(true);
    setError('');
    try {
      const response: ApiResponse<Product> = await api.get(`/products/${id}`);
      if (response.code === "SUCCESS" && response.data) {
        // 处理 apiSpec 中的换行符转义
        const processedData = processProductSpecs(response.data);
        
        setApiData(processedData);
        
        // 尝试从apiSpec中解析端点信息
        if (processedData.apiSpec) {
          try {
            const spec = JSON.parse(processedData.apiSpec) as Record<string, unknown>;
            if (spec.paths) {
              const endpointList: ApiEndpoint[] = [];
              Object.entries(spec.paths).forEach(([path, methods]: [string, unknown]) => {
                const methodsObj = methods as Record<string, unknown>;
                Object.keys(methodsObj).forEach((method) => {
                  const methodObj = methodsObj[method] as Record<string, unknown>;
                  endpointList.push({
                    key: `${method}-${path}`,
                    method: method.toUpperCase(),
                    path,
                    description: (methodObj.summary as string) || (methodObj.description as string) || '无描述'
                  });
                });
              });
              setEndpoints(endpointList);
            }
          } catch (error) {
            console.warn('解析API规范失败:', error);
            // 设置默认端点
            setEndpoints([
              {
                key: "1",
                method: "GET",
                path: "/api/endpoints",
                description: "获取端点列表"
              }
            ]);
          }
        } else {
          // 设置默认端点
          setEndpoints([
            {
              key: "1",
              method: "GET",
              path: "/api/endpoints",
              description: "获取端点列表"
            }
          ]);
        }
      }
    } catch (error) {
      console.error('获取API详情失败:', error);
      setError('加载失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const getStatusText = (status: ProductStatus) => {
    switch (status) {
      case ProductStatus.ENABLE:
        return '活跃'
      case ProductStatus.DISABLE:
        return '非活跃'
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

  const columns = [
    {
      title: '方法',
      dataIndex: 'method',
      key: 'method',
      render: (method: string) => (
        <Tag color={method === 'GET' ? 'green' : method === 'POST' ? 'blue' : 'orange'}>
          {method}
        </Tag>
      )
    },
    {
      title: '路径',
      dataIndex: 'path',
      key: 'path',
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
            status={apiData.status === ProductStatus.ENABLE ? 'success' : 'default'} 
            text={getStatusText(apiData.status)} 
          />
          <Tag color="blue">v1.0.0</Tag>
          <Tag color="purple">{getCategoryText(apiData.category)}</Tag>
        </Space>
        <Paragraph className="text-gray-600">
          {apiData.description}
        </Paragraph>
      </div>

      <Card title="基本信息" className="mb-6">
        <Descriptions column={1} bordered size="small">
          <Descriptions.Item label="产品ID">{apiData.productId}</Descriptions.Item>
          <Descriptions.Item label="类型">{apiData.type}</Descriptions.Item>
          <Descriptions.Item label="状态">{getStatusText(apiData.status)}</Descriptions.Item>
          <Descriptions.Item label="分类">{getCategoryText(apiData.category)}</Descriptions.Item>
          <Descriptions.Item label="消费者认证">
            {apiData.enableConsumerAuth ? '启用' : '禁用'}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="API 端点" className="mb-8">
        <Table 
          columns={columns} 
          dataSource={endpoints}
          rowKey="key"
          pagination={false}
        />
      </Card>

      <Card title="API 绑定关系">
        <div className="p-4">
          <Paragraph className="text-gray-600">
            这里展示API的绑定关系、消费者等信息
          </Paragraph>
        </div>
      </Card>
    </Layout>
  );
}

export default ApiDetailPage; 