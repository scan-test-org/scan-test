import { Card, Button, Table, Tag, Space, Modal, Form, Input, Select, message } from 'antd'
import { PlusOutlined, LinkOutlined, DeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import type { ApiProduct } from '@/types/api-product'
import { apiProductApi, gatewayApi } from '@/lib/api'
import { getServiceName } from '@/lib/utils'

interface ApiProductLinkApiProps {
  apiProduct: ApiProduct
  handleRefresh: () => void
}

interface RestAPIItem {
  apiId: string
  apiName: string
}

interface HigressMCPItem {
  mcpServerName: string
  fromGatewayType: 'HIGRESS'
}

interface APIGAIMCPItem {
  mcpServerName: string
  fromGatewayType: 'APIG_AI'
  mcpRouteId: string
  apiId: string
}

type ApiItem = RestAPIItem | HigressMCPItem | APIGAIMCPItem;

interface LinkedService {
  productId: string
  gatewayId: string
  sourceType: 'GATEWAY' | 'NACOS'
  apigRefConfig?: RestAPIItem | APIGAIMCPItem
  higressRefConfig?: HigressMCPItem
}

interface Gateway {
  gatewayId: string
  gatewayName: string
  gatewayType: 'APIG_API' | 'HIGRESS' | 'APIG_AI'
  createAt: string
}



export function ApiProductLinkApi({ apiProduct, handleRefresh }: ApiProductLinkApiProps) {
  const [linkedService, setLinkedService] = useState<LinkedService | null>(null)
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

  useEffect(() => {
    if (apiProduct.productId) {
      fetchLinkedService()
    }
  }, [apiProduct.productId])

  const fetchLinkedService = async () => {
    try {
      const res = await apiProductApi.getApiProductRef(apiProduct.productId)
      setLinkedService(res.data || null)
    } catch (error) {
      console.error('获取关联服务失败:', error)
      setLinkedService(null)
    }
  }

  const fetchGateways = async () => {
    setGatewayLoading(true)
    try {
      const res = await gatewayApi.getGateways()
      const result = apiProduct.type === 'REST_API' ?
       res.data?.content?.filter?.((item: Gateway) => item.gatewayType === 'APIG_API') :
       res.data?.content?.filter?.((item: Gateway) => item.gatewayType === 'HIGRESS' || item.gatewayType === 'APIG_AI')
      setGateways(result || [])
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
        // APIG_API类型：获取REST API列表
        const restRes = await gatewayApi.getGatewayRestApis(gatewayId)
        const restApis = (restRes.data?.content || []).map((api: any) => ({
          apiId: api.apiId,
          apiName: api.apiName,
          type: 'REST API'
        }))
        setApiList(restApis)
      } else if (gateway.gatewayType === 'HIGRESS') {
        // HIGRESS类型：获取MCP Server列表
        const res = await gatewayApi.getGatewayMcpServers(gatewayId)
        const mcpServers = (res.data?.content || []).map((api: any) => ({
          mcpServerName: api.mcpServerName,
          fromGatewayType: 'HIGRESS' as const,
          type: 'MCP Server'
        }))
        setApiList(mcpServers)
      } else if (gateway.gatewayType === 'APIG_AI') {
        // APIG_AI类型：获取MCP Server列表
        const res = await gatewayApi.getGatewayMcpServers(gatewayId)
        const mcpServers = (res.data?.content || []).map((api: any) => ({
          mcpServerName: api.mcpServerName,
          fromGatewayType: 'APIG_AI' as const,
          mcpRouteId: api.mcpRouteId,
          apiId: api.apiId,
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

  const renderServiceDetails = () => {
    if (!linkedService) {
      return (
        <div className="text-center py-8 text-gray-500">
          <p>暂未关联任何服务</p>
        </div>
      )
    }

    const getServiceType = () => {
      
      if (linkedService.apigRefConfig) {
        if ('apiName' in linkedService.apigRefConfig && linkedService.apigRefConfig.apiName) {
          return 'REST API'
        }
        return 'MCP Server (APIG_AI)'
      }
      if (linkedService.higressRefConfig) {
        return 'MCP Server (HIGRESS)'
      }
      return '未知类型'
    }

    return (
      <div className="space-y-4">
        <div className="flex justify-between items-start">
          <div>
            <h3 className="text-lg font-medium">{getServiceName(linkedService)}</h3>
            <p className="text-sm text-gray-500">{getServiceType()}</p>
          </div>
          <Button 
            type="primary" 
            danger 
            icon={<DeleteOutlined />}
            onClick={handleDelete}
          >
            解除关联
          </Button>
        </div>
        
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <span className="font-medium">网关ID:</span>
            <span className="ml-2">{linkedService.gatewayId}</span>
          </div>
          <div>
            <span className="font-medium">来源类型:</span>
            <span className="ml-2">{linkedService.sourceType}</span>
          </div>
          
        </div>
      </div>
    )
  }

  const handleAdd = () => {
    if (linkedService) {
      Modal.confirm({
        title: '重新关联',
        content: '重新关联将删除现有的关联关系，确定继续吗？',
        onOk: () => {
          apiProductApi.deleteApiProductRef(apiProduct.productId).then(() => {
            setLinkedService(null)
            setIsModalVisible(true)
          }).catch((err: any) => {
            message.error('删除现有关联失败')
          })
        }
      })
    } else {
      setIsModalVisible(true)
    }
  }

  const handleDelete = () => {
    Modal.confirm({
      title: '确认解除关联',
      icon: <ExclamationCircleOutlined />,
      content: '确定要解除服务关联吗？此操作不可恢复。',
      okText: '确认解除',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => {
        apiProductApi.deleteApiProductRef(apiProduct.productId).then((res: any) => {
          message.success('解除关联成功')
          fetchLinkedService()
        }).catch((err: any) => {
          message.error('解除关联失败')
        })
      }
    })
  }

  const handleModalOk = () => {
    form.validateFields().then((values) => {
      const { sourceType, gatewayId, apiId } = values
      const selectedApi = apiList.find(item => {
        if ('apiId' in item) {
          return item.apiId === apiId
        } else if ('mcpServerName' in item) {
          return item.mcpServerName === apiId
        }
        return false
      })
      
      const newService: LinkedService = {
        gatewayId,
        sourceType,
        productId: apiProduct.productId,
        apigRefConfig: selectedApi && 'apiId' in selectedApi ? selectedApi as RestAPIItem | APIGAIMCPItem : undefined,
        higressRefConfig: selectedApi && 'mcpServerName' in selectedApi && 'fromGatewayType' in selectedApi && selectedApi.fromGatewayType === 'HIGRESS' ? selectedApi as HigressMCPItem : undefined,
      }
      
      apiProductApi.createApiProductRef(apiProduct.productId, newService).then((res: any) => {
        message.success('关联成功')
        setIsModalVisible(false)
        fetchLinkedService()
        form.resetFields()
        setSelectedGateway(null)
        setApiList([])
      }).catch((err: any) => {
        message.error('关联失败')
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
          <h1 className="text-2xl font-bold mb-2">关联服务</h1>
          <p className="text-gray-600">
            {apiProduct.type === 'REST_API' 
              ? '关联REST API服务到此产品' 
              : '关联MCP Server服务到此产品'
            }
          </p>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          {linkedService ? '重新关联' : '关联服务'}
        </Button>
      </div>

      <Card>
        {renderServiceDetails()}
      </Card>

      <Modal
        title={linkedService ? '重新关联服务' : '关联新服务'}
        open={isModalVisible}
        onOk={handleModalOk}
        onCancel={handleModalCancel}
        okText="关联"
        cancelText="取消"
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="sourceType"
            label="来源类型"
            initialValue="GATEWAY"
            rules={[{ required: true, message: '请选择来源类型' }]}
          >
            <Select placeholder="请选择来源类型">
              <Select.Option value="GATEWAY">网关</Select.Option>
              <Select.Option value="NACOS">Nacos</Select.Option>
            </Select>
          </Form.Item>

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
              label={apiProduct.type === 'REST_API' ? '选择REST API' : '选择MCP Server'}
              rules={[{ required: true, message: apiProduct.type === 'REST_API' ? '请选择REST API' : '请选择MCP Server' }]}
            >
                              <Select 
                  placeholder={apiProduct.type === 'REST_API' ? '请选择REST API' : '请选择MCP Server'} 
                  loading={apiLoading}
                  showSearch
                  filterOption={(input, option) =>
                    (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())
                  }
                  optionLabelProp="label"
                >
                {apiList.map((api: any) => (
                  <Select.Option 
                    key={api.apiId || api.mcpServerName} 
                    value={api.apiId || api.mcpServerName}
                    label={api.apiName || api.mcpServerName}
                  >
                    <div>
                      <div className="font-medium">{api.apiName || api.mcpServerName}</div>
                      <div className="text-sm text-gray-500">
                        {api.type} - {api.apiId || api.mcpServerName}
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