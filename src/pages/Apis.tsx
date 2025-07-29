import { useEffect, useState } from "react";
import { Card, Table, Tag, Button, Space, Typography, Input, Select } from "antd";
import { SearchOutlined, EyeOutlined, FileTextOutlined } from "@ant-design/icons";
import { Link } from "react-router-dom";
import { Layout } from "../components/Layout";
import api from "../lib/api";
import { ProductType, ProductStatus, ProductCategory } from "../types";
import type { Product, ApiResponse, PaginatedResponse } from "../types";

const { Title, Paragraph } = Typography;
const { Search } = Input;

interface ApiItem {
  key: string;
  name: string;
  description: string;
  category: string;
  status: string;
  version: string;
  subscribers: number;
}

function APIsPage() {
  const [loading, setLoading] = useState(false);
  const [apis, setApis] = useState<ApiItem[]>([]);
  const [searchText, setSearchText] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('');

  useEffect(() => {
    fetchApis();
  }, []);

  const fetchApis = async () => {
    setLoading(true);
    try {
      const response: ApiResponse<PaginatedResponse<Product>> = await api.get("/products?type=REST_API&page=0&size=100");
      if (response.code === "SUCCESS" && response.data) {
        const mapped = response.data.content
          .filter((item: Product) => item.type === ProductType.REST_API)
          .map((item: Product) => ({
            key: item.productId,
            name: item.name,
            description: item.description,
            category: item.category,
            status: item.status === ProductStatus.ENABLE ? 'active' : 'inactive',
            version: 'v1.0.0', // 从apiSpec中解析版本信息
            subscribers: 0, // 暂时设为0，后续可以从其他接口获取
          }));
        setApis(mapped);
      }
    } catch (error) {
      console.error('获取API列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'active':
        return '活跃'
      case 'inactive':
        return '非活跃'
      default:
        return status
    }
  };

  const getCategoryText = (category: string) => {
    switch (category) {
      case ProductCategory.OFFICIAL:
        return '官方'
      case ProductCategory.COMMUNITY:
        return '社区'
      case ProductCategory.CUSTOM:
        return '自定义'
      default:
        return category
    }
  };

  const filteredApis = apis.filter(api => {
    const matchesSearch = api.name.toLowerCase().includes(searchText.toLowerCase()) ||
                         api.description.toLowerCase().includes(searchText.toLowerCase());
    const matchesCategory = !selectedCategory || api.category === selectedCategory;
    const matchesStatus = !selectedStatus || api.status === selectedStatus;
    return matchesSearch && matchesCategory && matchesStatus;
  });

  const columns = [
    {
      title: 'API名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: ApiItem) => (
        <div>
          <div className="font-medium">{name}</div>
          <div className="text-sm text-gray-500">{record.description}</div>
        </div>
      ),
    },
    {
      title: '分类',
      dataIndex: 'category',
      key: 'category',
      render: (category: string) => <Tag color="blue">{getCategoryText(category)}</Tag>
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Tag color={status === 'active' ? 'green' : 'orange'}>
          {getStatusText(status)}
        </Tag>
      )
    },
    {
      title: '版本',
      dataIndex: 'version',
      key: 'version',
      render: (version: string) => <Tag color="purple">{version}</Tag>
    },
    {
      title: '订阅者',
      dataIndex: 'subscribers',
      key: 'subscribers',
      render: (subscribers: number) => subscribers.toLocaleString()
    },
    {
      title: '操作',
      key: 'action',
      render: (_: unknown, record: ApiItem) => (
        <Space>
          <Link to={`/apis/${record.key}`}>
            <Button type="link" icon={<EyeOutlined />}>
              查看
            </Button>
          </Link>
          <Button type="link" icon={<FileTextOutlined />}>
            文档
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Layout>
      <div className="mb-8">
        <Title level={1} className="mb-2">
          API 列表
        </Title>
        <Paragraph className="text-gray-600">
          探索我们提供的各种API服务
        </Paragraph>
      </div>

      <Card>
        <div className="mb-4 flex gap-4">
          <Search
            placeholder="搜索API..."
            prefix={<SearchOutlined />}
            style={{ width: 300 }}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
          />
          <Select
            placeholder="选择分类"
            style={{ width: 150 }}
            allowClear
            value={selectedCategory}
            onChange={setSelectedCategory}
          >
            <Select.Option value={ProductCategory.OFFICIAL}>官方</Select.Option>
            <Select.Option value={ProductCategory.COMMUNITY}>社区</Select.Option>
            <Select.Option value={ProductCategory.CUSTOM}>自定义</Select.Option>
          </Select>
          <Select
            placeholder="选择状态"
            style={{ width: 150 }}
            allowClear
            value={selectedStatus}
            onChange={setSelectedStatus}
          >
            <Select.Option value="active">活跃</Select.Option>
            <Select.Option value="inactive">非活跃</Select.Option>
          </Select>
        </div>
        
        <Table 
          columns={columns} 
          dataSource={filteredApis}
          loading={loading}
          rowKey="key"
          pagination={{
            total: filteredApis.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
          }}
        />
      </Card>
    </Layout>
  );
}

export default APIsPage; 