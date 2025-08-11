import { useState, useEffect } from "react";
import {
  Card,
  Button,
  message,
  Tabs,
  Modal,
  Radio,
  Input,
  Table,
  Popconfirm,
  Select,
    Form,
} from "antd";
import {
  PlusOutlined,
  InfoCircleOutlined,
  CopyOutlined,
  DeleteOutlined,
  EditOutlined
} from "@ant-design/icons";
import api from "../../lib/api";
import type { ConsumerCredentialResult, CreateCredentialParam } from "../../types/consumer";
import type { ApiResponse } from "../../types";

interface CredentialManagerProps {
  consumerId: string;
}

export function CredentialManager({ consumerId }: CredentialManagerProps) {
  const [credentialType, setCredentialType] = useState<'API_KEY' | 'HMAC'>('API_KEY');
  const [credentialModalVisible, setCredentialModalVisible] = useState(false);
  const [credentialLoading, setCredentialLoading] = useState(false);
  const [generationMethod, setGenerationMethod] = useState<'SYSTEM' | 'CUSTOM'>('SYSTEM');
  const [customApiKey, setCustomApiKey] = useState('');
  const [customAccessKey, setCustomAccessKey] = useState('');
  const [customSecretKey, setCustomSecretKey] = useState('');
  const [sourceModalVisible, setSourceModalVisible] = useState(false);
  const [editingSource, setEditingSource] = useState<string>('Default');
  const [editingKey, setEditingKey] = useState<string>('Authorization');
  // 已保存（展示用）与编辑中的两套状态，取消时回滚到已保存值
  const [currentSource, setCurrentSource] = useState<string>('Default');
  const [currentKey, setCurrentKey] = useState<string>('Authorization');
  // 表单（编辑凭证来源）
  const [sourceForm] = Form.useForm();
  // 当前完整配置（驱动表格数据源）
  const [currentConfig, setCurrentConfig] = useState<ConsumerCredentialResult | null>(null);

  // 初始化时获取当前配置
  const fetchCurrentConfig = async () => {
    try {
      const response: ApiResponse<ConsumerCredentialResult> = await api.get(`/consumers/${consumerId}/credentials`);
      if (response.code === "SUCCESS" && response.data) {
        const config = response.data;
        setCurrentConfig(config);
        if (config.apiKeyConfig) {
          setCurrentSource(config.apiKeyConfig.source || 'Default');
          setCurrentKey(config.apiKeyConfig.key || 'Authorization');
        }
      }
    } catch (error) {
      console.error('获取当前配置失败:', error);
    }
  };

  // 组件挂载时获取配置
  useEffect(() => {
    fetchCurrentConfig();
  }, [consumerId]);

  const handleCreateCredential = async () => {
    setCredentialLoading(true);
    try {
      // 先获取当前的凭证配置
      const currentResponse: ApiResponse<ConsumerCredentialResult> = await api.get(`/consumers/${consumerId}/credentials`);
      let currentConfig: ConsumerCredentialResult = {};
      
      if (currentResponse.code === "SUCCESS" && currentResponse.data) {
        currentConfig = currentResponse.data;
      }

      // 构建新的凭证配置
      const param: CreateCredentialParam = {};
      
      if (credentialType === 'API_KEY') {
        param.apiKeyConfig = {
          ...currentConfig.apiKeyConfig,
          credentials: [{
            apiKey: generationMethod === 'CUSTOM' ? customApiKey : undefined,
            mode: generationMethod
          }]
        };
        // 保留其他类型的凭证
        if (currentConfig.hmacConfig) {
          param.hmacConfig = {
            credentials: currentConfig.hmacConfig.credentials?.map(cred => ({
              ak: cred.ak,
              sk: cred.sk,
              mode: 'SYSTEM' as const
            }))
          };
        }
        if (currentConfig.jwtConfig) {
          param.jwtConfig = currentConfig.jwtConfig;
        }
      } else if (credentialType === 'HMAC') {
        param.hmacConfig = {
          credentials: [{
            ak: generationMethod === 'CUSTOM' ? customAccessKey : undefined,
            sk: generationMethod === 'CUSTOM' ? customSecretKey : undefined,
            mode: generationMethod
          }]
        };
        // 保留其他类型的凭证
        if (currentConfig.apiKeyConfig) {
          param.apiKeyConfig = {
            ...currentConfig.apiKeyConfig,
            credentials: currentConfig.apiKeyConfig.credentials?.map(cred => ({
              apiKey: cred.apiKey,
              mode: 'SYSTEM' as const
            }))
          };
        }
        if (currentConfig.jwtConfig) {
          param.jwtConfig = currentConfig.jwtConfig;
        }
      }

      const response: ApiResponse<ConsumerCredentialResult> = await api.post(`/consumers/${consumerId}/credentials`, param);
      if (response?.code === "SUCCESS") {
        message.success('凭证创建成功');
        setCredentialModalVisible(false);
        resetCredentialForm();
        // 刷新当前配置以驱动表格
        await fetchCurrentConfig();
      }
    } catch (error) {
      console.error('创建凭证失败:', error);
      message.error('创建凭证失败');
    } finally {
      setCredentialLoading(false);
    }
  };

  const handleDeleteCredential = async (credentialType: string) => {
    try {
      // 先获取当前的凭证配置
      const currentResponse: ApiResponse<ConsumerCredentialResult> = await api.get(`/consumers/${consumerId}/credentials`);
      let currentConfig: ConsumerCredentialResult = {};
      
      if (currentResponse.code === "SUCCESS" && currentResponse.data) {
        currentConfig = currentResponse.data;
      }

      // 构建删除后的凭证配置，清空对应类型的凭证
      const param: CreateCredentialParam = {};
      
      if (credentialType === 'API_KEY') {
        param.apiKeyConfig = {
          credentials: [],
          source: currentConfig.apiKeyConfig?.source || 'Default',
          key: currentConfig.apiKeyConfig?.key || 'Authorization'
        };
        // 保留其他类型的凭证
        if (currentConfig.hmacConfig) {
          param.hmacConfig = {
            credentials: currentConfig.hmacConfig.credentials?.map(cred => ({
              ak: cred.ak,
              sk: cred.sk,
              mode: 'SYSTEM' as const
            }))
          };
        }
        if (currentConfig.jwtConfig) {
          param.jwtConfig = currentConfig.jwtConfig;
        }
      } else if (credentialType === 'HMAC') {
        param.hmacConfig = {
          credentials: []
        };
        // 保留其他类型的凭证
        if (currentConfig.apiKeyConfig) {
          param.apiKeyConfig = {
            ...currentConfig.apiKeyConfig,
            credentials: currentConfig.apiKeyConfig.credentials?.map(cred => ({
              apiKey: cred.apiKey,
              mode: 'SYSTEM' as const
            }))
          };
        }
        if (currentConfig.jwtConfig) {
          param.jwtConfig = currentConfig.jwtConfig;
        }
      }

      const response: ApiResponse<ConsumerCredentialResult> = await api.post(`/consumers/${consumerId}/credentials`, param);
      if (response?.code === "SUCCESS") {
        message.success('凭证删除成功');
        await fetchCurrentConfig();
      }
    } catch (error) {
      console.error('删除凭证失败:', error);
      message.error('删除凭证失败');
    }
  };

  const handleCopyCredential = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      message.success('已复制到剪贴板');
    } catch {
      message.error('复制失败');
    }
  };

  const resetCredentialForm = () => {
    setGenerationMethod('SYSTEM');
    setCustomApiKey('');
    setCustomAccessKey('');
    setCustomSecretKey('');
  };

  const handleEditSource = async () => {
    try {
      // 先获取当前的凭证配置
      const currentResponse: ApiResponse<ConsumerCredentialResult> = await api.get(`/consumers/${consumerId}/credentials`);
      let currentConfig: ConsumerCredentialResult = {};
      
      if (currentResponse.code === "SUCCESS" && currentResponse.data) {
        currentConfig = currentResponse.data as ConsumerCredentialResult;
      }

      // 构建新的凭证配置
      const param: CreateCredentialParam = {};
      
      // 更新API Key配置的source和key
      if (currentConfig.apiKeyConfig) {
        param.apiKeyConfig = {
          source: editingSource,
          key: editingSource === 'Default' ? 'Authorization' : editingKey,
          credentials: currentConfig.apiKeyConfig.credentials
        };
      } else {
        param.apiKeyConfig = {
          source: editingSource,
          key: editingSource === 'Default' ? 'Authorization' : editingKey,
          credentials: []
        };
      }

      

      // 提交配置到后端
      const response: ApiResponse<ConsumerCredentialResult> = await api.post(`/consumers/${consumerId}/credentials`, param);
      if (response?.code === "SUCCESS") {
        message.success('凭证来源更新成功');
        
        // 重新查询接口获取最新配置，确保数据落盘
        const updatedResponse: ApiResponse<ConsumerCredentialResult> = await api.get(`/consumers/${consumerId}/credentials`);
        if (updatedResponse.code === "SUCCESS" && updatedResponse.data) {
          const updatedConfig = updatedResponse.data;
          if (updatedConfig.apiKeyConfig) {
            setCurrentSource(updatedConfig.apiKeyConfig.source || 'Default');
            setCurrentKey(updatedConfig.apiKeyConfig.key || 'Authorization');
          }
        }
        
        setSourceModalVisible(false);
        await fetchCurrentConfig();
      }
    } catch (error) {
      console.error('更新凭证来源失败:', error);
      message.error('更新凭证来源失败');
    }
  };

  const openSourceModal = () => {
    // 打开弹窗前将已保存值拷贝到编辑态和表单
    const initSource = currentSource;
    const initKey = initSource === 'Default' ? 'Authorization' : currentKey;
    setEditingSource(initSource);
    setEditingKey(initKey);
    sourceForm.setFieldsValue({ source: initSource, key: initKey });
    setSourceModalVisible(true);
  };

  // API Key 列
  const apiKeyColumns = [
    {
      title: 'API Key',
      dataIndex: 'apiKey',
      key: 'apiKey',
      render: (apiKey: string) => (
        <div className="flex items-center space-x-2">
          <code className="text-sm bg-gray-100 px-2 py-1 rounded">{apiKey}</code>
          <Button type="text" size="small" icon={<CopyOutlined />} onClick={() => handleCopyCredential(apiKey)} />
        </div>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createAt',
      key: 'createAt',
      render: (date: string) => (date ? new Date(date).toLocaleString() : '-'),
    },
    {
      title: '操作',
      key: 'action',
      render: () => (
        <Popconfirm title="确定要删除所有API Key凭证吗？" onConfirm={() => handleDeleteCredential('API_KEY')}>
          <Button type="link" danger size="small" icon={<DeleteOutlined />}>删除</Button>
        </Popconfirm>
      ),
    },
  ];

  // HMAC 列
  const hmacColumns = [
    {
      title: 'Access Key',
      dataIndex: 'ak',
      key: 'ak',
      render: (ak: string) => (
        <div className="flex items-center space-x-2">
          <code className="text-sm bg-gray-100 px-2 py-1 rounded">{ak}</code>
          <Button type="text" size="small" icon={<CopyOutlined />} onClick={() => handleCopyCredential(ak)} />
        </div>
      ),
    },
    {
      title: 'Secret Key',
      dataIndex: 'sk',
      key: 'sk',
      render: (sk: string) => (
        <div className="flex items-center space-x-2">
          <code className="text-sm bg-gray-100 px-2 py-1 rounded">{sk}</code>
          <Button type="text" size="small" icon={<CopyOutlined />} onClick={() => handleCopyCredential(sk)} />
        </div>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createAt',
      key: 'createAt',
      render: (date: string) => (date ? new Date(date).toLocaleString() : '-'),
    },
    {
      title: '操作',
      key: 'action',
      render: () => (
        <Popconfirm title="确定要删除所有AK/SK凭证吗？" onConfirm={() => handleDeleteCredential('HMAC')}>
          <Button type="link" danger size="small" icon={<DeleteOutlined />}>删除</Button>
        </Popconfirm>
      ),
    },
  ];

  return (
    <>
      <Card title="认证方式">
        <Tabs defaultActiveKey="API_KEY">
          <Tabs.TabPane tab="API Key" key="API_KEY">
            <div className="mb-4">
              <div className="flex items-start space-x-2 mb-4">
                <InfoCircleOutlined className="text-blue-500 mt-1" />
                <div className="text-sm text-gray-600">
                  API Key是一种简单的认证方式，客户端需要在请求中添加凭证，网关会验证API Key的合法性和权限。
                  API Key常用于简单场景，不涉及敏感操作，安全性相对较低，请注意凭证的管理与保护。
                </div>
              </div>
              
              {/* 凭证来源配置（展示已保存值）*/}
              <div className="mb-4 p-3 bg-gray-50 rounded border">
                <div className="flex items-center gap-2 mb-2">
                  <span className="text-sm font-medium text-gray-700">凭证来源</span>
                  <Button type="link" size="small" icon={<EditOutlined />} onClick={openSourceModal}>
                    编辑
                  </Button>
                </div>
                <div className="text-sm text-gray-600">
                  {currentSource === 'Default' ? 'Authorization: Bearer <token>' : `${currentKey}: <value>`}
                </div>
              </div>
              
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => {
                  setCredentialType('API_KEY');
                  setCredentialModalVisible(true);
                }}
              >
                添加凭证
              </Button>
            </div>
            <Table
              columns={apiKeyColumns}
              dataSource={currentConfig?.apiKeyConfig?.credentials || []}
              rowKey={(record) => record.apiKey || Math.random().toString()}
              pagination={false}
              size="small"
              locale={{ emptyText: '暂无API Key凭证，请点击上方按钮创建' }}
            />
          </Tabs.TabPane>

          <Tabs.TabPane tab="HMAC" key="HMAC">
            <div className="mb-4">
              <div className="flex items-start space-x-2 mb-4">
                <InfoCircleOutlined className="text-blue-500 mt-1" />
                <div className="text-sm text-gray-600">
                  一种基于HMAC算法的AK/SK签名认证方式。客户端在调用API时，需要使用签名密钥对请求内容进行签名计算，
                  并将签名同步传输给服务器端进行签名验证。
                </div>
              </div>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => {
                  setCredentialType('HMAC');
                  setCredentialModalVisible(true);
                }}
              >
                添加AK/SK
              </Button>
            </div>
            <Table
              columns={hmacColumns}
              dataSource={currentConfig?.hmacConfig?.credentials || []}
              rowKey={(record) => record.ak || record.sk || Math.random().toString()}
              pagination={false}
              size="small"
              locale={{ emptyText: '暂无AK/SK凭证，请点击上方按钮创建' }}
            />
          </Tabs.TabPane>

          <Tabs.TabPane tab="JWT" key="JWT" disabled>
            <div className="text-center py-8 text-gray-500">
              JWT功能暂未开放
            </div>
          </Tabs.TabPane>
        </Tabs>
      </Card>

      {/* 创建凭证模态框 */}
      <Modal
        title={`添加 ${credentialType === 'API_KEY' ? 'API Key' : 'AK/SK'}`}
        open={credentialModalVisible}
        onCancel={() => {
          setCredentialModalVisible(false);
          resetCredentialForm();
        }}
        onOk={handleCreateCredential}
        confirmLoading={credentialLoading}
        okText="添加"
        cancelText="取消"
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              <span className="text-red-500">*</span> 生成方式
            </label>
            <Radio.Group value={generationMethod} onChange={(e) => setGenerationMethod(e.target.value)}>
              <Radio value="SYSTEM">系统生成</Radio>
              <Radio value="CUSTOM">自定义</Radio>
            </Radio.Group>
          </div>

          {generationMethod === 'CUSTOM' && (
            <div className="space-y-4">
              {credentialType === 'API_KEY' && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    API Key
                  </label>
                  <Input
                    placeholder="请输入自定义API Key"
                    value={customApiKey}
                    onChange={(e) => setCustomApiKey(e.target.value)}
                  />
                </div>
              )}
              {credentialType === 'HMAC' && (
                <>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Access Key
                    </label>
                    <Input
                      placeholder="请输入自定义Access Key"
                      value={customAccessKey}
                      onChange={(e) => setCustomAccessKey(e.target.value)}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Secret Key
                    </label>
                    <Input
                      placeholder="请输入自定义Secret Key"
                      value={customSecretKey}
                      onChange={(e) => setCustomSecretKey(e.target.value)}
                    />
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      </Modal>

      {/* 编辑凭证来源模态框 */}
      <Modal
        title="编辑凭证来源"
        open={sourceModalVisible}
        onCancel={() => {
          // 取消不落盘，回退到已保存值并重置表单
          const initSource = currentSource;
          const initKey = initSource === 'Default' ? 'Authorization' : currentKey;
          setEditingSource(initSource);
          setEditingKey(initKey);
          sourceForm.resetFields();
          setSourceModalVisible(false);
        }}
        onOk={async () => {
          try {
            const values = await sourceForm.validateFields();
            setEditingSource(values.source);
            setEditingKey(values.key);
            await handleEditSource();
          } catch {
            // 校验失败，不提交
          }
        }}
        okText="保存"
        cancelText="取消"
      >
        <Form form={sourceForm} layout="vertical" initialValues={{ source: editingSource, key: editingKey }}>
          <Form.Item
            label="凭证来源"
            name="source"
            rules={[{ required: true, message: '请选择凭证来源' }]}
          >
            <Select
              onChange={(value) => {
                const nextKey = value === 'Default' ? 'Authorization' : '';
                sourceForm.setFieldsValue({ key: nextKey });
              }}
              style={{ width: '100%' }}
            >
              <Select.Option value="Header">Header</Select.Option>
              <Select.Option value="QueryString">QueryString</Select.Option>
              <Select.Option value="Default">默认</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item noStyle shouldUpdate={(prev, curr) => prev.source !== curr.source}>
            {({ getFieldValue }) =>
              getFieldValue('source') !== 'Default' ? (
                <Form.Item
                  label="键名"
                  name="key"
                  rules={[
                    {
                      required: true,
                      message: '请输入键名',
                    },
                    {
                      pattern: /^[A-Za-z0-9-_]+$/,
                      message: '仅支持字母/数字/-/_',
                    },
                  ]}
                >
                  <Input placeholder="请输入键名" />
                </Form.Item>
              ) : null
            }
          </Form.Item>

          <div className="text-sm text-gray-500">
            <div>说明：</div>
            <div>• Header: 凭证放在HTTP请求头中</div>
            <div>• QueryString: 凭证放在URL查询参数中</div>
            <div>• Default: 使用标准的Authorization头</div>
          </div>
        </Form>
      </Modal>
    </>
  );
} 