import { Card, Table, Tag, Button, Space, Typography, Input, Avatar, Badge } from "antd";
import { SearchOutlined, UserOutlined, EditOutlined, DeleteOutlined } from "@ant-design/icons";
import { Layout } from "../components/Layout";

const { Title, Paragraph } = Typography;
const { Search } = Input;

interface Consumer {
  key: string;
  name: string;
  email: string;
  company: string;
  status: string;
  plan: string;
  joinedAt: string;
  apiCalls: number;
  subscriptions: number;
}

const mockConsumers: Consumer[] = [
  {
    key: "1",
    name: "张三",
    email: "zhangsan@company-a.com",
    company: "Company A",
    status: "active",
    plan: "premium",
    joinedAt: "2025-01-01",
    apiCalls: 15420,
    subscriptions: 3
  },
  {
    key: "2",
    name: "李四",
    email: "lisi@company-b.com",
    company: "Company B",
    status: "active",
    plan: "standard",
    joinedAt: "2025-01-02",
    apiCalls: 8765,
    subscriptions: 2
  },
  {
    key: "3",
    name: "王五",
    email: "wangwu@company-c.com",
    company: "Company C",
    status: "inactive",
    plan: "basic",
    joinedAt: "2025-01-03",
    apiCalls: 1200,
    subscriptions: 1
  }
];

function ConsumersPage() {
  const getPlanColor = (plan: string) => {
    switch (plan) {
      case 'premium':
        return 'gold'
      case 'standard':
        return 'blue'
      case 'basic':
        return 'green'
      default:
        return 'default'
    }
  };

  const getPlanText = (plan: string) => {
    switch (plan) {
      case 'premium':
        return '高级版'
      case 'standard':
        return '标准版'
      case 'basic':
        return '基础版'
      default:
        return plan
    }
  };

  const columns = [
    {
      title: '消费者',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: Consumer) => (
        <div className="flex items-center space-x-3">
          <Avatar className="bg-blue-500">
            {name.charAt(0).toUpperCase()}
          </Avatar>
          <div>
            <div className="font-medium">{name}</div>
            <div className="text-sm text-gray-500">{record.email}</div>
            <div className="text-xs text-gray-400">{record.company}</div>
          </div>
        </div>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Badge status={status === 'active' ? 'success' : 'default'} text={status === 'active' ? '活跃' : '非活跃'} />
      )
    },
    {
      title: '套餐',
      dataIndex: 'plan',
      key: 'plan',
      render: (plan: string) => (
        <Tag color={getPlanColor(plan)}>
          {getPlanText(plan)}
        </Tag>
      )
    },
    {
      title: 'API调用',
      dataIndex: 'apiCalls',
      key: 'apiCalls',
      render: (calls: number) => calls.toLocaleString()
    },
    {
      title: '订阅数',
      dataIndex: 'subscriptions',
      key: 'subscriptions',
      render: (subscriptions: number) => subscriptions.toLocaleString()
    },
    {
      title: '加入时间',
      dataIndex: 'joinedAt',
      key: 'joinedAt',
      render: (date: string) => new Date(date).toLocaleDateString()
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any) => (
        <Space>
          <Button type="link" icon={<UserOutlined />}>
            查看
          </Button>
          <Button type="link" icon={<EditOutlined />}>
            编辑
          </Button>
          <Button type="link" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Layout>
      <div className="mb-8">
        <Title level={1} className="mb-2">
          消费者管理
        </Title>
        <Paragraph className="text-gray-600">
          管理API的消费者用户和订阅信息
        </Paragraph>
      </div>

      <Card>
        <div className="mb-4 flex gap-4">
          <Search
            placeholder="搜索消费者..."
            prefix={<SearchOutlined />}
            style={{ width: 300 }}
          />
        </div>
        
        <Table 
          columns={columns} 
          dataSource={mockConsumers}
          rowKey="key"
          pagination={{
            total: mockConsumers.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
        />
      </Card>

      <Card title="消费者统计" className="mt-8">
        <div className="grid grid-cols-4 gap-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">{mockConsumers.length}</div>
            <div className="text-sm text-gray-500">总消费者</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {mockConsumers.filter(c => c.status === 'active').length}
            </div>
            <div className="text-sm text-gray-500">活跃消费者</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-purple-600">
              {mockConsumers.reduce((sum, c) => sum + c.apiCalls, 0).toLocaleString()}
            </div>
            <div className="text-sm text-gray-500">总API调用</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-orange-600">
              {mockConsumers.reduce((sum, c) => sum + c.subscriptions, 0)}
            </div>
            <div className="text-sm text-gray-500">总订阅数</div>
          </div>
        </div>
      </Card>
    </Layout>
  );
}

export default ConsumersPage; 