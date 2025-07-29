import { useEffect, useState } from "react";
import { Card, Table, Tag, Button, Space, Typography, Input, Badge } from "antd";
import { SearchOutlined, EyeOutlined } from "@ant-design/icons";
import { Link } from "react-router-dom";
import { Layout } from "../components/Layout";
import api from "../lib/api";
import { ProductType, ProductStatus, ProductCategory } from "../types";
import type { Product, ApiResponse, PaginatedResponse } from "../types";

const { Title, Paragraph } = Typography;
const { Search } = Input;

interface McpServer {
  key: string;
  name: string;
  description: string;
  status: string;
  version: string;
  endpoints: number;
  lastUpdated: string;
  category: string;
}

function McpPage() {
  const [loading, setLoading] = useState(false);
  const [mcpServers, setMcpServers] = useState<McpServer[]>([]);
  const [searchText, setSearchText] = useState('');

  useEffect(() => {
    fetchMcpServers();
  }, []);

  const fetchMcpServers = async () => {
    setLoading(true);
    try {
      const response: ApiResponse<PaginatedResponse<Product>> = await api.get("/products?type=MCP_SERVER&page=0&size=100");
      if (response.code === "SUCCESS" && response.data) {
        const mapped = response.data.content
          .filter((item: Product) => item.type === ProductType.MCP_SERVER)
          .map((item: Product) => {
            // 尝试解析MCP配置以获取工具数量
            let toolCount = 0;
            try {
              if (item.mcpSpec) {
                const mcpConfig = JSON.parse(item.mcpSpec) as Record<string, unknown>;
                if (mcpConfig.tools && Array.isArray(mcpConfig.tools)) {
                  toolCount = mcpConfig.tools.length;
                }
              }
            } catch (error) {
              console.warn('解析MCP配置失败:', error);
            }

            return {
              key: item.productId,
              name: item.name,
              description: item.description,
              status: item.status === ProductStatus.ENABLE ? 'active' : 'inactive',
              version: 'v1.0.0', // 从mcpSpec中解析版本信息
              endpoints: toolCount,
              lastUpdated: new Date().toISOString().slice(0, 10), // 暂时使用当前日期
              category: item.category,
            };
          });
        setMcpServers(mapped);
      }
    } catch (error) {
      console.error('获取MCP服务器列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'active':
        return '活跃'
      case 'maintenance':
        return '维护中'
      case 'inactive':
        return '非活跃'
      default:
        return status
    }
  };

  const getCategoryText = (category: string) => {
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

  const filteredMcpServers = mcpServers.filter(server => {
    return server.name.toLowerCase().includes(searchText.toLowerCase()) ||
           server.description.toLowerCase().includes(searchText.toLowerCase());
  });

  const columns = [
    {
      title: '服务器名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: McpServer) => (
        <div className="flex items-center space-x-3">
          <div>
            <div className="font-medium">{name}</div>
            <div className="text-sm text-gray-500">{record.description}</div>
          </div>
        </div>
      ),
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (category: string) => <Tag color="blue">{getCategoryText(category)}</Tag>
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: string) => (
        <Badge 
          status={status === 'active' ? 'success' : status === 'maintenance' ? 'processing' : 'default'} 
          text={getStatusText(status)} 
        />
      )
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      render: (version: string) => <Tag color="purple">{version}</Tag>
    },
    {
      title: '端点数量',
      dataIndex: 'endpoints',
      width: 100,
      key: 'endpoints',
      render: (endpoints: number) => endpoints.toLocaleString()
    },
    {
      title: '最后更新',
      dataIndex: 'lastUpdated',
      key: 'lastUpdated',
      render: (date: string) => new Date(date).toLocaleDateString()
    },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, record: McpServer) => (
        <Space>
          <Link to={`/mcp/${record.key}`}>
            <Button type="link" icon={<EyeOutlined />}>
              查看
            </Button>
          </Link>
        </Space>
      ),
    },
  ];

  return (
    <Layout>
      <div className="mb-8">
        <Title level={1} className="mb-2">
          MCP 服务器
        </Title>
        <Paragraph className="text-gray-600">
          管理和监控您的MCP (Model Context Protocol) 服务器
        </Paragraph>
      </div>

      <Card>
        <div className="mb-4 flex gap-4">
          <Search
            placeholder="搜索MCP服务器..."
            prefix={<SearchOutlined />}
            style={{ width: 300 }}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
          />
        </div>
        
        <Table 
          columns={columns} 
          dataSource={filteredMcpServers}
          loading={loading}
          rowKey="key"
          pagination={{
            total: filteredMcpServers.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
        />
      </Card>

      <Card title="MCP 服务器统计" className="mt-8">
        <div className="grid grid-cols-4 gap-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">{mcpServers.length}</div>
            <div className="text-sm text-gray-500">总服务器</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {mcpServers.filter(s => s.status === 'active').length}
            </div>
            <div className="text-sm text-gray-500">活跃服务器</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-purple-600">
              {mcpServers.reduce((sum, s) => sum + s.endpoints, 0)}
            </div>
            <div className="text-sm text-gray-500">总端点</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-orange-600">
              {mcpServers.filter(s => s.status === 'maintenance').length}
            </div>
            <div className="text-sm text-gray-500">维护中</div>
          </div>
        </div>
      </Card>
    </Layout>
  );
}

export default McpPage; 