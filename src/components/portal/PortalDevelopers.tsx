import { Card, Table, Badge, Button, Space, Avatar, Tag, Input } from 'antd'
import { SearchOutlined, UserAddOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { useState } from 'react'
import { Portal } from '@/types'

interface PortalDevelopersProps {
  portal: Portal
}

interface Developer {
  id: string
  name: string
  email: string
  status: string
  role: string
  joinedAt: string
  lastActive: string
  subscriptions: number
}

const mockDevelopers: Developer[] = [
  {
    id: "1",
    name: "张三",
    email: "zhangsan@example.com",
    status: "active",
    role: "developer",
    joinedAt: "2025-01-01T10:00:00Z",
    lastActive: "2025-01-08T15:30:00Z",
    subscriptions: 3
  },
  {
    id: "2",
    name: "李四",
    email: "lisi@example.com",
    status: "active",
    role: "admin",
    joinedAt: "2025-01-02T11:00:00Z",
    lastActive: "2025-01-08T14:20:00Z",
    subscriptions: 5
  },
  {
    id: "3",
    name: "王五",
    email: "wangwu@example.com",
    status: "inactive",
    role: "developer",
    joinedAt: "2025-01-03T12:00:00Z",
    lastActive: "2025-01-05T09:15:00Z",
    subscriptions: 1
  }
]

export function PortalDevelopers({ portal }: PortalDevelopersProps) {
  const [developers, setDevelopers] = useState<Developer[]>(mockDevelopers)
  const [searchText, setSearchText] = useState('')

  const filteredDevelopers = developers.filter(developer =>
    developer.name.toLowerCase().includes(searchText.toLowerCase()) ||
    developer.email.toLowerCase().includes(searchText.toLowerCase())
  )

  const columns = [
    {
      title: '开发者',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: Developer) => (
        <div className="flex items-center space-x-3">
          <Avatar className="bg-blue-500">
            {name.charAt(0).toUpperCase()}
          </Avatar>
          <div>
            <div className="font-medium">{name}</div>
            <div className="text-sm text-gray-500">{record.email}</div>
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
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      render: (role: string) => (
        <Tag color={role === 'admin' ? 'red' : 'blue'}>
          {role === 'admin' ? '管理员' : '开发者'}
        </Tag>
      )
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
      title: '最后活跃',
      dataIndex: 'lastActive',
      key: 'lastActive',
      render: (date: string) => new Date(date).toLocaleDateString()
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Developer) => (
        <Space size="middle">
          <Button type="link" icon={<EditOutlined />}>
            编辑
          </Button>
          <Button type="link" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold mb-2">开发者</h1>
          <p className="text-gray-600">管理Portal的开发者用户</p>
        </div>
        <Button type="primary" icon={<UserAddOutlined />}>
          添加开发者
        </Button>
      </div>

      <Card>
        <div className="mb-4">
          <Input
            placeholder="搜索开发者..."
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            style={{ width: 300 }}
          />
        </div>
        <Table 
          columns={columns} 
          dataSource={filteredDevelopers}
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Card title="开发者统计">
        <div className="grid grid-cols-4 gap-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">{developers.length}</div>
            <div className="text-sm text-gray-500">总开发者</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {developers.filter(d => d.status === 'active').length}
            </div>
            <div className="text-sm text-gray-500">活跃开发者</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-purple-600">
              {developers.filter(d => d.role === 'admin').length}
            </div>
            <div className="text-sm text-gray-500">管理员</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-orange-600">
              {developers.reduce((sum, d) => sum + d.subscriptions, 0)}
            </div>
            <div className="text-sm text-gray-500">总订阅数</div>
          </div>
        </div>
      </Card>
    </div>
  )
} 