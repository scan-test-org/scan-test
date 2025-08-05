import { useNavigate } from 'react-router-dom'
import { Card, Button, Table, Tag, Space, Switch, Modal, Form, Input, Select, message } from 'antd'
import { PlusOutlined, EyeOutlined, EditOutlined, DeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import type { ApiProduct } from '@/types/api-product';
import { apiProductApi, portalApi } from '@/lib/api';

interface ApiProductPortalProps {
  apiProduct: ApiProduct
}

interface Portal {
  portalId: string
  portalName: string
  autoApproveSubscription: boolean
  createdAt: string
}


export function ApiProductPortal({ apiProduct }: ApiProductPortalProps) {
  const [publishedPortals, setPublishedPortals] = useState<Portal[]>([])
  const [allPortals, setAllPortals] = useState<Portal[]>([])
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [portalLoading, setPortalLoading] = useState(false)

  const navigate = useNavigate()

  // 获取已发布的门户列表
  useEffect(() => {
    if (apiProduct.productId) {
      fetchPublishedPortals()
    }
  }, [apiProduct.productId])

  // 获取所有门户列表
  useEffect(() => {
    fetchAllPortals()
  }, [])

  const fetchPublishedPortals = async () => {
    setLoading(true)
    try {
      const res = await apiProductApi.getApiProductPublications(apiProduct.productId)
      setPublishedPortals(res.data.content || [])
    } catch (error) {
      console.error('获取已发布门户失败:', error)
      message.error('获取已发布门户失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchAllPortals = async () => {
    setPortalLoading(true)
    try {
      const res = await portalApi.getPortals()
      setAllPortals(res.data.content || [])
    } catch (error) {
      console.error('获取门户列表失败:', error)
      message.error('获取门户列表失败')
    } finally {
      setPortalLoading(false)
    }
  }

  const columns = [
    {
      title: '门户名称',
      dataIndex: 'portalName',
      key: 'portalName',
    },
    {
      title: '门户ID',
      dataIndex: 'portalId',
      key: 'portalId',
    },
    {
      title: '自动审批订阅',
      dataIndex: 'autoApproveSubscription',
      key: 'autoApproveSubscription',
      render: (autoApproveSubscription: boolean) => (
        <Switch checked={autoApproveSubscription} disabled />
      )
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Portal) => (
        <Space size="middle">
          <Button onClick={() => {
            navigate(`/portals/detail?id=${record.portalId}`)
          }} type="link" icon={<EyeOutlined />}>
            查看
          </Button>
         
          <Button 
            type="link" 
            danger 
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.portalId, record.portalName)}
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

  const handleDelete = (portalId: string, portalName: string) => {
    Modal.confirm({
      title: '确认移除',
      icon: <ExclamationCircleOutlined />,
      content: `确定要从API产品中移除门户 "${portalName}" 吗？此操作不可恢复。`,
      okText: '确认移除',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
        apiProductApi.cancelPublishToPortal(apiProduct.productId, portalId).then((res) => {
          message.success('移除成功')
          fetchPublishedPortals()
        }).catch((error) => {
          console.error('移除失败:', error)
          message.error('移除失败')
        })
      },
    })
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      const { portalId } = values
      
      await apiProductApi.publishToPortal(apiProduct.productId, portalId)
      message.success('发布成功')
      setIsModalVisible(false)
      form.resetFields()
      // 重新获取已发布的门户列表
      fetchPublishedPortals()
    } catch (error) {
      console.error('发布失败:', error)
      message.error('发布失败')
    }
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
        {publishedPortals.length === 0 && !loading ? (
          <div className="text-center py-8 text-gray-500">
            <p>暂未发布到任何门户</p>
          </div>
        ) : (
          <Table 
            columns={columns} 
            dataSource={publishedPortals}
            rowKey="portalId"
            loading={loading}
            pagination={false}
          />
        )}
      </Card>

      <Modal
        title="发布到门户"
        open={isModalVisible}
        onOk={handleModalOk}
        onCancel={handleModalCancel}
        okText="发布"
        cancelText="取消"
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="portalId"
            label="选择门户"
            rules={[{ required: true, message: '请选择门户' }]}
          >
            <Select 
              placeholder="请选择要发布到的门户" 
              loading={portalLoading}
              showSearch
              filterOption={(input, option) =>
                (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())
              }
              optionLabelProp="label"
            >
              {allPortals
                .filter(portal => !publishedPortals.some(published => published.portalId === portal.portalId))
                .map(portal => (
                  <Select.Option 
                    key={portal.portalId} 
                    value={portal.portalId}
                    label={portal.name}
                  >
                    <div>
                      <div className="font-medium">{portal.name}</div>
                      <div className="text-sm text-gray-500">
                        {portal.portalId} - {portal.description || '无描述'}
                      </div>
                    </div>
                  </Select.Option>
                ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
} 