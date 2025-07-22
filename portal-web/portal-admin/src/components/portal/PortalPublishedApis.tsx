import { Card, Table, Badge, Button, Space, Tag } from 'antd'
import { EyeOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'

interface Portal {
  id: string
  name: string
  title: string
  description: string
  url: string
  userAuth: string
  rbac: string
  authStrategy: string
  apiVisibility: string
  pageVisibility: string
  logo?: string
}

interface PortalPublishedApisProps {
  portal: Portal
}

interface PublishedApi {
  id: string
  name: string
  version: string
  status: string
  subscribers: number
  requests: number
  publishedAt: string
  category: string
}

const mockPublishedApis: PublishedApi[] = [
  {
    id: "1",
    name: "Payment API",
    version: "v1.2.0",
    status: "published",
    subscribers: 25,
    requests: 15420,
    publishedAt: "2025-01-01T10:00:00Z",
    category: "Finance"
  },
  {
    id: "2",
    name: "User API",
    version: "v1.1.0",
    status: "published",
    subscribers: 18,
    requests: 8765,
    publishedAt: "2025-01-02T11:00:00Z",
    category: "Authentication"
  },
  {
    id: "3",
    name: "Notification API",
    version: "v1.0.0",
    status: "draft",
    subscribers: 0,
    requests: 0,
    publishedAt: "2025-01-03T12:00:00Z",
    category: "Communication"
  }
]

export function PortalPublishedApis({ portal }: PortalPublishedApisProps) {
  const columns = [
    {
      title: 'API名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: PublishedApi) => (
        <div>
          <div className="font-medium">{name}</div>
          <div className="text-sm text-gray-500">{record.category}</div>
        </div>
      ),
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      render: (version: string) => <Tag color="blue">{version}</Tag>
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Badge status={status === 'published' ? 'success' : 'default'} text={status === 'published' ? '已发布' : '草稿'} />
      )
    },
    {
      title: '订阅者',
      dataIndex: 'subscribers',
      key: 'subscribers',
      render: (subscribers: number) => subscribers.toLocaleString()
    },
    {
      title: '请求数',
      dataIndex: 'requests',
      key: 'requests',
      render: (requests: number) => requests.toLocaleString()
    },
    {
      title: '发布时间',
      dataIndex: 'publishedAt',
      key: 'publishedAt',
      render: (date: string) => new Date(date).toLocaleDateString()
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: PublishedApi) => (
        <Space size="middle">
          <Button type="link" icon={<EyeOutlined />}>
            查看
          </Button>
          <Button type="link" icon={<EditOutlined />}>
            编辑
          </Button>
          <Button type="link" danger icon={<DeleteOutlined />}>
            移除
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold mb-2">已发布API</h1>
          <p className="text-gray-600">管理在此Portal中发布的API</p>
        </div>
        <Button type="primary">
          发布新API
        </Button>
      </div>

      <Card>
        <Table 
          columns={columns} 
          dataSource={mockPublishedApis}
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Card title="发布设置">
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <span>自动发布</span>
            <Badge status="success" text="启用" />
          </div>
          <div className="flex justify-between items-center">
            <span>版本控制</span>
            <Badge status="success" text="启用" />
          </div>
          <div className="flex justify-between items-center">
            <span>访问统计</span>
            <Badge status="success" text="启用" />
          </div>
        </div>
      </Card>
    </div>
  )
} 