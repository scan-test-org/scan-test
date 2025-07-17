import { useParams } from "react-router-dom";
import { Card, Badge, Table, Typography, Space, Tag } from "antd";
import { Layout } from "../components/Layout";

const { Title, Paragraph } = Typography;

function ApiDetailPage() {
  const { id } = useParams();
  
  // 模拟API详情数据
  const apiData = {
    name: `API ${id}`,
    description: "这是一个示例API的描述信息",
    version: "v1.0.0",
    status: "active",
    category: "Payment",
    endpoints: [
      {
        key: "1",
        method: "GET",
        path: "/api/payments",
        description: "获取支付列表"
      },
      {
        key: "2", 
        method: "POST",
        path: "/api/payments",
        description: "创建新支付"
      }
    ]
  };

  const columns = [
    {
      title: '方法',
      dataIndex: 'method',
      key: 'method',
      render: (method: string) => (
        <Tag color={method === 'GET' ? 'green' : method === 'POST' ? 'blue' : 'orange'}>
          {method}
        </Tag>
      )
    },
    {
      title: '路径',
      dataIndex: 'path',
      key: 'path',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    }
  ];

  return (
    <Layout>
      <div className="mb-8">
        <Title level={1} className="mb-2">
          {apiData.name}
        </Title>
        <Space className="mb-4">
          <Badge status="success" text="活跃" />
          <Tag color="blue">{apiData.version}</Tag>
          <Tag color="purple">{apiData.category}</Tag>
        </Space>
        <Paragraph className="text-gray-600">
          {apiData.description}
        </Paragraph>
      </div>

      <Card title="API 端点" className="mb-8">
        <Table 
          columns={columns} 
          dataSource={apiData.endpoints}
          rowKey="key"
          pagination={false}
        />
      </Card>

      <Card title="API 绑定关系">
        <div className="p-4">
          <Paragraph className="text-gray-600">
            这里展示API的绑定关系、消费者等信息
          </Paragraph>
        </div>
      </Card>
    </Layout>
  );
}

export default ApiDetailPage; 