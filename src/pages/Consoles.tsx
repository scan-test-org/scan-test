import { useState, useEffect } from 'react'
import { Button, Table, Modal, Form, Input, message, Select, Divider } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import { gatewayApi } from '@/lib/api';

interface Gateway {
  gatewayId: string
  gatewayName: string
  gatewayType: 'APIG_API' | 'HIGRESS' | 'APIG_AI'
  createAt: string
}

export default function Consoles() {
  const [gateways, setGateways] = useState<Gateway[]>([])

  // 导入实例弹窗相关
  const [importVisible, setImportVisible] = useState(false);
  const [importForm] = Form.useForm();

  const [gatewayLoading, setGatewayLoading] = useState(false);
  const [gatewayList, setGatewayList] = useState<Gateway[]>([]);
  const [selectedGateway, setSelectedGateway] = useState<Gateway | null>(null);

  const [apiList, setApiList] = useState<any[]>([]);
  const [apiLoading, setApiLoading] = useState(false);
  const [selectedApi, setSelectedApi] = useState<any | null>(null);

  useEffect(() => {
    gatewayApi.getGateways().then((res: any) => {      
      setGateways(res.data?.content || [])
    })
  }, [])

  // 获取网关列表
  const fetchGateways = async (values: any) => {
    setGatewayLoading(true);
    try {
      const res = await gatewayApi.createApigGateway(values);
      setGatewayList(res.data?.content || []);
    } catch (error) {
      message.error('获取网关列表失败');
    } finally {
      setGatewayLoading(false);
    }
  };

  // 获取API列表
  const fetchApis = async (gateway: Gateway) => {
    setApiLoading(true);
    try {
      const { gatewayId, gatewayType } = gateway;
      if (gatewayType === 'APIG_API') {
        const [restRes, mcpRes] = await Promise.all([
          gatewayApi.getGatewayRestApis(gatewayId),
          gatewayApi.getGatewayMcpServers(gatewayId)
        ]);
        setApiList([...(restRes.data?.content || []), ...(mcpRes.data?.content || [])]);
      } else if (gatewayType === 'HIGRESS') {
        const res = await gatewayApi.getGatewayMcpServers(gatewayId);
        setApiList(res.data?.content || []);
      }
    } catch (error) {
      message.error('获取API列表失败');
    } finally {
      setApiLoading(false);
    }
  };

  // 处理网关选择
  const handleGatewaySelect = (gateway: Gateway) => {
    setSelectedGateway(gateway);
    setSelectedApi(null);
    fetchApis(gateway);
  };

  // 处理导入
  const handleImport = () => {
    if (!selectedApi) {
      message.warning('请选择一个API');
      return;
    }
    // 这里可以处理导入逻辑
    message.success('导入成功！');
    setImportVisible(false);
    setSelectedGateway(null);
    setSelectedApi(null);
    setGatewayList([]);
    setApiList([]);
    importForm.resetFields();
  };

  // 重置弹窗状态
  const handleCancel = () => {
    setImportVisible(false);
    setSelectedGateway(null);
    setSelectedApi(null);
    setGatewayList([]);
    setApiList([]);
    importForm.resetFields();
  };

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
        <Button danger>删除</Button>
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
          pagination={{
            position: ['bottomRight'],
          }}
        />
      </div>

      <Modal
        title="导入网关实例"
        open={importVisible}
        onCancel={handleCancel}
        footer={null}
        width={800}
      >
        <Form form={importForm} layout="vertical" preserve={false}>
          <div className="mb-4">
            <h3 className="text-lg font-medium mb-3">认证信息</h3>
            <Form.Item label="Region" name="region" rules={[{ required: true, message: '请输入region' }]}>
              <Input />
            </Form.Item>
            <Form.Item label="Access Key" name="accessKey" rules={[{ required: true, message: '请输入accessKey' }]}>
              <Input />
            </Form.Item>
            <Form.Item label="Secret Key" name="secretKey" rules={[{ required: true, message: '请输入secretKey' }]}>
              <Input.Password />
            </Form.Item>
            <Button 
              type="primary" 
              onClick={() => importForm.validateFields().then(fetchGateways)}
              loading={gatewayLoading}
            >
              获取网关列表
            </Button>
          </div>

          {gatewayList.length > 0 && (
            <div className="mb-4">
              <Divider />
              <h3 className="text-lg font-medium mb-3">选择网关实例</h3>
              <Table
                rowKey="gatewayId"
                columns={[
                  { title: 'ID', dataIndex: 'gatewayId' },
                  { title: '类型', dataIndex: 'gatewayType' },
                  { title: '名称', dataIndex: 'gatewayName' },
                ]}
                dataSource={gatewayList}
                rowSelection={{
                  type: 'radio',
                  selectedRowKeys: selectedGateway ? [selectedGateway.gatewayId] : [],
                  onChange: (selectedRowKeys, selectedRows) => {
                    handleGatewaySelect(selectedRows[0]);
                  },
                }}
                pagination={false}
                size="small"
              />
            </div>
          )}

          {selectedGateway && apiList.length > 0 && (
            <div className="mb-4">
              <Divider />
              <h3 className="text-lg font-medium mb-3">选择API</h3>
              <Table
                rowKey={record => record.apiId || record.id}
                loading={apiLoading}
                columns={[
                  { title: 'API ID', dataIndex: 'apiId' },
                  { title: '名称', dataIndex: 'name' },
                  { title: '类型', dataIndex: 'type' },
                ]}
                dataSource={apiList}
                rowSelection={{
                  type: 'radio',
                  selectedRowKeys: selectedApi ? [selectedApi.apiId || selectedApi.id] : [],
                  onChange: (selectedRowKeys, selectedRows) => {
                    setSelectedApi(selectedRows[0]);
                  },
                }}
                pagination={false}
                size="small"
              />
            </div>
          )}

          {selectedApi && (
            <div className="text-right">
              <Button type="primary" onClick={handleImport}>
                完成导入
              </Button>
            </div>
          )}
        </Form>
      </Modal>
    </div>
  )
}
