import { useState, useEffect } from 'react'
import { Button, Table, Modal, Steps, Form, Input, message } from 'antd'
import { PlusOutlined } from '@ant-design/icons'
import api from '@/lib/api.ts';

interface Gateway {
  gatewayId: string
  gatewayName: string
  gatewayType: 'APIG_API' | 'HIGRESS'
  createAt: string
}

export default function Consoles() {
  const [gateways, setGateways] = useState<Gateway[]>([])

  // 导入实例弹窗相关
  const [importVisible, setImportVisible] = useState(false);

  const [importStep, setImportStep] = useState(0);
  const [importForm] = Form.useForm();

  const [gatewayLoading, setGatewayLoading] = useState(false);
  const [gatewayList, setGatewayList] = useState<Gateway[]>([]);
  const [selectedGateway, setSelectedGateway] = useState<Gateway | null>(null);

  const [apiList, setApiList] = useState<any[]>([]);
  const [apiLoading, setApiLoading] = useState(false);
  const [selectedApi, setSelectedApi] = useState<any | null>(null);

  useEffect(() => {
    api.get('/gateways').then(res => {      
      setGateways(res.data?.content || [])
    })
  }, [])



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

  // 步骤切换
  const handleImportNext = async () => {
    if (importStep === 0) {
      // 校验表单并请求/gateways/apig
      try {
        const values = await importForm.validateFields();
        setGatewayLoading(true);
        api.post('/gateways/apig', { ...values, gatewayType: 'APIG_API' }).then(res => {
          setGatewayList(res.data?.content || []);
          setImportStep(1);
        }).finally(() => setGatewayLoading(false));
      } catch (err) {
        console.log('表单校验错误', err);
      }
    } else if (importStep === 1) {
      if (!selectedGateway) {
        message.warning('请选择一个网关实例');
        return;
      }
      // 根据网关类型请求API列表
      setApiLoading(true);
      const { gatewayId, gatewayType } = selectedGateway;
      if (gatewayType === 'APIG_API') {
        Promise.all([
          api.get(`/gateways/${gatewayId}/rest-apis`),
          api.get(`/gateways/${gatewayId}/mcp-servers`)
        ]).then(([restRes, mcpRes]) => {
          setApiList([...(restRes.data?.content || []), ...(mcpRes.data?.content || [])]);
          setImportStep(2);
        }).finally(() => setApiLoading(false));
      } else if (gatewayType === 'HIGRESS') {
        api.get(`/gateways/${gatewayId}/mcp-servers`).then(res => {
          setApiList(res.data?.content || []);
          setImportStep(2);
        }).finally(() => setApiLoading(false));
      }
    }
  };

  const handleImportPrev = () => {
    setImportStep(importStep - 1);
  };

  const handleImportOk = () => {
    if (!selectedApi) {
      message.warning('请选择一个API');
      return;
    }
    // 这里可以处理导入逻辑
    message.success('导入成功！');
    setImportVisible(false);
    setImportStep(0);
    setSelectedGateway(null);
    setSelectedApi(null);
    setGatewayList([]);
    setApiList([]);
    importForm.resetFields();
  };

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
        onCancel={() => {
          setImportVisible(false);
          setImportStep(0);
          setSelectedGateway(null);
          setSelectedApi(null);
          setGatewayList([]);
          setApiList([]);
          importForm.resetFields();
        }}
        footer={null}
        width={800}
      >
        <Steps current={importStep} style={{ marginBottom: 24 }}>
          <Steps.Step title="填写认证信息" />
          <Steps.Step title="选择网关实例" />
          <Steps.Step title="选择API" />
        </Steps>
        <div style={{ minHeight: 300 }}>
          {importStep === 0 && (
            <Form form={importForm} layout="vertical" preserve={false}>
              <Form.Item label="Region" name="region" rules={[{ required: true, message: '请输入region' }]}> <Input /> </Form.Item>
              <Form.Item label="Access Key" name="accessKey" rules={[{ required: true, message: '请输入accessKey' }]}> <Input /> </Form.Item>
              <Form.Item label="Secret Key" name="secretKey" rules={[{ required: true, message: '请输入secretKey' }]}> <Input.Password /> </Form.Item>
            </Form>
          )}
          {importStep === 1 && (
            <Table
              rowKey="gatewayId"
              loading={gatewayLoading}
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
                  setSelectedGateway(selectedRows[0]);
                },
              }}
              pagination={false}
            />
          )}
          {importStep === 2 && (
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
            />
          )}
        </div>
        <div style={{ marginTop: 24, textAlign: 'right' }}>
          {importStep > 0 && (
            <Button style={{ marginRight: 8 }} onClick={handleImportPrev}>
              上一步
            </Button>
          )}
          {importStep < 2 && (
            <Button type="primary" onClick={handleImportNext}>
              下一步
            </Button>
          )}
          {importStep === 2 && (
            <Button type="primary" onClick={handleImportOk}>
              完成导入
            </Button>
          )}
        </div>
      </Modal>
    </div>
  )
}
