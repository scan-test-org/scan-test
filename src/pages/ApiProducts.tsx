import { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { MenuProps } from 'antd';
import { Badge, Button, Card, Col, Dropdown, Modal, Row, Select, Statistic, Form, Input, message } from 'antd';
import type { ApiProduct } from '@/types/api-product';
import { ApiOutlined, ClockCircleOutlined, MoreOutlined, PlusOutlined } from '@ant-design/icons';
import api from '@/lib/api.ts';
import { getStatusBadgeVariant } from '@/lib/utils';


// 优化的产品卡片组件
const ProductCard = memo(({ product, onNavigate, handleRefresh }: {
  product: ApiProduct;
  onNavigate: (productId: string) => void;
  handleRefresh: () => void;
}) => {
  const getTypeIcon = (type: string) => {
    return type === "REST_API" ? <ApiOutlined className="h-4 w-4" /> : <ClockCircleOutlined className="h-4 w-4" />
  }

  const getTypeBadgeVariant = (type: string) => {
    return type === "REST_API" ? "blue" : "purple"
  }


  const handleClick = useCallback((e: React.MouseEvent<HTMLDivElement>) => {
    onNavigate(product.productId)
  }, [product.productId, onNavigate]);



  const handleDelete = useCallback((productId: string, e?: React.MouseEvent | any) => {
    if (e && e.stopPropagation) e.stopPropagation();
    api.delete(`/products/${productId}`).then(res => {
      message.success('API Product 删除成功');
      handleRefresh();
    });
  }, [handleRefresh]);

  const dropdownItems: MenuProps['items'] = [
    {
      key: 'edit',
      label: '编辑',
    },
    {
      type: 'divider',
    },
    {
      key: 'delete',
      label: '删除',
      danger: true,
      onClick: (info: any) => handleDelete(product.productId, info?.domEvent),
    },
  ]

  return (
    <Card
      className="hover:shadow-lg transition-shadow cursor-pointer"
      onClick={handleClick}
      bodyStyle={{ padding: '16px' }}
    >
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100">
            {getTypeIcon(product.type)}
          </div>
          <div>
            <h3 className="text-lg font-semibold">{product.name}</h3>
            <div className="flex items-center gap-2 mt-1 flex-wrap">
              <Badge color="green" text={product.category} />
              <Badge color={getTypeBadgeVariant(product.type)} text={product.type === "REST_API" ? "REST API" : "MCP Server"} />
              <Badge color={getStatusBadgeVariant(product.status)} text={product.status === "PENDING" ? "待关联" : product.status === "READY" ? "已关联" : "已发布"} />
            </div>
          </div>
        </div>
        <Dropdown menu={{ items: dropdownItems }} trigger={['click']}>
          <Button
            type="text"
            icon={<MoreOutlined />}
            onClick={(e) => e.stopPropagation()}
          />
        </Dropdown>
      </div>

      <div className="space-y-4">
        {product.description && (
          <p className="text-sm text-gray-600">{product.description}</p>
        )}

        {/* <Row gutter={16} className="text-center">
          <Col span={8}>
            <Statistic title="Requests" value={product.requests} />
          </Col>
          <Col span={8}>
            <Statistic title="Error Rate" value={product.errorRate} />
          </Col>
          <Col span={8}>
            <Statistic title="Avg. Latency" value={product.avgLatency} />
          </Col>
        </Row> */}

        <div className="space-y-2 text-sm">

          <div className="flex justify-between">
            <span className="text-gray-500">Product ID</span>
            <span>{product.productId}</span>
          </div>

          <div className="flex justify-between">
            <span className="text-gray-500">Created At</span>
            <span>{product.createdAt}</span>
          </div>
          
          <div className="flex justify-between">
            <span className="text-gray-500">enableConsumerAuth</span>
            <span>{product.enableConsumerAuth }</span>
          </div>
        </div>
      </div>
    </Card>
  )
})

ProductCard.displayName = 'ProductCard'

export default function ApiProducts() {
  const navigate = useNavigate();
  const [apiProducts, setApiProducts] = useState<ApiProduct[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [selectedType, setSelectedType] = useState<string>('All');

  const [createVisible, setCreateVisible] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchApiProducts();
  }, []);

  const fetchApiProducts = useCallback(() => {
    api.get('/products').then(res => {
      setApiProducts(res.data.content);
    });
  }, []);
  

  // 优化的过滤器处理函数
  const handleCategoryChange = useCallback((category: string) => {
    setSelectedCategory(category);
  }, [])

  const handleTypeChange = useCallback((type: string) => {
    setSelectedType(type)
  }, [])

  // 使用useMemo优化数据计算
  const categories = useMemo(() =>
    ["All", ...Array.from(new Set(apiProducts?.map(product => product.category)))],
    [apiProducts]
  )

  const types = useMemo(() =>
    ["All", "REST API", "MCP Server"],
    []
  )

  // 过滤API Products
  const filteredProducts = useMemo(() =>
    apiProducts?.filter(product => {
      const categoryMatch = selectedCategory === "All" || product.category === selectedCategory
      const typeMatch = selectedType === "All" ||
        (selectedType === "REST API" && product.type === "REST_API") ||
        (selectedType === 'MCP Server' && product.type === 'MCP_SERVER')
      return categoryMatch && typeMatch;
    }),
    [apiProducts, selectedCategory, selectedType],
  )

  // 优化的导航处理函数
  const handleNavigateToProduct = useCallback((productId: string) => {
    navigate(`/api-products/detail?productId=${productId}`);
  }, [navigate]);

  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      api.post('/products', values).then(res => {
        form.resetFields();
        message.success('API Product 创建成功');
        fetchApiProducts();
      }).finally(() => {
        setCreateVisible(false);
      });
     
    } catch (e) {
      // 校验失败
    }
  };


  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">API Products</h1>
          <p className="text-gray-500 mt-2">
            管理和配置您的API产品
          </p>
        </div>
        <Button onClick={() => {
          setCreateVisible(true);
        }} type="primary" icon={<PlusOutlined/>}>
          创建 API Product
        </Button>
      </div>

      {/* 过滤器 */}
      <div className="flex gap-4">
        <Select
          value={selectedCategory}
          onChange={handleCategoryChange}
          style={{ width: 200 }}
          placeholder="选择分类"
        >
          {categories.map(category => (
            <Select.Option key={category} value={category}>
              {category}
            </Select.Option>
          ))}
        </Select>
        <Select
          value={selectedType}
          onChange={handleTypeChange}
          style={{ width: 200 }}
          placeholder="选择类型"
        >
          {types.map(type => (
            <Select.Option key={type} value={type}>
              {type}
            </Select.Option>
          ))}
        </Select>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {filteredProducts.map((product) => (
          <ProductCard
            key={product.productId}
            product={product}
            onNavigate={handleNavigateToProduct}
            handleRefresh={fetchApiProducts}
          />
        ))}
      </div>

      <Modal
        title="创建API Product"
        open={createVisible}
        onOk={handleCreate}
        onCancel={() => {
          setCreateVisible(false);
          form.resetFields();
        }}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          preserve={false}
        >
          <Form.Item
            label="名称"
            name="name"
            rules={[{ required: true, message: '请输入API Product名称' }]}
          >
            <Input placeholder="请输入API Product名称" />
          </Form.Item>
          <Form.Item
            label="描述"
            name="description"
            rules={[{ required: true, message: '请输入描述' }]}
          >
            <Input.TextArea placeholder="请输入描述" rows={3} />
          </Form.Item>
          <Form.Item
            label="类型"
            name="type"
            rules={[{ required: true, message: '请选择类型' }]}
          >
            <Select placeholder="请选择类型">
              <Select.Option value="REST_API">REST API</Select.Option>
              <Select.Option value="MCP_SERVER">MCP Server</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            label="分类"
            name="category"
            rules={[{ required: true, message: '请输入分类' }]}
          >
            <Input placeholder="请输入分类" />
          </Form.Item>
        </Form>
        
      </Modal>
    </div>
  )
}
