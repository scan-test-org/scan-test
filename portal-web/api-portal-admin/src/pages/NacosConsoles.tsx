import { useState, useEffect } from 'react'
import { Tooltip, Button, Table, Modal, Form, Input, Space, message, Popconfirm } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons'
import { nacosApi } from '@/lib/api'

interface NacosInstance {
  nacosId: string
  nacosName: string
  serverUrl: string
  namespace: string
  username: string
  description: string
  adminId: string
}

interface NacosFormData {
  nacosName: string
  serverUrl: string
  namespace: string
  username: string
  password: string
  description: string
}

export default function NacosConsoles() {
  const [nacosInstances, setNacosInstances] = useState<NacosInstance[]>([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingNacos, setEditingNacos] = useState<NacosInstance | null>(null)
  const [form] = Form.useForm()
  
  // 分页状态
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [total, setTotal] = useState(0)

  useEffect(() => {
    fetchNacosInstances()
  }, [currentPage, pageSize])

  const fetchNacosInstances = async () => {
    setLoading(true)
    try {
      const response = await nacosApi.getNacos({
        page: currentPage,
        size: pageSize
      })
      setNacosInstances(response.data.content || [])
      setTotal(response.data.totalElements || 0)
    } catch (error) {
      console.error('获取Nacos实例列表失败:', error)
      message.error('获取Nacos实例列表失败')
    } finally {
      setLoading(false)
    }
  }

  const handlePageChange = (page: number, size?: number) => {
    setCurrentPage(page)
    if (size) {
      setPageSize(size)
    }
  }


  const handleEdit = (record: NacosInstance) => {
    setEditingNacos(record)
    form.setFieldsValue({
      nacosName: record.nacosName,
      serverUrl: record.serverUrl,
      namespace: record.namespace,
      username: record.username,
      description: record.description
    })
    setModalVisible(true)
  }

  const handleDelete = async (nacosId: string, nacosName: string) => {
    try {
      await nacosApi.deleteNacos(nacosId)
      message.success(`成功删除Nacos实例: ${nacosName}`)
      fetchNacosInstances()
    } catch (error) {
      console.error('删除失败:', error)
      message.error('删除失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      
      if (editingNacos) {
        // 编辑模式
        await nacosApi.updateNacos(editingNacos.nacosId, values)
        message.success('更新成功')
      } else {
        // 创建模式
        await nacosApi.createNacos(values)
        message.success('创建成功')
      }
      
      setModalVisible(false)
      form.resetFields()
      fetchNacosInstances()
    } catch (error) {
      console.error('操作失败:', error)
      message.error('操作失败')
    }
  }

  const handleModalCancel = () => {
    setModalVisible(false)
    setEditingNacos(null)
    form.resetFields()
  }

  const columns = [
    {
      title: '实例名称',
      dataIndex: 'nacosName',
      key: 'nacosName',
    },
    {
      title: '服务器地址',
      dataIndex: 'serverUrl',
      key: 'serverUrl',
    },
    {
      title: '命名空间',
      dataIndex: 'namespace',
      key: 'namespace',
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      // render: (description: string) => {
      //   return <Tooltip title={description}>{description || '-'}</Tooltip>
      // },
      ellipsis: true,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: any, record: NacosInstance) => (
        <div className='flex items-center'>
          <Button type="link" onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button className='ml-2' type="link" danger onClick={() => handleDelete(record.nacosId, record.nacosName)}>
            删除
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Nacos实例管理</h1>
          <p className="text-gray-500 mt-2">
          管理Nacos配置中心实例
          </p>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalVisible(true)}>
        创建实例
        </Button>
      </div>

     

      <div className="bg-white rounded-lg">
        <Table
          columns={columns}
          dataSource={nacosInstances}
          rowKey="nacosId"
          loading={loading}
          pagination={{
            current: currentPage,
            pageSize: pageSize,
            total: total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
            onChange: handlePageChange,
            onShowSizeChange: handlePageChange,
          }}
        />
      </div>

      <Modal
        title={editingNacos ? '编辑Nacos实例' : '创建Nacos实例'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={handleModalCancel}
        okText={editingNacos ? '更新' : '创建'}
        cancelText="取消"
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            namespace: 'public'
          }}
        >
          <Form.Item
            name="nacosName"
            label="实例名称"
            rules={[{ required: true, message: '请输入实例名称' }]}
          >
            <Input placeholder="请输入Nacos实例名称" />
          </Form.Item>

          <Form.Item
            name="serverUrl"
            label="服务器地址"
            rules={[{ required: true, message: '请输入服务器地址' }]}
          >
            <Input placeholder="例如: http://localhost:8848" />
          </Form.Item>

          <Form.Item
            name="namespace"
            label="命名空间"
            rules={[{ required: true, message: '请输入命名空间' }]}
          >
            <Input placeholder="例如: public" />
          </Form.Item>

          <Form.Item
            name="username"
            label="用户名"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input placeholder="请输入Nacos用户名" />
          </Form.Item>

          {!editingNacos && (
            <Form.Item
              name="password"
              label="密码"
              rules={[{ required: true, message: '请输入密码' }]}
            >
              <Input.Password placeholder="请输入Nacos密码" />
            </Form.Item>
          )}

          <Form.Item
            name="description"
            label="描述"
          >
            <Input.TextArea
              rows={3}
              placeholder="请输入实例描述（可选）"
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
