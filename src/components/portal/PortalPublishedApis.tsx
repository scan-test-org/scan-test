import { useState, useEffect } from 'react'
import { Card, Table, Modal, Form, Button, Space, Select, message } from 'antd'
import { EyeOutlined, DeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import { Portal, ApiProduct } from '@/types'
import { apiProductApi } from '@/lib/api'
import { useNavigate } from 'react-router-dom'

interface PortalApiProductsProps {
  portal: Portal
}

export function PortalPublishedApis({ portal }: PortalApiProductsProps) {
  const navigate = useNavigate()
  const [apiProducts, setApiProducts] = useState<ApiProduct[]>([])
  const [apiProductsOptions, setApiProductsOptions] = useState<ApiProduct[]>([])
  const [isModalVisible, setIsModalVisible] = useState(false)

  const [form] = Form.useForm()
  useEffect(() => {
    if (portal.portalId) {
      fetchApiProducts()
    }
  }, [portal.portalId])

  const fetchApiProducts = () => {
    apiProductApi.getApiProducts({
      portalId: portal.portalId
    }).then((res) => {
      setApiProducts(res.data.content)
    })
  }

  useEffect(() => {
    if (isModalVisible) {
      apiProductApi.getApiProducts({}).then((res) => {
        // 过滤掉已发布在该门户里的api
        setApiProductsOptions(res.data.content.filter((api: ApiProduct) => !apiProducts.some((a: ApiProduct) => a.productId === api.productId)))
      })
    }
  }, [isModalVisible, apiProducts])


  const columns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'ID',
      dataIndex: 'productId',
      key: 'productId',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: ApiProduct) => (
        <Space size="middle">
          <Button
            onClick={() => {
              navigate(`/api-products/detail?productId=${record.productId}`)
            }}
            type="link" icon={<EyeOutlined />}>
            查看
          </Button>
          
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record.productId, record.name)}>
            移除
          </Button>
        </Space>
      ),
    },
  ]

  const handleDelete = (productId: string, productName: string) => {
    Modal.confirm({
      title: '确认移除',
      icon: <ExclamationCircleOutlined />,
      content: `确定要从门户中移除API产品 "${productName}" 吗？此操作不可恢复。`,
      okText: '确认移除',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
        apiProductApi.cancelPublishToPortal(productId, portal.portalId).then((res) => {
          message.success('移除成功')
          fetchApiProducts()
          setIsModalVisible(false)
        }).catch((error) => {
          message.error('移除失败')
        })
      },
    })
  }

  const handlePublish = async () => {
    const values = await form.validateFields()
    apiProductApi.publishToPortal(values.productId, portal.portalId).then((res) => {
      message.success('发布成功')
      fetchApiProducts()
      setIsModalVisible(false)
    }).catch((error) => {
      message.error('发布失败')
    })
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold mb-2">API管理</h1>
          <p className="text-gray-600">管理在此Portal中发布的API</p>
        </div>
        <Button type="primary" onClick={() => setIsModalVisible(true)}>
          发布新API
        </Button>
      </div>

      <Card>
        <Table 
          columns={columns} 
          dataSource={apiProducts}
          rowKey="id"
          pagination={false}
        />
      </Card>

      <Modal
        title="发布新API"
        open={isModalVisible}
        onOk={handlePublish}
        onCancel={() => setIsModalVisible(false)}
      >
        <Form
          layout="vertical"
          form={form}
          onFinish={handlePublish}
        >
          {/* 选择API */}
          <Form.Item
            label="API ID"
            name="productId"
            rules={[{ required: true, message: '请输入API ID' }]}
          >
            <Select
              options={apiProductsOptions.map((api) => ({
                label: api.name,
                value: api.productId
              }))}
            />
          </Form.Item>
        </Form>
      </Modal>

    </div>
  )
} 