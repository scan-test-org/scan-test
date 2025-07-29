import { Card, Form, Input, Select, Switch, Button, Divider, Space, Tag, Table, Modal, message, Tabs } from 'antd'
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

  // 通用的设置更新方法 - 仅更新表单值，不立即保存
  const handleSettingUpdate = (fieldName: string, value: any) => {
    form.setFieldsValue({ [fieldName]: value })
  }

  const handleSave = async () => {
    setLoading(true)
    try {
      const values = await form.validateFields()
      console.log('保存设置:', values)
      
      // 调用API保存设置
      const updateData = {
        ...values,
        // 保持现有的OIDC配置
        oidcOptions: portal.portalSettingConfig.oidcConfigs || []
      }
      
      await portalApi.updatePortalSettings(portal.portalId, updateData)
      message.success('设置保存成功')
      onRefresh?.()
    } catch (error) {
      console.error('保存设置失败:', error)
      message.error('保存设置失败')
    } finally {
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
      render: (_: any, record: any) => (
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

  const tabItems = [
    {
      key: 'basic',
      label: '基本信息',
      children: (
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
        </div>
      )
    },
    {
      key: 'auth',
      label: '认证设置',
      children: (
        <div className="grid grid-cols-2 gap-6">
          <Form.Item
            name="builtinAuthEnabled"
            label="内置认证"
            valuePropName="checked"
          >
            <Switch 
              onChange={(checked) => handleSettingUpdate('builtinAuthEnabled', checked)}
            />
          </Form.Item>
          <Form.Item
            name="oidcAuthEnabled"
            label="OIDC认证"
            valuePropName="checked"
          >
            <Switch 
              onChange={(checked) => handleSettingUpdate('oidcAuthEnabled', checked)}
            />
          </Form.Item>
          <Form.Item
            name="autoApproveDevelopers"
            label="开发者自动审批"
            valuePropName="checked"
          >
            <Switch 
              onChange={(checked) => handleSettingUpdate('autoApproveDevelopers', checked)}
            />
          </Form.Item>
          <Form.Item
            name="autoApproveSubscriptions"
            label="订阅自动审批"
            valuePropName="checked"
          >
            <Switch 
              onChange={(checked) => handleSettingUpdate('autoApproveSubscriptions', checked)}
            />
          </Form.Item>
          <Form.Item
            name="frontendRedirectUrl"
            label="前端重定向URL"
            className="col-span-2"
          >
            <Input placeholder="http://portal.example.com/callback" />
          </Form.Item>
        </div>
      )
    },
    {
      key: 'domain',
      label: '域名管理',
      children: (
        <div>
          <div className="flex justify-between items-center mb-4">
            <div>
              <h3 className="text-lg font-medium">域名列表</h3>
              <p className="text-sm text-gray-500">管理Portal的域名配置</p>
            </div>
            <Button 
              type="primary" 
              icon={<PlusOutlined />} 
              onClick={handleAddDomain}
            >
              绑定域名
            </Button>
          </div>
          <Table 
            columns={domainColumns} 
            dataSource={portal.portalDomainConfig}
            rowKey="domain"
            pagination={false}
            size="small"
          />
        </div>
      )
    },
    {
      key: 'visibility',
      label: '可见性设置',
      children: (
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
      )
    },
    {
      key: 'features',
      label: '功能开关',
      children: (
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <div>
              <div className="font-medium">分析统计</div>
              <div className="text-sm text-gray-500">启用访问统计和分析功能</div>
            </div>
            <Form.Item name="enableAnalytics" valuePropName="checked" noStyle>
              <Switch 
                onChange={(checked) => handleSettingUpdate('enableAnalytics', checked)}
              />
            </Form.Item>
          </div>
          <Divider />
          <div className="flex justify-between items-center">
            <div>
              <div className="font-medium">通知系统</div>
              <div className="text-sm text-gray-500">启用邮件和Webhook通知</div>
            </div>
            <Form.Item name="enableNotifications" valuePropName="checked" noStyle>
              <Switch 
                onChange={(checked) => handleSettingUpdate('enableNotifications', checked)}
              />
            </Form.Item>
          </div>
          <Divider />
          <div className="flex justify-between items-center">
            <div>
              <div className="font-medium">审计日志</div>
              <div className="text-sm text-gray-500">记录用户操作和系统事件</div>
            </div>
            <Form.Item name="enableAuditLog" valuePropName="checked" noStyle>
              <Switch 
                onChange={(checked) => handleSettingUpdate('enableAuditLog', checked)}
              />
            </Form.Item>
          </div>
          <Divider />
          <div className="flex justify-between items-center">
            <div>
              <div className="font-medium">限流控制</div>
              <div className="text-sm text-gray-500">启用API调用频率限制</div>
            </div>
            <Form.Item name="enableRateLimiting" valuePropName="checked" noStyle>
              <Switch 
                onChange={(checked) => handleSettingUpdate('enableRateLimiting', checked)}
              />
            </Form.Item>
          </div>
          <Divider />
          <div className="flex justify-between items-center">
            <div>
              <div className="font-medium">缓存</div>
              <div className="text-sm text-gray-500">启用响应缓存以提高性能</div>
            </div>
            <Form.Item name="enableCaching" valuePropName="checked" noStyle>
              <Switch 
                onChange={(checked) => handleSettingUpdate('enableCaching', checked)}
              />
            </Form.Item>
          </div>
          <Divider />
          <div className="flex justify-between items-center">
            <div>
              <div className="font-medium">压缩</div>
              <div className="text-sm text-gray-500">启用响应压缩以减少带宽</div>
            </div>
            <Form.Item name="enableCompression" valuePropName="checked" noStyle>
              <Switch 
                onChange={(checked) => handleSettingUpdate('enableCompression', checked)}
              />
            </Form.Item>
          </div>
          <Divider />
          <div className="flex justify-between items-center">
            <div>
              <div className="font-medium">SSL/TLS</div>
              <div className="text-sm text-gray-500">强制使用HTTPS连接</div>
            </div>
            <Form.Item name="enableSSL" valuePropName="checked" noStyle>
              <Switch 
                onChange={(checked) => handleSettingUpdate('enableSSL', checked)}
              />
            </Form.Item>
          </div>
          <Divider />
          <div className="flex justify-between items-center">
            <div>
              <div className="font-medium">CORS</div>
              <div className="text-sm text-gray-500">启用跨域资源共享</div>
            </div>
            <Form.Item name="enableCORS" valuePropName="checked" noStyle>
              <Switch 
                onChange={(checked) => handleSettingUpdate('enableCORS', checked)}
              />
            </Form.Item>
          </div>
        </div>
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
        <Card>
          <Tabs 
            items={tabItems}
            defaultActiveKey="basic"
            type="card"
          />
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