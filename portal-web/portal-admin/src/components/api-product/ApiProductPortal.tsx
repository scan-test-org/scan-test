import { Card, Button, Table, Tag, Space, Switch, Modal, Form, Input, Select } from 'antd'
import { PlusOutlined, EyeOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { useState } from 'react'

interface ApiProduct {
  id: string
  name: string
  description: string
  version: string
  status: string
  visibility: string
  createdAt: string
  updatedAt: string
  portals: number
  linkedServices: number
  policies: number
}

interface ApiProductPortalProps {
  apiProduct: ApiProduct
}

interface Portal {
  id: string
  name: string
  url: string
  status: string
  publishedAt: string
  visitors: number
  subscriptions: number
}

const mockPortals: Portal[] = [
  {
    id: "1",
    name: "Company Portal",
    url: "https://company.example.com",
    status: "published",
    publishedAt: "2025-01-01T10:00:00Z",
    visitors: 1250,
    subscriptions: 45
  },
  {
    id: "2",
    name: "Developer Portal",
    url: "https://dev.example.com",
    status: "draft",
    publishedAt: "2025-01-02T11:00:00Z",
    visitors: 0,
    subscriptions: 0
  }
]

export function ApiProductPortal({ apiProduct }: ApiProductPortalProps) {
  const [portals, setPortals] = useState<Portal[]>(mockPortals)
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [form] = Form.useForm()

  const columns = [
    {
      title: '门户名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'URL',
      dataIndex: 'url',
      key: 'url',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'published' ? 'green' : 'orange'}>
          {status === 'published' ? '已发布' : '草稿'}
        </Tag>
      )
    },
    {
      title: '访问量',
      dataIndex: 'visitors',
      key: 'visitors',
      render: (visitors: number) => visitors.toLocaleString()
    },
    {
      title: '订阅数',
      dataIndex: 'subscriptions',
      key: 'subscriptions',
      render: (subscriptions: number) => subscriptions.toLocaleString()
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
      render: (_: any, record: Portal) => (
        <Space size="middle">
          <Button type="link" icon={<EyeOutlined />}>
            查看
          </Button>
          <Button type="link" icon={<EditOutlined />}>
            编辑
          </Button>
          <Button 
            type="link" 
            danger 
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
          >
            移除
          </Button>
        </Space>
      ),
    },
  ]

  const handleAdd = () => {
    setIsModalVisible(true)
  }

  const handleDelete = (id: string) => {
    setPortals(portals.filter(portal => portal.id !== id))
  }

  const handleModalOk = () => {
    form.validateFields().then((values) => {
      const newPortal: Portal = {
        id: Date.now().toString(),
        name: values.name,
        url: values.url,
        status: 'draft',
        publishedAt: new Date().toISOString(),
        visitors: 0,
        subscriptions: 0
      }
      setPortals([...portals, newPortal])
      setIsModalVisible(false)
      form.resetFields()
    })
  }

  const handleModalCancel = () => {
    setIsModalVisible(false)
    form.resetFields()
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold mb-2">发布门户</h1>
          <p className="text-gray-600">管理API产品发布的门户</p>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          发布到门户
        </Button>
      </div>

      <Card>
        <Table 
          columns={columns} 
          dataSource={portals}
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Card title="发布设置">
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <span>自动发布</span>
            <Switch defaultChecked />
          </div>
          <div className="flex justify-between items-center">
            <span>版本控制</span>
            <Switch defaultChecked />
          </div>
          <div className="flex justify-between items-center">
            <span>访问统计</span>
            <Switch defaultChecked />
          </div>
        </div>
      </Card>

      <Modal
        title="发布到门户"
        open={isModalVisible}
        onOk={handleModalOk}
        onCancel={handleModalCancel}
        okText="发布"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="门户名称"
            rules={[{ required: true, message: '请输入门户名称' }]}
          >
            <Input placeholder="请输入门户名称" />
          </Form.Item>
          <Form.Item
            name="url"
            label="门户URL"
            rules={[{ required: true, message: '请输入门户URL' }]}
          >
            <Input placeholder="https://portal.example.com" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
} 