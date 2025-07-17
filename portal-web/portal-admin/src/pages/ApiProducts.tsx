import { useState, useCallback, memo, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, Badge, Dropdown, Space, Select, Row, Col, Statistic } from 'antd'
import { PlusOutlined, MoreOutlined, ApiOutlined, ClockCircleOutlined } from '@ant-design/icons'
import type { MenuProps } from 'antd'

interface ApiProduct {
  id: string
  name: string
  description: string
  type: "restApi" | "mcpServer"
  category: string
  status: "active" | "inactive"
  requests: number
  errorRate: string
  avgLatency: string
  createdAt: string
  labels: string[]
}

const mockApiProducts: ApiProduct[] = [
  {
    id: "1",
    name: "test",
    description: "测试API产品",
    type: "restApi",
    category: "Testing",
    status: "active",
    requests: 0,
    errorRate: "0.00%",
    avgLatency: "0ms",
    createdAt: "Jul 8, 2025, 5:00 PM",
    labels: []
  },
  {
    id: "2",
    name: "payments",
    description: "支付处理API",
    type: "restApi",
    category: "Finance",
    status: "active",
    requests: 15420,
    errorRate: "0.12%",
    avgLatency: "245ms",
    createdAt: "Jul 5, 2025, 2:30 PM",
    labels: ["production", "payment"]
  },
  {
    id: "3",
    name: "users",
    description: "用户管理和认证API",
    type: "restApi",
    category: "Authentication",
    status: "active",
    requests: 8765,
    errorRate: "0.08%",
    avgLatency: "180ms",
    createdAt: "Jul 3, 2025, 11:15 AM",
    labels: ["auth", "users"]
  },
  {
    id: "4",
    name: "notifications",
    description: "消息通知服务",
    type: "mcpServer",
    category: "Communication",
    status: "active",
    requests: 3421,
    errorRate: "0.05%",
    avgLatency: "120ms",
    createdAt: "Jul 1, 2025, 9:45 AM",
    labels: ["notification", "mcp"]
  },
  {
    id: "5",
    name: "analytics",
    description: "数据分析和统计API",
    type: "restApi",
    category: "Analytics",
    status: "inactive",
    requests: 0,
    errorRate: "0.00%",
    avgLatency: "0ms",
    createdAt: "Jun 28, 2025, 4:20 PM",
    labels: ["analytics", "draft"]
  }
]

// 优化的产品卡片组件
const ProductCard = memo(({ product, onNavigate }: { 
  product: ApiProduct; 
  onNavigate: (id: string) => void;
}) => {
  const getTypeIcon = (type: string) => {
    return type === "restApi" ? <ApiOutlined className="h-4 w-4" /> : <ClockCircleOutlined className="h-4 w-4" />
  }

  const getTypeBadgeVariant = (type: string) => {
    return type === "restApi" ? "blue" : "purple"
  }

  const handleClick = useCallback(() => {
    onNavigate(product.id)
  }, [product.id, onNavigate])

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
              <Badge color="default" text={product.category} />
              <Badge color={getTypeBadgeVariant(product.type)} text={product.type === "restApi" ? "REST API" : "MCP Server"} />
              <Badge color={product.status === "active" ? "green" : "orange"} text={product.status === "active" ? "活跃" : "非活跃"} />
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

        <Row gutter={16} className="text-center">
          <Col span={8}>
            <Statistic title="Requests" value={product.requests} />
          </Col>
          <Col span={8}>
            <Statistic title="Error Rate" value={product.errorRate} />
          </Col>
          <Col span={8}>
            <Statistic title="Avg. Latency" value={product.avgLatency} />
          </Col>
        </Row>

        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-500">Created At</span>
            <span>{product.createdAt}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">Labels</span>
            <span>{product.labels.length > 0 ? product.labels.join(", ") : "None"}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500">Custom Attributes</span>
            <span>None</span>
          </div>
        </div>
      </div>
    </Card>
  )
})

ProductCard.displayName = 'ProductCard'

export default function ApiProducts() {
  const navigate = useNavigate()
  const [apiProducts, setApiProducts] = useState<ApiProduct[]>(mockApiProducts)
  const [selectedCategory, setSelectedCategory] = useState<string>("All")
  const [selectedType, setSelectedType] = useState<string>("All")
  
  // 优化的过滤器处理函数
  const handleCategoryChange = useCallback((category: string) => {
    setSelectedCategory(category)
  }, [])

  const handleTypeChange = useCallback((type: string) => {
    setSelectedType(type)
  }, [])

  // 使用useMemo优化数据计算
  const categories = useMemo(() => 
    ["All", ...Array.from(new Set(apiProducts.map(product => product.category)))],
    [apiProducts]
  )
  
  const types = useMemo(() => 
    ["All", "REST API", "MCP Server"],
    []
  )
  
  // 过滤API Products
  const filteredProducts = useMemo(() => 
    apiProducts.filter(product => {
      const categoryMatch = selectedCategory === "All" || product.category === selectedCategory
      const typeMatch = selectedType === "All" || 
        (selectedType === "REST API" && product.type === "restApi") ||
        (selectedType === "MCP Server" && product.type === "mcpServer")
      return categoryMatch && typeMatch
    }),
    [apiProducts, selectedCategory, selectedType]
  )

  // 优化的导航处理函数
  const handleNavigateToProduct = useCallback((productId: string) => {
    navigate(`/api-products/detail?id=${productId}`)
  }, [navigate])

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">API Products</h1>
          <p className="text-gray-500 mt-2">
            管理和配置您的API产品
          </p>
        </div>
        <Button type="primary" icon={<PlusOutlined />}>
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
            key={product.id}
            product={product}
            onNavigate={handleNavigateToProduct}
          />
        ))}
      </div>
    </div>
  )
} 