import { Card, Table, Tag, Button, Space, Typography, Input, Badge, Avatar } from "antd";
import { SearchOutlined, EyeOutlined, SettingOutlined } from "@ant-design/icons";
import { Link } from "react-router-dom";
import { Layout } from "../components/Layout";

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

const mockMcpServers: McpServer[] = [
  {
    key: "1",
    name: "GitHub MCP Server",
    description: "GitHub仓库和项目管理服务",
    status: "active",
    version: "v1.0.0",
    endpoints: 15,
    lastUpdated: "2025-01-15",
    category: "Development"
  },
  {
    key: "2",
    name: "Database MCP Server",
    description: "数据库连接和查询服务",
    status: "active",
    version: "v1.2.0",
    endpoints: 8,
    lastUpdated: "2025-01-14",
    category: "Data"
  },
  {
    key: "3",
    name: "File System MCP Server",
    description: "文件系统操作服务",
    status: "maintenance",
    version: "v1.1.0",
    endpoints: 12,
    lastUpdated: "2025-01-13",
    category: "Storage"
  }
];

function McpPage() {
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

  const columns = [
    {
      title: '服务器名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: McpServer) => (
        <div className="flex items-center space-x-3">
          <Avatar className="bg-blue-500" />
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
      render: (category: string) => <Tag color="blue">{category}</Tag>
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
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
          <Button type="link" icon={<SettingOutlined />}>
            配置
          </Button>
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
          />
        </div>
        
        <Table 
          columns={columns} 
          dataSource={mockMcpServers}
          rowKey="key"
          pagination={{
            total: mockMcpServers.length,
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
            <div className="text-2xl font-bold text-blue-600">{mockMcpServers.length}</div>
            <div className="text-sm text-gray-500">总服务器</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {mockMcpServers.filter(s => s.status === 'active').length}
            </div>
            <div className="text-sm text-gray-500">活跃服务器</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-purple-600">
              {mockMcpServers.reduce((sum, s) => sum + s.endpoints, 0)}
            </div>
            <div className="text-sm text-gray-500">总端点</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-orange-600">
              {mockMcpServers.filter(s => s.status === 'maintenance').length}
            </div>
            <div className="text-sm text-gray-500">维护中</div>
          </div>
        </div>
      </Card>
    </Layout>
  );
}

export default McpPage; 