import { Card, Form, Input, Select, Switch, Button, Divider, Space, Tag } from 'antd'
import { SaveOutlined, ReloadOutlined } from '@ant-design/icons'
import { useState } from 'react'

interface Portal {
  id: string
  name: string
  title: string
  description: string
  url: string
  userAuth: string
  rbac: string
  authStrategy: string
  apiVisibility: string
  pageVisibility: string
  logo?: string
}

interface PortalSettingsProps {
  portal: Portal
}

export function PortalSettings({ portal }: PortalSettingsProps) {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)

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
          url: portal.url,
          userAuth: portal.userAuth,
          rbac: portal.rbac,
          authStrategy: portal.authStrategy,
          apiVisibility: portal.apiVisibility,
          pageVisibility: portal.pageVisibility,
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
              label="Portal ID"
              rules={[{ required: true, message: '请输入Portal ID' }]}
            >
              <Input placeholder="请输入Portal ID" />
            </Form.Item>
            <Form.Item
              name="title"
              label="Portal标题"
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
            <Form.Item
              name="url"
              label="Portal URL"
              rules={[{ required: true, message: '请输入Portal URL' }]}
              className="col-span-2"
            >
              <Input placeholder="https://portal.example.com" />
            </Form.Item>
          </div>
        </Card>

        <Card title="认证设置" className="mb-6">
          <div className="grid grid-cols-2 gap-6">
            <Form.Item
              name="userAuth"
              label="用户认证"
              rules={[{ required: true, message: '请选择用户认证方式' }]}
            >
              <Select placeholder="请选择用户认证方式">
                <Select.Option value="Konnect Built-in">Konnect Built-in</Select.Option>
                <Select.Option value="LDAP">LDAP</Select.Option>
                <Select.Option value="OAuth2">OAuth2</Select.Option>
                <Select.Option value="SAML">SAML</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item
              name="rbac"
              label="RBAC"
              rules={[{ required: true, message: '请选择RBAC状态' }]}
            >
              <Select placeholder="请选择RBAC状态">
                <Select.Option value="Enabled">启用</Select.Option>
                <Select.Option value="Disabled">禁用</Select.Option>
              </Select>
            </Form.Item>
            <Form.Item
              name="authStrategy"
              label="认证策略"
              rules={[{ required: true, message: '请选择认证策略' }]}
            >
              <Select placeholder="请选择认证策略">
                <Select.Option value="key-auth">API Key</Select.Option>
                <Select.Option value="oauth2">OAuth2</Select.Option>
                <Select.Option value="jwt">JWT</Select.Option>
                <Select.Option value="basic-auth">Basic Auth</Select.Option>
              </Select>
            </Form.Item>
          </div>
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
    </div>
  )
} 