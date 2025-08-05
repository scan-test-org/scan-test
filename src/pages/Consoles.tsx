import { useState, useEffect, useCallback } from 'react'
import { Button, Table, message, Modal } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { gatewayApi } from '@/lib/api'
import ImportGatewayModal from '@/components/console/ImportGatewayModal'

interface Gateway {
  gatewayId: string
  gatewayName: string
  gatewayType: 'APIG_API' | 'HIGRESS' | 'APIG_AI'
  createAt: string
}

export default function Consoles() {
  const [gateways, setGateways] = useState<Gateway[]>([])
  const [importVisible, setImportVisible] = useState(false)
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  const fetchGatewaysConsoles = useCallback(async (page = 0, size = 10) => {
    setLoading(true)
    try {
      const res = await gatewayApi.getGateways({ page, size })
      setGateways(res.data?.content || [])
      setPagination({
        current: page + 1,
        pageSize: size,
        total: res.data?.totalElements || 0,
      })
    } catch (error) {
      message.error('获取网关列表失败')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchGatewaysConsoles(0, 10)
  }, [fetchGatewaysConsoles])

  // 处理导入成功
  const handleImportSuccess = () => {
    fetchGatewaysConsoles(pagination.current - 1, pagination.pageSize)
  }

  // 处理分页变化
  const handlePaginationChange = (page: number, pageSize: number) => {
    fetchGatewaysConsoles(page - 1, pageSize)
  }

  const handleDeleteGateway = async (gatewayId: string) => {
    Modal.confirm({
      title: '确认删除',
      content: '确定要删除该网关吗？',
      onOk: async () => {
        try {
          await gatewayApi.deleteGateway(gatewayId)
          message.success('删除成功')
          fetchGatewaysConsoles(pagination.current - 1, pagination.pageSize)
        } catch (error) {
          message.error('删除失败')
        }
      },
    })
  }

  const columns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: Gateway) => (
        <div>
          <div className="font-medium">{name}</div>
          <div className="text-sm text-gray-500">
            {record.gatewayId}
          </div>
          
        </div>
      ),
    },
    {
      title: '类型',
      dataIndex: 'gatewayType',
      key: 'gatewayType',
      
    },
    
    {
      title: '创建时间',
      dataIndex: 'createAt',
      key: 'createAt',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Gateway) => (
        <Button danger onClick={() => handleDeleteGateway(record.gatewayId)}>删除</Button>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">网关实例</h1>
          <p className="text-gray-500 mt-2">
            管理和配置您的网关实例
          </p>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setImportVisible(true)}>
          导入网关实例
        </Button>
      </div>

      <div className="bg-white rounded-lg border">
        <Table
          columns={columns}
          dataSource={gateways}
          rowKey="id"
          loading={loading}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            onChange: handlePaginationChange,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条/共 ${total} 条`,
            position: ['bottomRight'],
          }}
        />
      </div>

      <ImportGatewayModal
        visible={importVisible}
        onCancel={() => setImportVisible(false)}
        onSuccess={handleImportSuccess}
      />
    </div>
  )
}
