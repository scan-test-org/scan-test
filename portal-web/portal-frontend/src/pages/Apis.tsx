import { Card, Table, Tag, Button, Space, Typography, Input, Select } from "antd";
import { SearchOutlined, EyeOutlined, FileTextOutlined } from "@ant-design/icons";
import { Link } from "react-router-dom";
import { Layout } from "../components/Layout";

const { Title, Paragraph } = Typography;
const { Search } = Input;

interface ApiItem {
  key: string;
  name: string;
  description: string;
  category: string;
  status: string;
  version: string;
  subscribers: number;
}

const mockApis: ApiItem[] = [
  {
    key: "1",
    name: "Payment API",
    description: "处理支付相关的API接口",
    category: "Finance",
    status: "active",
    version: "v1.2.0",
    subscribers: 25
  },
  {
    key: "2",
    name: "User API",
    description: "用户管理和认证API",
    category: "Authentication",
    status: "active",
    version: "v1.1.0",
    subscribers: 18
  },
  {
    key: "3",
    name: "Notification API",
    description: "消息通知服务API",
    category: "Communication",
    status: "draft",
    version: "v1.0.0",
    subscribers: 0
  }
];

function APIsPage() {
  const columns = [
    {
      title: 'API名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: ApiItem) => (
        <div>
          <div className="font-medium">{name}</div>
          <div className="text-sm text-gray-500">{record.description}</div>
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
        <Tag color={status === 'active' ? 'green' : 'orange'}>
          {status === 'active' ? '活跃' : '草稿'}
        </Tag>
      )
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      render: (version: string) => <Tag color="purple">{version}</Tag>
    },
    {
      title: '订阅者',
      dataIndex: 'subscribers',
      key: 'subscribers',
      render: (subscribers: number) => subscribers.toLocaleString()
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: ApiItem) => (
        <Space>
          <Link to={`/apis/${record.key}`}>
            <Button type="link" icon={<EyeOutlined />}>
              查看
            </Button>
          </Link>
          <Button type="link" icon={<FileTextOutlined />}>
            文档
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Layout>
      <div className="mb-8">
        <Title level={1} className="mb-2">
          API 列表
        </Title>
        <Paragraph className="text-gray-600">
          探索我们提供的各种API服务
        </Paragraph>
      </div>

      <Card>
        <div className="mb-4 flex gap-4">
          <Search
            placeholder="搜索API..."
            prefix={<SearchOutlined />}
            style={{ width: 300 }}
          />
          <Select
            placeholder="选择分类"
            style={{ width: 150 }}
            allowClear
          >
            <Select.Option value="Finance">Finance</Select.Option>
            <Select.Option value="Authentication">Authentication</Select.Option>
            <Select.Option value="Communication">Communication</Select.Option>
          </Select>
          <Select
            placeholder="选择状态"
            style={{ width: 150 }}
            allowClear
          >
            <Select.Option value="active">活跃</Select.Option>
            <Select.Option value="draft">草稿</Select.Option>
          </Select>
        </div>
        
        <Table 
          columns={columns} 
          dataSource={mockApis}
          rowKey="key"
          pagination={{
            total: mockApis.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
        />
      </Card>
    </Layout>
  );
}

export default APIsPage; 