import { Card, Table, Badge, Button, Space, Avatar, message, Modal } from 'antd'
import { EditOutlined, DeleteOutlined, ExclamationCircleOutlined, EyeOutlined } from '@ant-design/icons'
import { useEffect, useState } from 'react'
import { Portal, Developer } from '@/types'
import { portalApi } from '@/lib/api'

interface PortalDevelopersProps {
  portal: Portal
}

interface Consumer {
  consumerId: string
  name: string
  description: string
  status: string
  createdAt: string
}

export function PortalDevelopers({ portal }: PortalDevelopersProps) {
  const [developers, setDevelopers] = useState<Developer[]>([])
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number, range: [number, number]) => 
      `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
  })

  // Consumer相关状态
  const [consumers, setConsumers] = useState<Consumer[]>([])
  const [consumerModalVisible, setConsumerModalVisible] = useState(false)
  const [currentDeveloper, setCurrentDeveloper] = useState<Developer | null>(null)
  const [consumerPagination, setConsumerPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number, range: [number, number]) => 
      `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
  })

  useEffect(() => {
    fetchDevelopers()
  }, [portal.portalId, pagination.current, pagination.pageSize])

  const fetchDevelopers = () => {
    portalApi.getDeveloperList(portal.portalId, {
      page: pagination.current - 1, // 后端从0开始
      size: pagination.pageSize
    }).then((res) => {
      setDevelopers(res.data.content)
      setPagination(prev => ({
        ...prev,
        total: res.data.totalElements || 0
      }))
    })
  }

  const handleUpdateDeveloperStatus = (developerId: string, status: string) => {
    portalApi.updateDeveloperStatus(portal.portalId, developerId, status).then((res) => {
      message.success('审批成功')
      fetchDevelopers()
    }).catch((err) => {
      message.error('审批失败')
    })
  }

  const handleTableChange = (paginationInfo: any) => {
    setPagination(prev => ({
      ...prev,
      current: paginationInfo.current,
      pageSize: paginationInfo.pageSize
    }))
  }

  const handleDeleteDeveloper = (developerId: string, username: string) => {
    Modal.confirm({
      title: '确认删除',
      icon: <ExclamationCircleOutlined />,
      content: `确定要删除开发者 "${username}" 吗？此操作不可恢复。`,
      okText: '确认删除',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
        // portalApi.deleteDeveloper(portal.portalId, developerId).then((res) => {
        //   message.success('删除成功')
        //   fetchDevelopers()
        // }).catch((err) => {
        //   message.error('删除失败')
        // })
        message.info('删除功能待实现')
      },
    })
  }

  // Consumer相关函数
  const handleViewConsumers = (developer: Developer) => {
    setCurrentDeveloper(developer)
    setConsumerModalVisible(true)
    setConsumerPagination(prev => ({ ...prev, current: 1 }))
    fetchConsumers(developer.developerId, 1, consumerPagination.pageSize)
  }

  const fetchConsumers = (developerId: string, page: number, size: number) => {
    portalApi.getConsumerList(portal.portalId, developerId, { page: page - 1, size }).then((res) => {
      setConsumers(res.data.content || [])
      setConsumerPagination(prev => ({
        ...prev,
        total: res.data.totalElements || 0
      }))
    }).then((res: any) => {
      setConsumers(res.data.content || [])
      setConsumerPagination(prev => ({
        ...prev,
        total: res.data.totalElements || 0
      }))
    })
  }

  const handleConsumerTableChange = (paginationInfo: any) => {
    if (currentDeveloper) {
      setConsumerPagination(prev => ({
        ...prev,
        current: paginationInfo.current,
        pageSize: paginationInfo.pageSize
      }))
      fetchConsumers(currentDeveloper.developerId, paginationInfo.current, paginationInfo.pageSize)
    }
  }

  const handleConsumerStatusUpdate = (consumerId: string) => {
    if (currentDeveloper) {
      portalApi.approveConsumer(consumerId).then((res) => {
        message.success('审批成功')
        fetchConsumers(currentDeveloper.developerId, consumerPagination.current, consumerPagination.pageSize)
      }).catch((err) => {
        message.error('审批失败')
      })
    }
  }


  const columns = [
    {
      title: '开发者名称/ID',
      dataIndex: 'username',
      key: 'username',
      fixed: 'left',
      width: 300,
      render: (username: string, record: Developer) => (
        <div className="ml-2">
          <div className="font-medium">{username}</div>
          <div className="text-sm text-gray-500">{record.developerId}</div>
        </div>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Badge status={status === 'APPROVED' ? 'success' : 'default'} text={status} />
      )
    },
    
    {
      title: '创建时间',
      dataIndex: 'createAt',
      key: 'createAt',
      render: (date: string) => new Date(date).toLocaleDateString()
    },
  
    {
      title: '操作',
      key: 'action',
      fixed: 'right',
      width: 300,
      render: (_: any, record: Developer) => (
        <Space size="middle">
          <Button onClick={() => handleViewConsumers(record)} type="link" icon={<EyeOutlined />}>
            查看Consumer
          </Button>
          {
            !portal.portalSettingConfig.autoApproveDevelopers && (
              record.status === 'APPROVED' ? (
                <Button onClick={() => handleUpdateDeveloperStatus(record.developerId, 'PENDING')} type="link" icon={<EditOutlined />}>
                  取消授权
                </Button>
              ) : (
                <Button onClick={() => handleUpdateDeveloperStatus(record.developerId, 'APPROVED')} type="link" icon={<EditOutlined />}>
                  审批通过
                </Button>
              )
            )
          }
          <Button onClick={() => handleDeleteDeveloper(record.developerId, record.username)} type="link" danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Space>
      ),
    },
  ]

  // Consumer表格列定义
  const consumerColumns = [
    {
      title: 'Consumer名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
    },
    {
      title: 'Consumer ID',
      dataIndex: 'consumerId',
      key: 'consumerId',
      width: 200,
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: string) => (
        <Badge status={status === 'APPROVED' ? 'success' : 'default'} text={status} />
      )
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 150,
      render: (date: string) => new Date(date).toLocaleDateString()
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_: any, record: Consumer) => record.status !== 'APPROVED' && (
        <Button 
          onClick={() => handleConsumerStatusUpdate(record.consumerId)} 
          type="link" 
          icon={<EditOutlined />}
        >
          审批
        </Button>
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
      </div>

      <Card>
        {/* <div className="mb-4">
          <Input
            placeholder="搜索开发者..."
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            style={{ width: 300 }}
          />
        </div> */}
        <Table 
          columns={columns} 
          dataSource={developers}
          rowKey="developerId"
          pagination={pagination}
          onChange={handleTableChange}
          scroll={{ 
            y: 'calc(100vh - 400px)',
            x: 'max-content'
           }}
        />
      </Card>

      {/* Consumer弹窗 */}
      <Modal
        title={`查看Consumer - ${currentDeveloper?.username || ''}`}
        open={consumerModalVisible}
        onCancel={() => setConsumerModalVisible(false)}
        footer={null}
        width={1000}
        destroyOnClose
      >
        <Table 
          columns={consumerColumns} 
          dataSource={consumers}
          rowKey="consumerId"
          pagination={consumerPagination}
          onChange={handleConsumerTableChange}
          scroll={{ y: 'calc(100vh - 400px)' }}
        />
      </Modal>

    </div>
  )
} 