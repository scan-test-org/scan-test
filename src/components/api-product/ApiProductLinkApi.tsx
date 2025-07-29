import { Card, Button, Table, Tag, Space, Modal, Form, Input, Select, message } from 'antd'
import { PlusOutlined, LinkOutlined, DeleteOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import type { ApiProduct } from '@/types/api-product'
import api from '@/lib/api'

interface ApiProductLinkApiProps {
  apiProduct: ApiProduct
  handleRefresh: () => void
}

interface LinkedService {
  apiId?: string
  gatewayId: string
  routes?: {
    routeId?: string
    name: string
  }[]
  name: string
  createdAt: string
}

interface Gateway {
  gatewayId: string
  gatewayName: string
  gatewayType: 'APIG_API' | 'HIGRESS' | 'APIG_AI'
  createAt: string
}

interface ApiItem {
  apiId?: string
  id?: string
  name: string
  type: string
  endpoint?: string
}


export function ApiProductLinkApi({ apiProduct, handleRefresh }: ApiProductLinkApiProps) {
  const [services, setServices] = useState<LinkedService[]>([])
  const [isModalVisible, setIsModalVisible] = useState(false)
  const [form] = Form.useForm()
  const [gateways, setGateways] = useState<Gateway[]>([])
  const [gatewayLoading, setGatewayLoading] = useState(false)
  const [selectedGateway, setSelectedGateway] = useState<Gateway | null>(null)
  const [apiList, setApiList] = useState<ApiItem[]>([])
  const [apiLoading, setApiLoading] = useState(false)

  useEffect(() => {
    fetchGateways()
  }, [])

  const fetchGateways = async () => {
    setGatewayLoading(true)
    try {
      const res = await api.get('/gateways')
      setGateways(res.data?.content || [])
    } catch (error) {
      console.error('获取网关列表失败:', error)
    } finally {
      setGatewayLoading(false)
    }
  }

  const handleGatewayChange = async (gatewayId: string) => {
    const gateway = gateways.find(g => g.gatewayId === gatewayId)
    setSelectedGateway(gateway || null)
    
    if (!gateway) return

    console.log('gatewayId', gatewayId);
    
    setApiLoading(true)
    try {
      if (gateway.gatewayType === 'APIG_API') {
        // APIG_API类型：同时获取REST API和MCP Server列表
        const restRes = await api.get(`/gateways/${gatewayId}/rest-apis`)
        const restApis = (restRes.data?.content || []).map((api: any) => ({
          ...api,
          type: 'REST API'
        }))
        setApiList([...restApis])
      } else if (gateway.gatewayType === 'HIGRESS' || gateway.gatewayType === 'APIG_AI') {
        // HIGRESS类型：只获取MCP Server列表
        const res = await api.get(`/gateways/${gatewayId}/mcp-servers`)
        const mcpServers = (res.data?.content || []).map((api: any) => ({
          ...api,
          type: 'MCP Server'
        }))
        setApiList(mcpServers)
      }
    } catch (error) {
      console.error('获取API列表失败:', error)
    } finally {
      setApiLoading(false)
    }
  }

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
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (date: string) => new Date(date).toLocaleDateString()
    },
    {
      title: '操作',
      key: 'action',
      width: 300,
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
    Modal.confirm({
      title: '解除关联',
      content: '确定要解除关联吗？',
      onOk: () => {
        api.delete(`/products/${apiProduct.productId}/ref}`, {
          // product
        }).then(res => {
          message.success('解除关联成功')
          handleRefresh()
        }).catch(err => {
          message.error('解除关联失败')
        })
      }
    })
  }

  const handleModalOk = () => {
    form.validateFields().then((values) => {
      const newService: LinkedService = {
        
      }
      api.post(`/products/${apiProduct.productId}/ref`, newService).then(res => {
        message.success('关联成功')
        setIsModalVisible(false)
        handleRefresh()
      }).catch(err => {
        message.error('关联失败')
      }).finally(() => {
        form.resetFields()
        setSelectedGateway(null)
        setApiList([])
      })
    })
  }

  const handleModalCancel = () => {
    setIsModalVisible(false)
    form.resetFields()
    setSelectedGateway(null)
    setApiList([])
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
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="gatewayId"
            label="网关实例"
            rules={[{ required: true, message: '请选择网关' }]}
          >
            <Select 
              placeholder="请选择网关实例" 
              loading={gatewayLoading}
              showSearch
              filterOption={(input, option) =>
                (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())
              }
              onChange={handleGatewayChange}
              optionLabelProp="label"
            >
              {gateways.map(gateway => (
                <Select.Option 
                  key={gateway.gatewayId} 
                  value={gateway.gatewayId}
                  label={gateway.gatewayName}
                >
                  <div>
                    <div className="font-medium">{gateway.gatewayName}</div>
                    <div className="text-sm text-gray-500">
                      {gateway.gatewayId} - {gateway.gatewayType}
                    </div>
                  </div>
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          
          {selectedGateway && (
            <Form.Item
              name="apiId"
              label="选择API"
              rules={[{ required: true, message: '请选择API' }]}
            >
              <Select 
                placeholder="请选择API" 
                loading={apiLoading}
                showSearch
                filterOption={(input, option) =>
                  (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())
                }
                optionLabelProp="label"
              >
                {apiList.map(api => (
                  <Select.Option 
                    key={api.apiId || api.id} 
                    value={api.apiId || api.id}
                    label={api.name}
                  >
                    <div>
                      <div className="font-medium">{api.name}</div>
                      <div className="text-sm text-gray-500">
                        {api.type} - {api.apiId || api.id}
                      </div>
                    </div>
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
          )}
        </Form>
      </Modal>
    </div>
  )
} 