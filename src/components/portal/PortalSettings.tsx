import { Card, Form, Input, Select, Switch, Button, Divider, Space, Tag, Table, Modal, message } from 'antd'
import { SaveOutlined, ReloadOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons'
import { useState } from 'react'
import { Portal } from '@/types'
import { portalApi } from '@/lib/api'

interface PortalSettingsProps {
  portal: Portal
  onRefresh?: () => void
}

export function PortalSettings({ portal, onRefresh }: PortalSettingsProps) {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [domainModalVisible, setDomainModalVisible] = useState(false)
  const [domainForm] = Form.useForm()
  const [domainLoading, setDomainLoading] = useState(false)

  const handleSave = async () => {
    setLoading(true)
    try {
      const values = await form.validateFields()
      console.log('保存设置:', values)
      // 这里可以调用API保存设置
      setTimeout(() => {
        setLoading(false)
      }, 1000)
    } catch (error) {
      setLoading(false)
    }
  }

  const handleReset = () => {
    form.resetFields()
  }

  // 域名管理相关函数
  const handleAddDomain = () => {
    setDomainModalVisible(true)
    domainForm.resetFields()
  }

  const handleDomainModalOk = async () => {
    try {
      setDomainLoading(true)
      const values = await domainForm.validateFields()
      await portalApi.bindDomain(portal.portalId, {
        domain: values.domain,
        protocol: values.protocol,
        type: 'CUSTOM'
      })
      message.success('域名绑定成功')
      setDomainModalVisible(false)
      onRefresh?.()
    } catch (error) {
      console.error('绑定域名失败:', error)
      message.error('绑定域名失败')
    } finally {
      setDomainLoading(false)
    }
  }

  const handleDomainModalCancel = () => {
    setDomainModalVisible(false)
    domainForm.resetFields()
  }

  const handleDeleteDomain = async (domain: string) => {
    try {
      await portalApi.unbindDomain(portal.portalId, domain)
      message.success('域名解绑成功')
      onRefresh?.()
    } catch (error) {
      console.error('解绑域名失败:', error)
      message.error('解绑域名失败')
    }
  }

  // 域名表格列定义
  const domainColumns = [
    {
      title: '域名',
      dataIndex: 'domain',
      key: 'domain',
    },
    {
      title: '协议',
      dataIndex: 'protocol',
      key: 'protocol',
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => (
        <Tag color={type === 'DEFAULT' ? 'blue' : 'green'}>
          {type === 'DEFAULT' ? '默认域名' : '自定义域名'}
        </Tag>
      )
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record: any) => (
        <Space>
          {record.type === 'CUSTOM' && (
            <Button 
              type="link" 
              danger 
              icon={<DeleteOutlined />}
              onClick={() => handleDeleteDomain(record.domain)}
            >
              解绑
            </Button>
          )}
        </Space>
      )
    }
  ]

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold mb-2">设置</h1>
          <p className="text-gray-600">配置Portal的基本设置和高级选项</p>
        </div>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={handleReset}>
            重置
          </Button>
          <Button type="primary" icon={<SaveOutlined />} loading={loading} onClick={handleSave}>
            保存设置
          </Button>
        </Space>
      </div>

      <Form
        form={form}
        layout="vertical"
        initialValues={{
          name: portal.name,
          title: portal.title,
          description: portal.description,
          domain: portal.portalDomainConfig[0]?.domain || '',
          builtinAuthEnabled: portal.portalSettingConfig.builtinAuthEnabled,
          oidcAuthEnabled: portal.portalSettingConfig.oidcAuthEnabled,
          autoApproveDevelopers: portal.portalSettingConfig.autoApproveDevelopers,
          autoApproveSubscriptions: portal.portalSettingConfig.autoApproveSubscriptions,
          frontendRedirectUrl: portal.portalSettingConfig.frontendRedirectUrl,
          enableAnalytics: true,
          enableNotifications: true,
          enableAuditLog: true,
          enableRateLimiting: true,
          enableCaching: false,
          enableCompression: true,
          enableSSL: true,
          enableCORS: true
        }}
      >
        <Card title="基本信息" className="mb-6">
          <div className="grid grid-cols-2 gap-6">
            <Form.Item
              name="name"
              label="名称"
              rules={[{ required: true, message: '请输入Portal ID' }]}
            >
              <Input placeholder="请输入Portal ID" />
            </Form.Item>
            <Form.Item
              name="title"
              label="标题"
              rules={[{ required: true, message: '请输入Portal标题' }]}
            >
              <Input placeholder="请输入Portal标题" />
            </Form.Item>
            <Form.Item
              name="description"
              label="描述"
              className="col-span-2"
            >
              <Input.TextArea rows={3} placeholder="请输入Portal描述" />
            </Form.Item>
            {/* <Form.Item
              name="domain"
              label="Portal域名"
              rules={[{ required: true, message: '请输入Portal域名' }]}
              className="col-span-2"
            >
              <Input placeholder="portal.example.com" />
            </Form.Item> */}
          </div>
        </Card>

        <Card title="认证设置" className="mb-6">
          <div className="grid grid-cols-2 gap-6">
            <Form.Item
              name="builtinAuthEnabled"
              label="内置认证"
              valuePropName="checked"
            >
              <Switch />
            </Form.Item>
            <Form.Item
              name="oidcAuthEnabled"
              label="OIDC认证"
              valuePropName="checked"
            >
              <Switch />
            </Form.Item>
            <Form.Item
              name="autoApproveDevelopers"
              label="开发者自动审批"
              valuePropName="checked"
            >
              <Switch />
            </Form.Item>
            <Form.Item
              name="autoApproveSubscriptions"
              label="订阅自动审批"
              valuePropName="checked"
            >
              <Switch />
            </Form.Item>
            <Form.Item
              name="frontendRedirectUrl"
              label="前端重定向URL"
              className="col-span-2"
            >
              <Input placeholder="http://portal.example.com/callback" />
            </Form.Item>
          </div>
        </Card>

        <Card 
          title="域名管理" 
          className="mb-6"
          extra={
            <Button 
              type="primary" 
              icon={<PlusOutlined />} 
              onClick={handleAddDomain}
            >
              绑定域名
            </Button>
          }
        >
          <Table 
            columns={domainColumns} 
            dataSource={portal.portalDomainConfig}
            rowKey="domain"
            pagination={false}
            size="small"
          />
        </Card>

        <Card title="可见性设置" className="mb-6">
          <div className="grid grid-cols-2 gap-6">
            <Form.Item
              name="apiVisibility"
              label="默认API可见性"
              rules={[{ required: true, message: '请选择API可见性' }]}
            >
              <Select placeholder="请选择API可见性">
                <Select.Option value="Public">公开</Select.Option>
                <Select.Option value="Private">私有</Select.Option>
                <Select.Option value="Authenticated">需要认证</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item
              name="pageVisibility"
              label="默认页面可见性"
              rules={[{ required: true, message: '请选择页面可见性' }]}
            >
              <Select placeholder="请选择页面可见性">
                <Select.Option value="Public">公开</Select.Option>
                <Select.Option value="Private">私有</Select.Option>
                <Select.Option value="Authenticated">需要认证</Select.Option>
              </Select>
            </Form.Item>
          </div>
        </Card>

        <Card title="功能开关">
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <div>
                <div className="font-medium">分析统计</div>
                <div className="text-sm text-gray-500">启用访问统计和分析功能</div>
              </div>
              <Form.Item name="enableAnalytics" valuePropName="checked" noStyle>
                <Switch />
              </Form.Item>
            </div>
            <Divider />
            <div className="flex justify-between items-center">
              <div>
                <div className="font-medium">通知系统</div>
                <div className="text-sm text-gray-500">启用邮件和Webhook通知</div>
              </div>
              <Form.Item name="enableNotifications" valuePropName="checked" noStyle>
                <Switch />
              </Form.Item>
            </div>
            <Divider />
            <div className="flex justify-between items-center">
              <div>
                <div className="font-medium">审计日志</div>
                <div className="text-sm text-gray-500">记录用户操作和系统事件</div>
              </div>
              <Form.Item name="enableAuditLog" valuePropName="checked" noStyle>
                <Switch />
              </Form.Item>
            </div>
            <Divider />
            <div className="flex justify-between items-center">
              <div>
                <div className="font-medium">限流控制</div>
                <div className="text-sm text-gray-500">启用API调用频率限制</div>
              </div>
              <Form.Item name="enableRateLimiting" valuePropName="checked" noStyle>
                <Switch />
              </Form.Item>
            </div>
            <Divider />
            <div className="flex justify-between items-center">
              <div>
                <div className="font-medium">缓存</div>
                <div className="text-sm text-gray-500">启用响应缓存以提高性能</div>
              </div>
              <Form.Item name="enableCaching" valuePropName="checked" noStyle>
                <Switch />
              </Form.Item>
            </div>
            <Divider />
            <div className="flex justify-between items-center">
              <div>
                <div className="font-medium">压缩</div>
                <div className="text-sm text-gray-500">启用响应压缩以减少带宽</div>
              </div>
              <Form.Item name="enableCompression" valuePropName="checked" noStyle>
                <Switch />
              </Form.Item>
            </div>
            <Divider />
            <div className="flex justify-between items-center">
              <div>
                <div className="font-medium">SSL/TLS</div>
                <div className="text-sm text-gray-500">强制使用HTTPS连接</div>
              </div>
              <Form.Item name="enableSSL" valuePropName="checked" noStyle>
                <Switch />
              </Form.Item>
            </div>
            <Divider />
            <div className="flex justify-between items-center">
              <div>
                <div className="font-medium">CORS</div>
                <div className="text-sm text-gray-500">启用跨域资源共享</div>
              </div>
              <Form.Item name="enableCORS" valuePropName="checked" noStyle>
                <Switch />
              </Form.Item>
            </div>
          </div>
        </Card>
      </Form>

      {/* 域名绑定模态框 */}
      <Modal
        title="绑定域名"
        open={domainModalVisible}
        onOk={handleDomainModalOk}
        onCancel={handleDomainModalCancel}
        confirmLoading={domainLoading}
        okText="绑定"
        cancelText="取消"
      >
        <Form
          form={domainForm}
          layout="vertical"
        >
          <Form.Item
            name="domain"
            label="域名"
            rules={[
              { required: true, message: '请输入域名' },
              { pattern: /^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/, message: '请输入有效的域名格式' }
            ]}
          >
            <Input placeholder="example.com" />
          </Form.Item>
          <Form.Item
            name="protocol"
            label="协议"
            rules={[{ required: true, message: '请选择协议' }]}
          >
            <Select placeholder="请选择协议">
              <Select.Option value="HTTP">HTTP</Select.Option>
              <Select.Option value="HTTPS">HTTPS</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
} 