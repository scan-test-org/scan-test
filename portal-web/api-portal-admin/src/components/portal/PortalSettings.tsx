import {Card, Form, Input, Select, Switch, Button, Divider, Space, Tag, Table, Modal, message, Tabs} from 'antd'
import {SaveOutlined, PlusOutlined, DeleteOutlined, EditOutlined, ExclamationCircleOutlined} from '@ant-design/icons'
import {useState, useMemo} from 'react'
import {Portal, OidcConfig, AuthCodeConfig, OAuth2Config, GrantType} from '@/types'
import {portalApi} from '@/lib/api'
import {PublicKeyManager} from './PublicKeyManager'

interface PortalSettingsProps {
    portal: Portal
    onRefresh?: () => void
}

export function PortalSettings({portal, onRefresh}: PortalSettingsProps) {
    const [form] = Form.useForm()
    const [loading, setLoading] = useState(false)
    const [domainModalVisible, setDomainModalVisible] = useState(false)
    const [domainForm] = Form.useForm()
    const [domainLoading, setDomainLoading] = useState(false)

    // OIDC 配置相关状态
    const [oidcModalVisible, setOidcModalVisible] = useState(false)
    const [oidcForm] = Form.useForm()
    const [oidcLoading, setOidcLoading] = useState(false)
    const [editingOidc, setEditingOidc] = useState<OidcConfig | null>(null)

    // OAuth2 配置相关状态
    const [oauth2ModalVisible, setOAuth2ModalVisible] = useState(false)
    const [oauth2Form] = Form.useForm()
    const [oauth2Loading, setOAuth2Loading] = useState(false)
    const [editingOAuth2, setEditingOAuth2] = useState<OAuth2Config | null>(null)
    
    // 公钥管理状态
    const [publicKeyModalVisible, setPublicKeyModalVisible] = useState(false)
    const [currentOAuth2Provider, setCurrentOAuth2Provider] = useState<string | null>(null)

    // 本地OIDC配置状态，避免频繁刷新
    // local的有点问题，一切tab就坏了


    // 通用的设置更新方法 - 仅更新表单值，不立即保存
    const handleSettingUpdate = (fieldName: string, value: any) => {
        form.setFieldsValue({[fieldName]: value})
    }

    const handleSave = async () => {
        setLoading(true)
        try {
            const values = await form.validateFields()
            const {
                builtinAuthEnabled,
                oidcAuthEnabled,
                autoApproveDevelopers,
                autoApproveSubscriptions,
                frontendRedirectUrl,
                ...rest
            } = values

            // 调用API保存设置
            const updateData = {
                ...rest,
                portalSettingConfig: {
                    builtinAuthEnabled,
                    oidcAuthEnabled,
                    autoApproveDevelopers,
                    autoApproveSubscriptions,
                    frontendRedirectUrl,
                    oidcConfigs: portal.portalSettingConfig?.oidcConfigs || []
                }
            }

            await portalApi.updatePortal(portal.portalId, updateData)
            message.success('设置保存成功')
            onRefresh?.()
        } catch (error) {
            console.error('保存设置失败:', error)
            // message.error('保存设置失败')
        } finally {
            setLoading(false)
        }
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

            const newDomain = {
                domain: values.domain,
                protocol: values.protocol,
                type: 'CUSTOM'
            }

            await portalApi.bindDomain(portal.portalId, newDomain)
            message.success('域名绑定成功')
            onRefresh?.()
            setDomainModalVisible(false)

        } catch (error) {
            console.error('绑定域名失败:', error)
        } finally {
            setDomainLoading(false)
        }
    }

    const handleDomainModalCancel = () => {
        setDomainModalVisible(false)
        domainForm.resetFields()
    }

    const handleDeleteDomain = async (domain: string) => {
        Modal.confirm({
            title: '确认解绑',
            icon: <ExclamationCircleOutlined/>,
            content: `确定要解绑域名 "${domain}" 吗？此操作不可恢复。`,
            okText: '确认解绑',
            okType: 'danger',
            cancelText: '取消',
            async onOk() {
                try {
                    await portalApi.unbindDomain(portal.portalId, domain)
                    message.success('域名解绑成功')
                    onRefresh?.()
                } catch (error) {
                    console.error('解绑域名失败:', error)
                }
            },
        })
    }

    // OIDC 配置管理相关函数
    const handleAddOidc = () => {
        setEditingOidc(null)
        setOidcModalVisible(true)
        oidcForm.resetFields()
    }

     const handleEditOidc = (oidc: OidcConfig) => {
         const formValues = {
             ...oidc,
             ...oidc.authCodeConfig,
             grantType: '授权码模式'  // 转换为中文显示
         }
         setEditingOidc(oidc)
         setOidcModalVisible(true)
         oidcForm.setFieldsValue(formValues)
     }

    const oidcConfigs = useMemo(() => {
        return portal.portalSettingConfig?.oidcConfigs || []
    }, [portal.portalSettingConfig?.oidcConfigs])


    const handleOidcModalOk = async () => {
        try {
            setOidcLoading(true)
            const values = await oidcForm.validateFields()

            // 构造授权码配置
            const authCodeConfig: AuthCodeConfig = {
                clientId: values.clientId,
                clientSecret: values.clientSecret,
                scopes: values.scopes,
                authorizationEndpoint: values.authorizationEndpoint,
                tokenEndpoint: values.tokenEndpoint,
                userInfoEndpoint: values.userInfoEndpoint,
                jwkSetUri: values.jwkSetUri
            }

            let updatedOidcConfigs: OidcConfig[]

            if (editingOidc) {
                // 编辑现有配置
                const updatedConfig: OidcConfig = {
                    provider: values.provider,
                    name: values.name,
                    logoUrl: values.logoUrl,
                    enabled: values.enabled,
                    grantType: 'AUTHORIZATION_CODE',
                    authCodeConfig
                }
                updatedOidcConfigs = oidcConfigs.map(config =>
                    config.provider === editingOidc.provider ? updatedConfig : config
                )
            } else {
                // 添加新配置
                const newConfig: OidcConfig = {
                    provider: values.provider,
                    name: values.name,
                    logoUrl: values.logoUrl,
                    enabled: values.enabled,
                    grantType: 'AUTHORIZATION_CODE',
                    authCodeConfig
                }
                updatedOidcConfigs = [...oidcConfigs, newConfig]
            }


            // 更新设置
            await portalApi.updatePortal(portal.portalId, {
                ...portal,
                portalSettingConfig: {
                    ...portal.portalSettingConfig,
                    oidcConfigs: updatedOidcConfigs
                }
            })

            message.success(editingOidc ? 'OIDC配置更新成功' : 'OIDC配置添加成功')
            setOidcModalVisible(false)

            onRefresh?.()
        } catch (error) {
            console.error('保存OIDC配置失败:', error)
            // message.error('保存OIDC配置失败')
            // 如果保存失败，回滚本地状态
        } finally {
            setOidcLoading(false)
        }
    }

    const handleOidcModalCancel = () => {
        setOidcModalVisible(false)
        setEditingOidc(null)
        oidcForm.resetFields()
    }

    const handleDeleteOidc = async (provider: string, oidcName: string) => {
        Modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined/>,
            content: `确定要删除OIDC配置 "${oidcName}" 吗？此操作不可恢复。`,
            okText: '确认删除',
            okType: 'danger',
            cancelText: '取消',
            async onOk() {
                try {
                    const updatedOidcConfigs = oidcConfigs.filter(config => config.provider !== provider)

                    await portalApi.updatePortal(portal.portalId, {
                        ...portal,
                        portalSettingConfig: {
                            ...portal.portalSettingConfig,
                            oidcConfigs: updatedOidcConfigs
                        }
                    })

                    message.success('OIDC配置删除成功')
                    onRefresh?.()
                } catch (error) {
                    console.error('删除OIDC配置失败:', error)
                    // message.error('删除OIDC配置失败')
                }
            },
        })
    }

    // OAuth2 配置处理函数
    const oauth2Configs = portal.portalSettingConfig?.oauth2Configs || []
    
    const handleAddOAuth2 = () => {
        setEditingOAuth2(null)
        setOAuth2ModalVisible(true)
        oauth2Form.resetFields()
        oauth2Form.setFieldsValue({
            grantType: GrantType.JWT_BEARER
        })
    }

    const handleEditOAuth2 = (config: OAuth2Config) => {
        setEditingOAuth2(config)
        setOAuth2ModalVisible(true)
        oauth2Form.setFieldsValue({
            ...config,
            userIdField: config.identityMapping?.userIdField,
            userNameField: config.identityMapping?.userNameField
        })
    }

    const handleOAuth2ModalOk = async () => {
        try {
            setOAuth2Loading(true)
            const values = await oauth2Form.validateFields()

            const configData: OAuth2Config = {
                provider: values.provider,
                name: values.name,
                grantType: values.grantType,
                jwtBearerConfig: values.grantType === GrantType.JWT_BEARER ? {
                    publicKeys: []
                } : undefined,
                identityMapping: {
                    userIdField: values.userIdField,
                    userNameField: values.userNameField
                }
            }

            let updatedOAuth2Configs
            if (editingOAuth2) {
                updatedOAuth2Configs = oauth2Configs.map(config => 
                    config.provider === editingOAuth2.provider ? configData : config
                )
            } else {
                updatedOAuth2Configs = [...oauth2Configs, configData]
            }

            await portalApi.updatePortal(portal.portalId, {
                ...portal,
                portalSettingConfig: {
                    ...portal.portalSettingConfig,
                    oauth2Configs: updatedOAuth2Configs
                }
            })

            message.success(editingOAuth2 ? 'OAuth2配置更新成功' : 'OAuth2配置添加成功')
            setOAuth2ModalVisible(false)
            onRefresh?.()
        } catch (error) {
            console.error('保存OAuth2配置失败:', error)
            message.error('保存OAuth2配置失败')
        } finally {
            setOAuth2Loading(false)
        }
    }

    const handleOAuth2ModalCancel = () => {
        setOAuth2ModalVisible(false)
        setEditingOAuth2(null)
        oauth2Form.resetFields()
    }

    const handleDeleteOAuth2 = async (provider: string, name: string) => {
        Modal.confirm({
            title: '确认删除',
            icon: <ExclamationCircleOutlined/>,
            content: `确定要删除OAuth2配置 "${name}" 吗？此操作不可恢复。`,
            okText: '确认删除',
            okType: 'danger',
            cancelText: '取消',
            async onOk() {
                try {
                    const updatedOAuth2Configs = oauth2Configs.filter(config => config.provider !== provider)

                    await portalApi.updatePortal(portal.portalId, {
                        ...portal,
                        portalSettingConfig: {
                            ...portal.portalSettingConfig,
                            oauth2Configs: updatedOAuth2Configs
                        }
                    })

                    message.success('OAuth2配置删除成功')
                    onRefresh?.()
                } catch (error) {
                    console.error('删除OAuth2配置失败:', error)
                    message.error('删除OAuth2配置失败')
                }
            },
        })
    }

    const handleManagePublicKeys = (provider: string) => {
        setCurrentOAuth2Provider(provider)
        setPublicKeyModalVisible(true)
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
                            icon={<DeleteOutlined/>}
                            onClick={() => handleDeleteDomain(record.domain)}
                        >
                            解绑
                        </Button>
                    )}
                </Space>
            )
        }
    ]

    // OIDC 配置表格列定义
    const oidcColumns = [
        {
            title: '提供商',
            dataIndex: 'provider',
            key: 'provider',
            render: (provider: string) => (
                <Tag color="blue">{provider}</Tag>
            )
        },
        {
            title: '名称',
            dataIndex: 'name',
            key: 'name',
        },
        {
            title: 'Client ID',
            key: 'clientId',
            render: (record: OidcConfig) => (
                <span className="font-mono text-xs">
          {record.authCodeConfig?.clientId?.substring(0, 20)}...
        </span>
            )
        },
        {
            title: '状态',
            dataIndex: 'enabled',
            key: 'enabled',
            render: (enabled: boolean) => (
                <Tag color={enabled ? 'green' : 'red'}>
                    {enabled ? '启用' : '禁用'}
                </Tag>
            )
        },
        {
            title: '操作',
            key: 'action',
            render: (_: any, record: OidcConfig) => (
                <Space>
                    <Button
                        type="link"
                        icon={<EditOutlined/>}
                        onClick={() => handleEditOidc(record)}
                    >
                        编辑
                    </Button>
                    <Button
                        type="link"
                        danger
                        icon={<DeleteOutlined/>}
                        onClick={() => handleDeleteOidc(record.provider, record.name)}
                    >
                        删除
                    </Button>
                </Space>
            )
        }
    ]

    // OAuth2 配置表格列定义
    const oauth2Columns = [
        {
            title: '提供商',
            dataIndex: 'provider',
            key: 'provider',
            render: (provider: string) => (
                <Tag color="green">{provider}</Tag>
            )
        },
        {
            title: '名称',
            dataIndex: 'name',
            key: 'name',
        },
        {
            title: '授权模式',
            dataIndex: 'grantType',
            key: 'grantType',
            render: (grantType: GrantType) => (
                <Tag color={grantType === GrantType.JWT_BEARER ? 'orange' : 'blue'}>
                    {grantType === GrantType.JWT_BEARER ? 'JWT Bearer' : '授权码模式'}
                </Tag>
            )
        },
        {
            title: '公钥配置',
            key: 'publicKeys',
            render: (record: OAuth2Config) => {
                const publicKeys = record.jwtBearerConfig?.publicKeys || []
                return record.grantType === GrantType.JWT_BEARER ? (
                    <span className="text-blue-600">
                        {publicKeys.length} 个公钥
                    </span>
                ) : (
                    <span className="text-gray-400">-</span>
                )
            }
        },
        {
            title: '操作',
            key: 'action',
            render: (_: any, record: OAuth2Config) => (
                <Space>
                    {record.grantType === GrantType.JWT_BEARER && (
                        <Button
                            type="link"
                            size="small"
                            onClick={() => handleManagePublicKeys(record.provider)}
                        >
                            公钥管理
                        </Button>
                    )}
                    <Button
                        type="link"
                        icon={<EditOutlined/>}
                        onClick={() => handleEditOAuth2(record)}
                    >
                        编辑
                    </Button>
                    <Button
                        type="link"
                        danger
                        icon={<DeleteOutlined/>}
                        onClick={() => handleDeleteOAuth2(record.provider, record.name)}
                    >
                        删除
                    </Button>
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
                        rules={[{required: true, message: '请输入Portal ID'}]}
                    >
                        <Input placeholder="请输入Portal ID"/>
                    </Form.Item>
                    {/* <Form.Item
            name="title"
            label="标题"
            rules={[{ required: true, message: '请输入Portal标题' }]}
          >
            <Input placeholder="请输入Portal标题" />
          </Form.Item> */}
                    <Form.Item
                        name="description"
                        label="描述"
                        className="col-span-2"
                    >
                        <Input.TextArea rows={3} placeholder="请输入Portal描述"/>
                    </Form.Item>
                </div>
            )
        },
        {
            key: 'auth',
            label: '安全设置',
            children: (
                <div className="space-y-6">
                    {/* 基本安全设置 */}
                    <div className="grid grid-cols-2 gap-6">
                        <Form.Item
                            name="builtinAuthEnabled"
                            label="账号密码登录"
                            valuePropName="checked"
                        >
                            <Switch
                                onChange={(checked) => handleSettingUpdate('builtinAuthEnabled', checked)}
                            />
                        </Form.Item>
                        {/* <Form.Item
              name="oidcAuthEnabled"
              label="OIDC认证"
              valuePropName="checked"
            >
              <Switch 
                onChange={(checked) => handleSettingUpdate('oidcAuthEnabled', checked)}
              />
            </Form.Item> */}
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
                        {/* <Form.Item
              name="frontendRedirectUrl"
              label="前端重定向URL"
              className="col-span-2"
            >
              <Input placeholder="http://portal.example.com/callback" />
            </Form.Item> */}
                    </div>

                    {/* OIDC 配置管理 */}
                    <Divider/>
                    <div>
                        <div className="flex justify-between items-center mb-4">
                            <div>
                                <h3 className="text-lg font-medium">OIDC 配置</h3>
                                <p className="text-sm text-gray-500">管理第三方身份提供商配置</p>
                            </div>
                            <Button
                                type="primary"
                                icon={<PlusOutlined/>}
                                onClick={handleAddOidc}
                            >
                                添加 OIDC 配置
                            </Button>
                        </div>
                        <Table
                            columns={oidcColumns}
                            dataSource={oidcConfigs}
                            rowKey="provider"
                            pagination={false}
                            size="small"
                        />
                    </div>

                    {/* OAuth2 配置管理 */}
                    <Divider/>
                    <div>
                        <div className="flex justify-between items-center mb-4">
                            <div>
                                <h3 className="text-lg font-medium">OAuth2 配置</h3>
                                <p className="text-sm text-gray-500">管理OAuth2集成配置，支持JWT Bearer模式</p>
                            </div>
                            <Button
                                type="primary"
                                icon={<PlusOutlined/>}
                                onClick={handleAddOAuth2}
                            >
                                添加 OAuth2 配置
                            </Button>
                        </div>
                        <Table
                            columns={oauth2Columns}
                            dataSource={oauth2Configs}
                            rowKey="provider"
                            pagination={false}
                            size="small"
                        />
                    </div>
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
                            icon={<PlusOutlined/>}
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
        // {
        //   key: 'features',
        //   label: '功能开关',
        //   children: (
        //     <div className="space-y-4">
        //       <div className="flex justify-between items-center">
        //         <div>
        //           <div className="font-medium">分析统计</div>
        //           <div className="text-sm text-gray-500">启用访问统计和分析功能</div>
        //         </div>
        //         <Form.Item name="enableAnalytics" valuePropName="checked" noStyle>
        //           <Switch
        //             onChange={(checked) => handleSettingUpdate('enableAnalytics', checked)}
        //           />
        //         </Form.Item>
        //       </div>
        //       <Divider />
        //       <div className="flex justify-between items-center">
        //         <div>
        //           <div className="font-medium">通知系统</div>
        //           <div className="text-sm text-gray-500">启用邮件和Webhook通知</div>
        //         </div>
        //         <Form.Item name="enableNotifications" valuePropName="checked" noStyle>
        //           <Switch
        //             onChange={(checked) => handleSettingUpdate('enableNotifications', checked)}
        //           />
        //         </Form.Item>
        //       </div>
        //       <Divider />
        //       <div className="flex justify-between items-center">
        //         <div>
        //           <div className="font-medium">审计日志</div>
        //           <div className="text-sm text-gray-500">记录用户操作和系统事件</div>
        //         </div>
        //         <Form.Item name="enableAuditLog" valuePropName="checked" noStyle>
        //           <Switch
        //             onChange={(checked) => handleSettingUpdate('enableAuditLog', checked)}
        //           />
        //         </Form.Item>
        //       </div>
        //       <Divider />
        //       <div className="flex justify-between items-center">
        //         <div>
        //           <div className="font-medium">限流控制</div>
        //           <div className="text-sm text-gray-500">启用API调用频率限制</div>
        //         </div>
        //         <Form.Item name="enableRateLimiting" valuePropName="checked" noStyle>
        //           <Switch
        //             onChange={(checked) => handleSettingUpdate('enableRateLimiting', checked)}
        //           />
        //         </Form.Item>
        //       </div>
        //       <Divider />
        //       <div className="flex justify-between items-center">
        //         <div>
        //           <div className="font-medium">缓存</div>
        //           <div className="text-sm text-gray-500">启用响应缓存以提高性能</div>
        //         </div>
        //         <Form.Item name="enableCaching" valuePropName="checked" noStyle>
        //           <Switch
        //             onChange={(checked) => handleSettingUpdate('enableCaching', checked)}
        //           />
        //         </Form.Item>
        //       </div>
        //       <Divider />
        //       <div className="flex justify-between items-center">
        //         <div>
        //           <div className="font-medium">压缩</div>
        //           <div className="text-sm text-gray-500">启用响应压缩以减少带宽</div>
        //         </div>
        //         <Form.Item name="enableCompression" valuePropName="checked" noStyle>
        //           <Switch
        //             onChange={(checked) => handleSettingUpdate('enableCompression', checked)}
        //           />
        //         </Form.Item>
        //       </div>
        //       <Divider />
        //       <div className="flex justify-between items-center">
        //         <div>
        //           <div className="font-medium">SSL/TLS</div>
        //           <div className="text-sm text-gray-500">强制使用HTTPS连接</div>
        //         </div>
        //         <Form.Item name="enableSSL" valuePropName="checked" noStyle>
        //           <Switch
        //             onChange={(checked) => handleSettingUpdate('enableSSL', checked)}
        //           />
        //         </Form.Item>
        //       </div>
        //       <Divider />
        //       <div className="flex justify-between items-center">
        //         <div>
        //           <div className="font-medium">CORS</div>
        //           <div className="text-sm text-gray-500">启用跨域资源共享</div>
        //         </div>
        //         <Form.Item name="enableCORS" valuePropName="checked" noStyle>
        //           <Switch
        //             onChange={(checked) => handleSettingUpdate('enableCORS', checked)}
        //           />
        //         </Form.Item>
        //       </div>
        //     </div>
        //   )
        // }
    ]

    return (
        <div className="p-6 space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h1 className="text-2xl font-bold mb-2">设置</h1>
                    <p className="text-gray-600">配置Portal的基本设置和高级选项</p>
                </div>
                <Space>
                    {/* <Button icon={<ReloadOutlined />} onClick={handleReset}>
            重置
          </Button> */}
                    <Button type="primary" icon={<SaveOutlined/>} loading={loading} onClick={handleSave}>
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
                    portalSettingConfig: portal.portalSettingConfig,
                    builtinAuthEnabled: portal.portalSettingConfig?.builtinAuthEnabled,
                    oidcAuthEnabled: portal.portalSettingConfig?.oidcAuthEnabled,
                    autoApproveDevelopers: portal.portalSettingConfig?.autoApproveDevelopers,
                    autoApproveSubscriptions: portal.portalSettingConfig?.autoApproveSubscriptions,
                    frontendRedirectUrl: portal.portalSettingConfig?.frontendRedirectUrl,
                    portalDomainConfig: portal.portalDomainConfig,
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
                            {required: true, message: '请输入域名'},
                            {
                                pattern: /^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/,
                                message: '请输入有效的域名格式'
                            }
                        ]}
                    >
                        <Input placeholder="example.com"/>
                    </Form.Item>
                    <Form.Item
                        name="protocol"
                        label="协议"
                        rules={[{required: true, message: '请选择协议'}]}
                    >
                        <Select placeholder="请选择协议">
                            <Select.Option value="HTTP">HTTP</Select.Option>
                            <Select.Option value="HTTPS">HTTPS</Select.Option>
                        </Select>
                    </Form.Item>
                </Form>
            </Modal>

            {/* OIDC 配置模态框 */}
            <Modal
                title={editingOidc ? '编辑 OIDC 配置' : '添加 OIDC 配置'}
                open={oidcModalVisible}
                onOk={handleOidcModalOk}
                onCancel={handleOidcModalCancel}
                confirmLoading={oidcLoading}
                width={800}
                okText={editingOidc ? '更新' : '添加'}
                cancelText="取消"
            >
                <Form
                    form={oidcForm}
                    layout="vertical"
                >
                    <div className="grid grid-cols-2 gap-4">
                        <Form.Item
                            name="provider"
                            label="提供商"
                            rules={[{required: true, message: '请输入提供商名称'}]}
                        >
                            <Input
                                placeholder="如: aliyun, google, github, 唯一标识"
                                disabled={editingOidc !== null}
                            />
                        </Form.Item>
                        <Form.Item
                            name="name"
                            label="显示名称"
                            rules={[{required: true, message: '请输入显示名称'}]}
                        >
                            <Input placeholder="如: 阿里云登录"/>
                        </Form.Item>
                    </div>

                    <Divider/>

                     {/* 授权模式选择 */}
                     <Form.Item
                         name="grantType"
                         label="授权模式"
                         initialValue="授权码模式"
                     >
                         <Select disabled>
                             <Select.Option value="AUTHORIZATION_CODE">授权码模式</Select.Option>
                         </Select>
                     </Form.Item>

                     {/* OIDC授权码模式配置字段 */}
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

                     {/* 授权范围，与其他字段保持对齐 */}
                     <div className="grid grid-cols-2 gap-4">
                         <Form.Item
                             name="scopes"
                             label="授权范围"
                             rules={[{required: true, message: '请输入授权范围'}]}
                         >
                             <Input placeholder="如: openid profile email"/>
                         </Form.Item>
                         <div></div> {/* 空占位符，保持对齐 */}
                     </div>

                     <Divider/>

                     {/* 授权端点和令牌端点一行 */}
                     <div className="grid grid-cols-2 gap-4">
                         <Form.Item
                             name="authorizationEndpoint"
                             label="授权端点"
                             rules={[{required: true, message: '请输入授权端点'}]}
                         >
                             <Input placeholder="Authorization 授权端点，用于获取授权码。"/>
                         </Form.Item>

                         <Form.Item
                             name="tokenEndpoint"
                             label="令牌端点"
                             rules={[{required: true, message: '请输入令牌端点'}]}
                         >
                             <Input placeholder="Token 令牌端点，用于使用授权码换取令牌。"/>
                         </Form.Item>
                     </div>

                     {/* 用户信息端点和JWK集合URI一行 */}
                     <div className="grid grid-cols-2 gap-4">
                         <Form.Item
                             name="userInfoEndpoint"
                             label="用户信息端点"
                             rules={[{required: true, message: '请输入用户信息端点'}]}
                         >
                             <Input placeholder="UserInfo 端点，用于获取用户基本信息。"/>
                         </Form.Item>
                         <Form.Item
                             name="jwkSetUri"
                             label="公钥端点"
                         >
                             <Input placeholder="JWKS 公钥端点，用于校验 id_token 来源有效性。(可选)"/>
                         </Form.Item>
                     </div>

                    {/* Logo URL 字段暂时隐藏 */}
                    {false && (
                        <Form.Item
                            name="logoUrl"
                            label="Logo URL"
                        >
                            <Input placeholder="提供商 Logo 图片地址 (可选)"/>
                        </Form.Item>
                    )}

                    <Form.Item
                        name="enabled"
                        label="启用状态"
                        valuePropName="checked"
                    >
                        <Switch/>
                    </Form.Item>
                </Form>
            </Modal>

            {/* OAuth2 配置模态框 */}
            <Modal
                title={editingOAuth2 ? '编辑 OAuth2 配置' : '添加 OAuth2 配置'}
                open={oauth2ModalVisible}
                onOk={handleOAuth2ModalOk}
                onCancel={handleOAuth2ModalCancel}
                confirmLoading={oauth2Loading}
                width={800}
                okText={editingOAuth2 ? '更新' : '添加'}
                cancelText="取消"
            >
                <Form
                    form={oauth2Form}
                    layout="vertical"
                >
                    <div className="grid grid-cols-2 gap-4">
                        <Form.Item
                            name="provider"
                            label="提供商标识"
                            rules={[{required: true, message: '请输入提供商标识'}]}
                        >
                            <Input
                                placeholder="如: auth-service, company-sso"
                                disabled={editingOAuth2 !== null}
                            />
                        </Form.Item>
                        <Form.Item
                            name="name"
                            label="显示名称"
                            rules={[{required: true, message: '请输入显示名称'}]}
                        >
                            <Input placeholder="如: 公司认证服务"/>
                        </Form.Item>
                    </div>

                    <Divider/>

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

                    <Divider orientation="left">身份映射配置</Divider>

                    <div className="grid grid-cols-2 gap-4">
                        <Form.Item
                            name="userIdField"
                            label="用户ID字段"
                            rules={[{required: true, message: '请输入JWT中的用户ID字段'}]}
                        >
                            <Input placeholder="如: sub, user_id"/>
                        </Form.Item>
                        <Form.Item
                            name="userNameField"
                            label="用户名字段"
                            rules={[{required: true, message: '请输入JWT中的用户名字段'}]}
                        >
                            <Input placeholder="如: name, display_name"/>
                        </Form.Item>
                    </div>

                    <div className="bg-blue-50 p-4 rounded-lg mt-4">
                        <div className="flex items-start space-x-2">
                            <div className="text-blue-600 mt-0.5">
                                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                                    <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
                                </svg>
                            </div>
                            <div>
                                <h4 className="text-blue-800 font-medium">配置说明</h4>
                                <p className="text-blue-700 text-sm mt-1">
                                    JWT Bearer模式要求客户端使用JWT令牌进行认证。配置完成后，请通过"公钥管理"功能添加用于验证JWT签名的公钥。
                                    身份映射配置用于从JWT载荷中提取用户信息。
                                </p>
                            </div>
                        </div>
                    </div>
                </Form>
            </Modal>

            {/* 公钥管理模态框 */}
            <Modal
                title="公钥管理"
                open={publicKeyModalVisible}
                onCancel={() => setPublicKeyModalVisible(false)}
                width={900}
                footer={null}
            >
                <PublicKeyManager
                    provider={currentOAuth2Provider}
                    onSave={async (publicKeys) => {
                        try {
                            const updatedConfigs = oauth2Configs.map(config => 
                                config.provider === currentOAuth2Provider 
                                    ? {
                                        ...config,
                                        jwtBearerConfig: {
                                            ...config.jwtBearerConfig,
                                            publicKeys
                                        }
                                    }
                                    : config
                            )
                            
                            await portalApi.updatePortal(portal.portalId, {
                                ...portal,
                                portalSettingConfig: {
                                    ...portal.portalSettingConfig,
                                    oauth2Configs: updatedConfigs
                                }
                            })
                            
                            message.success('公钥配置保存成功')
                            setPublicKeyModalVisible(false)
                            onRefresh?.()
                        } catch (error) {
                            console.error('保存公钥配置失败:', error)
                            message.error('保存公钥配置失败')
                        }
                    }}
                    publicKeys={
                        oauth2Configs
                            .find(c => c.provider === currentOAuth2Provider)
                            ?.jwtBearerConfig?.publicKeys || []
                    }
                />
            </Modal>
        </div>
    )
} 