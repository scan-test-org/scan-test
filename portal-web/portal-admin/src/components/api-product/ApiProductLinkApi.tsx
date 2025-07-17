import { Card, Button, Table, Tag, Space, Modal, Form, Input, Select } from 'antd'
import { PlusOutlined, LinkOutlined, DeleteOutlined } from '@ant-design/icons'
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

interface ApiProductLinkApiProps {
  apiProduct: ApiProduct
}

interface LinkedService {
  id: string
  name: string
  type: string
  endpoint: string
  status: string
  createdAt: string
}

const mockLinkedServices: LinkedService[] = [
  {
    id: "1",
    name: "Payment Service",
    type: "REST API",
    endpoint: "https://api.example.com/payment",
    status: "active",
    createdAt: "2025-01-01T10:00:00Z"
  },
  {
    id: "2",
    name: "User Service",
    type: "REST API",
    endpoint: "https://api.example.com/user",
    status: "active",
    createdAt: "2025-01-02T11:00:00Z"
  }
]

export function ApiProductLinkApi({ apiProduct }: ApiProductLinkApiProps) {
  const [services, setServices] = useState<LinkedService[]>(mockLinkedServices)
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [form] = Form.useForm()

  const columns = [
    {
      title: '服务名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => <Tag color="blue">{type}</Tag>
    },
    {
      title: '端点',
      dataIndex: 'endpoint',
      key: 'endpoint',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'active' ? 'green' : 'red'}>
          {status === 'active' ? '活跃' : '非活跃'}
        </Tag>
      )
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleDateString()
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: LinkedService) => (
        <Space size="middle">
          <Button type="link" icon={<LinkOutlined />}>
            查看详情
          </Button>
          <Button 
            type="link" 
            danger 
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
          >
            解除关联
          </Button>
        </Space>
      ),
    },
  ]

  const handleAdd = () => {
    setIsModalVisible(true)
  }

  const handleDelete = (id: string) => {
    setServices(services.filter(service => service.id !== id))
  }

  const handleModalOk = () => {
    form.validateFields().then((values) => {
      const newService: LinkedService = {
        id: Date.now().toString(),
        name: values.name,
        type: values.type,
        endpoint: values.endpoint,
        status: 'active',
        createdAt: new Date().toISOString()
      }
      setServices([...services, newService])
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
          <h1 className="text-2xl font-bold mb-2">关联API</h1>
          <p className="text-gray-600">管理与此API产品关联的网关服务</p>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          关联服务
        </Button>
      </div>

      <Card>
        <Table 
          columns={columns} 
          dataSource={services}
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Modal
        title="关联新服务"
        open={isModalVisible}
        onOk={handleModalOk}
        onCancel={handleModalCancel}
        okText="关联"
        cancelText="取消"
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="服务名称"
            rules={[{ required: true, message: '请输入服务名称' }]}
          >
            <Input placeholder="请输入服务名称" />
          </Form.Item>
          <Form.Item
            name="type"
            label="服务类型"
            rules={[{ required: true, message: '请选择服务类型' }]}
          >
            <Select placeholder="请选择服务类型">
              <Select.Option value="REST API">REST API</Select.Option>
              <Select.Option value="GraphQL">GraphQL</Select.Option>
              <Select.Option value="gRPC">gRPC</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="endpoint"
            label="服务端点"
            rules={[{ required: true, message: '请输入服务端点' }]}
          >
            <Input placeholder="https://api.example.com/service" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
} 