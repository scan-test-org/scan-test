import { useState } from "react";
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
} from "antd";
import {
  PlusOutlined,
  InfoCircleOutlined,
  CopyOutlined,
  DeleteOutlined,
} from "@ant-design/icons";
import api from "../../lib/api";
import type { Credential, ConsumerCredentialResult, CreateCredentialParam } from "../../types/consumer";

interface CredentialManagerProps {
  consumerId: string;
  credentials: Credential[];
  onCredentialsChange: () => void;
}

export function CredentialManager({ consumerId, credentials, onCredentialsChange }: CredentialManagerProps) {
  const [credentialType, setCredentialType] = useState<'API_KEY' | 'HMAC'>('API_KEY');
  const [credentialModalVisible, setCredentialModalVisible] = useState(false);
  const [credentialLoading, setCredentialLoading] = useState(false);
  const [generationMethod, setGenerationMethod] = useState<'SYSTEM' | 'CUSTOM'>('SYSTEM');
  const [customApiKey, setCustomApiKey] = useState('');
  const [customAccessKey, setCustomAccessKey] = useState('');
  const [customSecretKey, setCustomSecretKey] = useState('');

  const handleCreateCredential = async () => {
    setCredentialLoading(true);
    try {
      // 先获取当前的凭证配置
      const currentResponse: any = await api.get(`/consumers/${consumerId}/credentials`);
      let currentConfig: ConsumerCredentialResult = {};
      
      if (currentResponse?.code === "SUCCESS" && currentResponse?.data) {
        currentConfig = currentResponse.data;
      }

      // 构建新的凭证配置
      const param: CreateCredentialParam = {};
      
      if (credentialType === 'API_KEY') {
        param.apiKeyConfig = {
          ...currentConfig.apiKeyConfig,
          generationMethod,
          customApiKey: generationMethod === 'CUSTOM' ? customApiKey : undefined
        };
        // 保留其他类型的凭证
        if (currentConfig.hmacConfig) {
          param.hmacConfig = currentConfig.hmacConfig;
        }
        if (currentConfig.jwtConfig) {
          param.jwtConfig = currentConfig.jwtConfig;
        }
      } else if (credentialType === 'HMAC') {
        param.hmacConfig = {
          ...currentConfig.hmacConfig,
          generationMethod,
          customAccessKey: generationMethod === 'CUSTOM' ? customAccessKey : undefined,
          customSecretKey: generationMethod === 'CUSTOM' ? customSecretKey : undefined
        };
        // 保留其他类型的凭证
        if (currentConfig.apiKeyConfig) {
          param.apiKeyConfig = currentConfig.apiKeyConfig;
        }
        if (currentConfig.jwtConfig) {
          param.jwtConfig = currentConfig.jwtConfig;
        }
      }

      const response: any = await api.post(`/consumers/${consumerId}/credentials`, param);
      if (response?.code === "SUCCESS") {
        message.success('凭证创建成功');
        setCredentialModalVisible(false);
        resetCredentialForm();
        onCredentialsChange();
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
      const currentResponse: any = await api.get(`/consumers/${consumerId}/credentials`);
      let currentConfig: ConsumerCredentialResult = {};
      
      if (currentResponse?.code === "SUCCESS" && currentResponse?.data) {
        currentConfig = currentResponse.data;
      }

      // 构建删除后的凭证配置，清空对应类型的凭证
      const param: CreateCredentialParam = {};
      
      if (credentialType === 'API_KEY') {
        param.apiKeyConfig = {
          credentials: [],
          source: currentConfig.apiKeyConfig?.source || 'Authorization: Bearer <token>'
        };
        // 保留其他类型的凭证
        if (currentConfig.hmacConfig) {
          param.hmacConfig = currentConfig.hmacConfig;
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
          param.apiKeyConfig = currentConfig.apiKeyConfig;
        }
        if (currentConfig.jwtConfig) {
          param.jwtConfig = currentConfig.jwtConfig;
        }
      }

      const response: any = await api.post(`/consumers/${consumerId}/credentials`, param);
      if (response?.code === "SUCCESS") {
        message.success('凭证删除成功');
        onCredentialsChange();
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

  const credentialColumns = [
    {
      title: '凭证类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => {
        const typeMap = {
          'API_KEY': 'API Key',
          'HMAC': 'AK/SK',
          'JWT': 'JWT'
        };
        return typeMap[type as keyof typeof typeMap] || type;
      }
    },
    {
      title: '凭证',
      key: 'credential',
      render: (record: Credential) => {
        if (record.type === 'API_KEY' && record.apiKey) {
          return (
            <div className="flex items-center space-x-2">
              <code className="text-sm bg-gray-100 px-2 py-1 rounded">{record.apiKey}</code>
              <Button
                type="text"
                size="small"
                icon={<CopyOutlined />}
                onClick={() => handleCopyCredential(record.apiKey!)}
              />
            </div>
          );
        } else if (record.type === 'HMAC' && record.accessKey && record.secretKey) {
          return (
            <div className="space-y-1">
              <div className="flex items-center space-x-2">
                <span className="text-xs text-gray-500">Access Key:</span>
                <code className="text-sm bg-gray-100 px-2 py-1 rounded">{record.accessKey}</code>
                <Button
                  type="text"
                  size="small"
                  icon={<CopyOutlined />}
                  onClick={() => handleCopyCredential(record.accessKey!)}
                />
              </div>
              <div className="flex items-center space-x-2">
                <span className="text-xs text-gray-500">Secret Key:</span>
                <code className="text-sm bg-gray-100 px-2 py-1 rounded">{record.secretKey}</code>
                <Button
                  type="text"
                  size="small"
                  icon={<CopyOutlined />}
                  onClick={() => handleCopyCredential(record.secretKey!)}
                />
              </div>
            </div>
          );
        }
        return '-';
      }
    },
    {
      title: '创建时间',
      dataIndex: 'createAt',
      key: 'createAt',
      render: (date: string) => date ? new Date(date).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      render: (record: Credential) => (
        <Popconfirm
          title={`确定要删除所有${record.type === 'API_KEY' ? 'API Key' : record.type === 'HMAC' ? 'AK/SK' : 'JWT'}凭证吗？`}
          onConfirm={() => handleDeleteCredential(record.type)}
        >
          <Button type="link" danger size="small" icon={<DeleteOutlined />}>
            删除
          </Button>
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
              columns={credentialColumns}
              dataSource={credentials.filter(c => c.type === 'API_KEY')}
              rowKey={(record) => record.id || record.apiKey || Math.random().toString()}
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
              columns={credentialColumns}
              dataSource={credentials.filter(c => c.type === 'HMAC')}
              rowKey={(record) => record.id || record.accessKey || Math.random().toString()}
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
    </>
  );
} 