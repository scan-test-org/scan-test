import {useState} from 'react'
import {Button, Form, Input, Select, Switch, Table, Modal, Space, message, Divider, Steps, Card, Tabs, Collapse} from 'antd'
import {PlusOutlined, EditOutlined, DeleteOutlined, ExclamationCircleOutlined, MinusCircleOutlined, KeyOutlined, CheckOutlined, CloseOutlined} from '@ant-design/icons'
import {ThirdPartyAuthConfig, AuthenticationType, GrantType, AuthCodeConfig, OAuth2AuthConfig, PublicKeyFormat} from '@/types'

interface ThirdPartyAuthManagerProps {
  configs: ThirdPartyAuthConfig[]
  onSave: (configs: ThirdPartyAuthConfig[]) => Promise<void>
}

export function ThirdPartyAuthManager({configs, onSave}: ThirdPartyAuthManagerProps) {
  const [form] = Form.useForm()
  const [modalVisible, setModalVisible] = useState(false)
  const [loading, setLoading] = useState(false)
  const [editingConfig, setEditingConfig] = useState<ThirdPartyAuthConfig | null>(null)
  const [currentStep, setCurrentStep] = useState(0)
  const [selectedType, setSelectedType] = useState<AuthenticationType | null>(null)

  // 添加新配置
  const handleAdd = () => {
    setEditingConfig(null)
    setSelectedType(null)
    setCurrentStep(0)
    setModalVisible(true)
    form.resetFields()
  }

  // 编辑配置
  const handleEdit = (config: ThirdPartyAuthConfig) => {
    setEditingConfig(config)
    setSelectedType(config.type)
    setCurrentStep(1) // 直接进入配置步骤
    setModalVisible(true)
    
    // 根据类型设置表单值
    if (config.type === AuthenticationType.OIDC && config.oidcConfig) {
      form.setFieldsValue({
        provider: config.provider,
        name: config.name,
        enabled: config.enabled,
        type: config.type,
        ...config.oidcConfig.authCodeConfig,
        userIdField: config.oidcConfig.authCodeConfig.identityMapping?.userIdField,
        userNameField: config.oidcConfig.authCodeConfig.identityMapping?.userNameField,
        emailField: config.oidcConfig.authCodeConfig.identityMapping?.emailField
      })
    } else if (config.type === AuthenticationType.OAUTH2 && config.oauth2Config) {
      form.setFieldsValue({
        provider: config.provider,
        name: config.name,
        enabled: config.enabled,
        type: config.type,
        grantType: config.oauth2Config.grantType,
        userIdField: config.oauth2Config.identityMapping?.userIdField,
        userNameField: config.oauth2Config.identityMapping?.userNameField,
        publicKeys: config.oauth2Config.jwtBearerConfig?.publicKeys || []
      })
    }
  }

  // 删除配置
  const handleDelete = async (provider: string, name: string) => {
    Modal.confirm({
      title: '确认删除',
      icon: <ExclamationCircleOutlined/>,
      content: `确定要删除第三方认证配置 "${name}" 吗？此操作不可恢复。`,
      okText: '确认删除',
      okType: 'danger',
      cancelText: '取消',
      async onOk() {
        try {
          const updatedConfigs = configs.filter(config => config.provider !== provider)
          await onSave(updatedConfigs)
          message.success('第三方认证配置删除成功')
        } catch (error) {
          console.error('删除第三方认证配置失败:', error)
          message.error('删除第三方认证配置失败')
        }
      },
    })
  }


  // 下一步
  const handleNext = async () => {
    if (currentStep === 0) {
      try {
        const values = await form.validateFields(['type'])
        setSelectedType(values.type)
        setCurrentStep(1)
      } catch (error) {
        // 验证失败
      }
    }
  }

  // 上一步
  const handlePrevious = () => {
    setCurrentStep(0)
  }

  // 保存配置
  const handleSave = async () => {
    try {
      setLoading(true)
      const values = await form.validateFields()

      let newConfig: ThirdPartyAuthConfig

      if (selectedType === AuthenticationType.OIDC) {
        // OIDC配置
        const authCodeConfig: AuthCodeConfig = {
          clientId: values.clientId,
          clientSecret: values.clientSecret,
          scopes: values.scopes,
          authorizationEndpoint: values.authorizationEndpoint,
          tokenEndpoint: values.tokenEndpoint,
          userInfoEndpoint: values.userInfoEndpoint,
          jwkSetUri: values.jwkSetUri,
          // 可选的身份映射配置
          identityMapping: (values.userIdField || values.userNameField || values.emailField) ? {
            userIdField: values.userIdField || undefined,
            userNameField: values.userNameField || undefined,
            emailField: values.emailField || undefined
          } : undefined
        }

        newConfig = {
          provider: values.provider,
          name: values.name,
          type: AuthenticationType.OIDC,
          enabled: values.enabled ?? true,
          oidcConfig: {
            grantType: 'AUTHORIZATION_CODE',
            authCodeConfig
          }
        }
      } else {
        // OAuth2配置
        const oauth2Config: OAuth2AuthConfig = {
          grantType: values.grantType,
          jwtBearerConfig: values.grantType === GrantType.JWT_BEARER ? {
            publicKeys: values.publicKeys || []
          } : undefined,
          identityMapping: {
            userIdField: values.userIdField,
            userNameField: values.userNameField
          }
        }

        newConfig = {
          provider: values.provider,
          name: values.name,
          type: AuthenticationType.OAUTH2,
          enabled: values.enabled ?? true,
          oauth2Config
        }
      }

      let updatedConfigs
      if (editingConfig) {
        updatedConfigs = configs.map(config => 
          config.provider === editingConfig.provider ? newConfig : config
        )
      } else {
        updatedConfigs = [...configs, newConfig]
      }

      await onSave(updatedConfigs)
      message.success(editingConfig ? '第三方认证配置更新成功' : '第三方认证配置添加成功')
      setModalVisible(false)
    } catch (error) {
      console.error('保存第三方认证配置失败:', error)
      message.error('保存第三方认证配置失败')
    } finally {
      setLoading(false)
    }
  }

  // 取消
  const handleCancel = () => {
    setModalVisible(false)
    setEditingConfig(null)
    setSelectedType(null)
    setCurrentStep(0)
    form.resetFields()
  }

  // OIDC表格列定义（不包含类型列）
  const oidcColumns = [
    {
      title: '提供商',
      dataIndex: 'provider',
      key: 'provider',
      width: 120,
      render: (provider: string) => (
        <span className="font-medium text-gray-700">{provider}</span>
      )
    },
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      width: 150,
    },
    {
      title: '授权模式',
      key: 'grantType',
      width: 120,
      render: () => <span className="text-gray-600">授权码模式</span>
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 80,
      render: (enabled: boolean) => (
        <div className="flex items-center">
          <div className={`w-3 h-3 rounded-full mr-2 flex items-center justify-center ${enabled ? 'bg-green-500' : 'bg-red-500'}`}>
            {enabled ? (
              <CheckOutlined className="text-white" style={{fontSize: '8px'}} />
            ) : (
              <CloseOutlined className="text-white" style={{fontSize: '8px'}} />
            )}
          </div>
          <span className="text-gray-700">
            {enabled ? '已启用' : '已禁用'}
          </span>
        </div>
      )
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: any, record: ThirdPartyAuthConfig) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined/>}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined/>}
            onClick={() => handleDelete(record.provider, record.name)}
          >
            删除
          </Button>
        </Space>
      )
    }
  ]

  // OAuth2表格列定义（不包含类型列）
  const oauth2Columns = [
    {
      title: '提供商',
      dataIndex: 'provider',
      key: 'provider',
      width: 120,
      render: (provider: string) => (
        <span className="font-medium text-gray-700">{provider}</span>
      )
    },
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      width: 150,
    },
    {
      title: '授权模式',
      key: 'grantType',
      width: 120,
      render: (record: ThirdPartyAuthConfig) => {
        if (record.oauth2Config) {
          return (
            <span className="text-gray-600">
              {record.oauth2Config.grantType === GrantType.JWT_BEARER ? 'JWT断言' : '授权码模式'}
            </span>
          )
        }
        return <span className="text-gray-600">JWT断言</span>
      }
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 80,
      render: (enabled: boolean) => (
        <div className="flex items-center">
          <div className={`w-3 h-3 rounded-full mr-2 flex items-center justify-center ${enabled ? 'bg-green-500' : 'bg-red-500'}`}>
            {enabled ? (
              <CheckOutlined className="text-white" style={{fontSize: '8px'}} />
            ) : (
              <CloseOutlined className="text-white" style={{fontSize: '8px'}} />
            )}
          </div>
          <span className="text-gray-700">
            {enabled ? '已启用' : '已禁用'}
          </span>
        </div>
      )
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: any, record: ThirdPartyAuthConfig) => (
        <Space>
          <Button
            type="link"
            icon={<EditOutlined/>}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            danger
            icon={<DeleteOutlined/>}
            onClick={() => handleDelete(record.provider, record.name)}
          >
            删除
          </Button>
        </Space>
      )
    }
  ]

  // 渲染OIDC配置表单
  const renderOidcForm = () => (
    <div className="space-y-6">
      <Form.Item
        name="grantType"
        label="授权模式"
        initialValue="AUTHORIZATION_CODE"
      >
        <Select disabled>
          <Select.Option value="AUTHORIZATION_CODE">授权码模式</Select.Option>
        </Select>
      </Form.Item>

      <div className="grid grid-cols-2 gap-4">
        <Form.Item
          name="clientId"
          label="Client ID"
          rules={[{required: true, message: '请输入 Client ID'}]}
        >
          <Input placeholder="Client ID"/>
        </Form.Item>
        <Form.Item
          name="clientSecret"
          label="Client Secret"
          rules={[{required: true, message: '请输入 Client Secret'}]}
        >
          <Input.Password placeholder="Client Secret"/>
        </Form.Item>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Form.Item
          name="scopes"
          label="授权范围"
          rules={[{required: true, message: '请输入授权范围'}]}
        >
          <Input placeholder="如: openid profile email"/>
        </Form.Item>
        <div></div>
      </div>

      <Divider />

      <div className="grid grid-cols-2 gap-4">
        <Form.Item
          name="authorizationEndpoint"
          label="授权端点"
          rules={[{required: true, message: '请输入授权端点'}]}
        >
          <Input placeholder="Authorization 授权端点"/>
        </Form.Item>
        <Form.Item
          name="tokenEndpoint"
          label="令牌端点"
          rules={[{required: true, message: '请输入令牌端点'}]}
        >
          <Input placeholder="Token 令牌端点"/>
        </Form.Item>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <Form.Item
          name="userInfoEndpoint"
          label="用户信息端点"
          rules={[{required: true, message: '请输入用户信息端点'}]}
        >
          <Input placeholder="UserInfo 端点"/>
        </Form.Item>
        <Form.Item
          name="jwkSetUri"
          label="公钥端点"
        >
          <Input placeholder="JWKS 公钥端点 (可选)"/>
        </Form.Item>
      </div>

      <div className="-ml-3">
        <Collapse
          size="small"
          ghost
          expandIcon={({ isActive }) => (
            <svg 
              className={`w-4 h-4 transition-transform ${isActive ? 'rotate-90' : ''}`}
              fill="currentColor" 
              viewBox="0 0 20 20"
            >
              <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
            </svg>
          )}
          items={[
            {
              key: 'advanced',
              label: (
                <div className="flex items-center text-gray-600">
                  <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947z" clipRule="evenodd" />
                    <path fillRule="evenodd" d="M10 13a3 3 0 100-6 3 3 0 000 6z" clipRule="evenodd" />
                  </svg>
                  <span>高级配置</span>
                  <span className="text-xs text-gray-400 ml-2">用户身份映射</span>
                </div>
              ),
              children: (
                <div className="space-y-4 pt-2 ml-3">
                  <div className="grid grid-cols-3 gap-4">
                    <Form.Item
                      name="userIdField"
                      label="用户ID字段"
                    >
                      <Input placeholder="默认: sub"/>
                    </Form.Item>
                    <Form.Item
                      name="userNameField"
                      label="用户名字段"
                    >
                      <Input placeholder="默认: name"/>
                    </Form.Item>
                    <Form.Item
                      name="emailField"
                      label="邮箱字段"
                    >
                      <Input placeholder="默认: email"/>
                    </Form.Item>
                  </div>

                <div className="bg-green-50 p-3 rounded-lg">
                  <div className="flex items-start space-x-2">
                    <div className="text-green-600 mt-0.5">
                      <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                      </svg>
                    </div>
                    <div>
                      <h4 className="text-green-800 font-medium text-sm">配置说明</h4>
                      <p className="text-green-700 text-xs mt-1">
                        身份映射用于从OIDC令牌中提取用户信息。如果不填写，系统将使用OIDC标准字段。
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )
          }
        ]}
      />
      </div>
    </div>
  )

  // 渲染OAuth2配置表单
  const renderOAuth2Form = () => (
    <div className="space-y-6">
      <Form.Item
        name="grantType"
        label="授权模式"
        initialValue={GrantType.JWT_BEARER}
        rules={[{required: true}]}
      >
        <Select disabled>
          <Select.Option value={GrantType.JWT_BEARER}>JWT Bearer</Select.Option>
        </Select>
      </Form.Item>

      <Form.List name="publicKeys">
        {(fields, { add, remove }) => (
          <div className="space-y-4">
            {fields.length > 0 && (
              <Collapse
                size="small"
                items={fields.map(({ key, name, ...restField }) => ({
                  key: key,
                  label: (
                    <div className="flex items-center">
                      <KeyOutlined className="mr-2" />
                      <span>公钥 {name + 1}</span>
                    </div>
                  ),
                  extra: (
                    <Button
                      type="link"
                      danger
                      size="small"
                      icon={<MinusCircleOutlined />}
                      onClick={(e) => {
                        e.stopPropagation()
                        remove(name)
                      }}
                    >
                      删除
                    </Button>
                  ),
                  children: (
                    <div className="space-y-4 px-4">
                      <div className="grid grid-cols-3 gap-4">
                        <Form.Item
                          {...restField}
                          name={[name, 'kid']}
                          label="Key ID"
                          rules={[{ required: true, message: '请输入Key ID' }]}
                        >
                          <Input placeholder="公钥标识符" size="small" />
                        </Form.Item>
                        <Form.Item
                          {...restField}
                          name={[name, 'algorithm']}
                          label="签名算法"
                          rules={[{ required: true, message: '请选择签名算法' }]}
                        >
                          <Select placeholder="选择签名算法" size="small">
                            <Select.Option value="RS256">RS256</Select.Option>
                            <Select.Option value="RS384">RS384</Select.Option>
                            <Select.Option value="RS512">RS512</Select.Option>
                            <Select.Option value="ES256">ES256</Select.Option>
                            <Select.Option value="ES384">ES384</Select.Option>
                            <Select.Option value="ES512">ES512</Select.Option>
                          </Select>
                        </Form.Item>
                        <Form.Item
                          {...restField}
                          name={[name, 'format']}
                          label="公钥格式"
                          rules={[{ required: true, message: '请选择公钥格式' }]}
                        >
                          <Select placeholder="选择公钥格式" size="small">
                            <Select.Option value={PublicKeyFormat.PEM}>PEM</Select.Option>
                            <Select.Option value={PublicKeyFormat.JWK}>JWK</Select.Option>
                          </Select>
                        </Form.Item>
                      </div>

                      <Form.Item
                        {...restField}
                        name={[name, 'value']}
                        label="公钥内容"
                        rules={[{ required: true, message: '请输入公钥内容' }]}
                      >
                        <Form.Item
                          noStyle
                          shouldUpdate={(prevValues, curValues) => {
                            const prevFormat = prevValues?.publicKeys?.[name]?.format
                            const curFormat = curValues?.publicKeys?.[name]?.format
                            return prevFormat !== curFormat
                          }}
                        >
                          {({ getFieldValue }) => {
                            const format = getFieldValue(['publicKeys', name, 'format'])
                            return (
                              <Input.TextArea
                                rows={6}
                                placeholder={
                                  format === PublicKeyFormat.JWK
                                    ? 'JWK格式公钥，例如:\n{\n  "kty": "RSA",\n  "kid": "key1",\n  "n": "...",\n  "e": "AQAB"\n}'
                                    : 'PEM格式公钥，例如:\n-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A...\n-----END PUBLIC KEY-----'
                                }
                                style={{ fontFamily: 'monospace', fontSize: '12px' }}
                              />
                            )
                          }}
                        </Form.Item>
                      </Form.Item>
                    </div>
                  )
                }))}
              />
            )}
            <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />} size="small">
              添加公钥
            </Button>
          </div>
        )}
      </Form.List>

      <div className="-ml-3">
        <Collapse
          size="small"
          ghost
          expandIcon={({ isActive }) => (
            <svg 
              className={`w-4 h-4 transition-transform ${isActive ? 'rotate-90' : ''}`}
              fill="currentColor" 
              viewBox="0 0 20 20"
            >
              <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
            </svg>
          )}
          items={[
            {
              key: 'advanced',
              label: (
                <div className="flex items-center text-gray-600">
                  <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947z" clipRule="evenodd" />
                    <path fillRule="evenodd" d="M10 13a3 3 0 100-6 3 3 0 000 6z" clipRule="evenodd" />
                  </svg>
                  <span>高级配置</span>
                  <span className="text-xs text-gray-400 ml-2">用户身份映射</span>
                </div>
              ),
              children: (
                <div className="space-y-4 pt-2 ml-3">
                  <div className="grid grid-cols-2 gap-6">
                    <Form.Item
                      name="userIdField"
                      label="用户ID字段"
                    >
                      <Input placeholder="默认: userId"/>
                    </Form.Item>
                    <Form.Item
                      name="userNameField"
                      label="用户名字段"
                    >
                      <Input placeholder="默认: username"/>
                    </Form.Item>
                  </div>

                <div className="bg-blue-50 p-3 rounded-lg">
                  <div className="flex items-start space-x-2">
                    <div className="text-blue-600 mt-0.5">
                      <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                      </svg>
                    </div>
                    <div>
                      <h4 className="text-blue-800 font-medium text-sm">配置说明</h4>
                      <p className="text-blue-700 text-xs mt-1">
                        身份映射用于从JWT载荷中提取用户信息。如果不填写，系统将使用默认字段名。
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            )
          }
        ]}
      />
      </div>
    </div>
  )

  // 按类型分组配置
  const oidcConfigs = configs.filter(config => config.type === AuthenticationType.OIDC)
  const oauth2Configs = configs.filter(config => config.type === AuthenticationType.OAUTH2)

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-lg font-medium">第三方认证</h3>
          <p className="text-sm text-gray-500">管理OIDC和OAuth2认证配置</p>
        </div>
        <Button
          type="primary"
          icon={<PlusOutlined/>}
          onClick={handleAdd}
        >
          添加认证配置
        </Button>
      </div>

      <Tabs
        defaultActiveKey="oidc"
        items={[
          {
            key: 'oidc',
            label: 'OIDC配置',
            children: (
              <div className="bg-white rounded-lg">
                <div className="py-4 border-b border-gray-200">
                  <h4 className="text-lg font-medium text-gray-900">OIDC配置</h4>
                  <p className="text-sm text-gray-500 mt-1">支持OpenID Connect标准协议的身份提供商</p>
                </div>
                <Table
                  columns={oidcColumns}
                  dataSource={oidcConfigs}
                  rowKey="provider"
                  pagination={false}
                  size="small"
                  locale={{
                    emptyText: '暂无OIDC配置'
                  }}
                />
              </div>
            ),
          },
          {
            key: 'oauth2',
            label: 'OAuth2配置',
            children: (
              <div className="bg-white rounded-lg">
                <div className="py-4 border-b border-gray-200">
                  <h4 className="text-lg font-medium text-gray-900">OAuth2配置</h4>
                  <p className="text-sm text-gray-500 mt-1">支持JWT Bearer模式的OAuth2认证</p>
                </div>
                <Table
                  columns={oauth2Columns}
                  dataSource={oauth2Configs}
                  rowKey="provider"
                  pagination={false}
                  size="small"
                  locale={{
                    emptyText: '暂无OAuth2配置'
                  }}
                />
              </div>
            ),
          },
        ]}
      />

      {/* 添加/编辑配置模态框 */}
      <Modal
        title={editingConfig ? '编辑第三方认证配置' : '添加第三方认证配置'}
        open={modalVisible}
        onCancel={handleCancel}
        width={800}
        footer={null}
      >
        <Steps
          current={currentStep}
          className="mb-6"
          items={[
            {
              title: '选择类型',
              description: '选择认证协议类型'
            },
            {
              title: '配置认证',
              description: '填写认证参数'
            }
          ]}
        />

        <Form
          form={form}
          layout="vertical"
        >
          {currentStep === 0 ? (
            // 第一步：选择类型
            <Card>
              <Form.Item
                name="type"
                label="认证类型"
                rules={[{required: true, message: '请选择认证类型'}]}
              >
                <Select placeholder="请选择认证方式" size="large">
                  <Select.Option value={AuthenticationType.OIDC}>
                    <div className="py-2">
                      <div className="font-medium">OIDC (OpenID Connect)</div>
                      <div className="text-sm text-gray-500">适用于Google、微软、阿里云等标准身份提供商</div>
                    </div>
                  </Select.Option>
                  <Select.Option value={AuthenticationType.OAUTH2}>
                    <div className="py-2">
                      <div className="font-medium">OAuth2 JWT Bearer</div>
                      <div className="text-sm text-gray-500">适用于企业内部认证系统和自定义令牌验证</div>
                    </div>
                  </Select.Option>
                </Select>
              </Form.Item>
              
              <div className="flex justify-end">
                <Button type="primary" onClick={handleNext}>
                  下一步
                </Button>
              </div>
            </Card>
          ) : (
            // 第二步：配置详情
            <div>
              <div className="grid grid-cols-2 gap-4">
                <Form.Item
                  name="provider"
                  label="提供商标识"
                  rules={[{required: true, message: '请输入提供商标识'}]}
                >
                  <Input
                    placeholder="如: google, company-sso"
                    disabled={editingConfig !== null}
                  />
                </Form.Item>
                <Form.Item
                  name="name"
                  label="显示名称"
                  rules={[{required: true, message: '请输入显示名称'}]}
                >
                  <Input placeholder="如: Google登录、公司SSO"/>
                </Form.Item>
              </div>

              <Form.Item
                name="enabled"
                label="启用状态"
                valuePropName="checked"
              >
                <Switch/>
              </Form.Item>

              <Divider />

              {/* 根据类型显示不同的配置表单 */}
              {selectedType === AuthenticationType.OIDC ? renderOidcForm() : renderOAuth2Form()}

              <div className="flex justify-between mt-6">
                <Button onClick={handlePrevious}>
                  上一步
                </Button>
                <Space>
                  <Button onClick={handleCancel}>
                    取消
                  </Button>
                  <Button type="primary" loading={loading} onClick={handleSave}>
                    {editingConfig ? '更新' : '添加'}
                  </Button>
                </Space>
              </div>
            </div>
          )}
        </Form>
      </Modal>

    </div>
  )
}
