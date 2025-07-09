"use client"

import { useState, useCallback, memo, useMemo } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Plus, MoreVertical, Activity, Clock, AlertCircle } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { CreateApiProductDialog } from "@/components/create-api-product-dialog"

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
    return type === "restApi" ? <Activity className="h-4 w-4" /> : <Clock className="h-4 w-4" />
  }

  const getTypeBadgeVariant = (type: string) => {
    return type === "restApi" ? "default" : "secondary"
  }

  const handleClick = useCallback(() => {
    onNavigate(product.id)
  }, [product.id, onNavigate])

  return (
    <Card 
      className="relative hover:shadow-lg transition-shadow cursor-pointer" 
      onClick={handleClick}
    >
      <CardHeader className="pb-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
              {getTypeIcon(product.type)}
            </div>
            <div>
              <CardTitle className="text-lg">{product.name}</CardTitle>
              <div className="flex items-center gap-2 mt-1 flex-wrap">
                <Badge variant="outline" className="text-xs">
                  {product.category}
                </Badge>
                <Badge variant={getTypeBadgeVariant(product.type)}>
                  {product.type === "restApi" ? "REST API" : "MCP Server"}
                </Badge>
                <Badge variant={product.status === "active" ? "default" : "secondary"}>
                  {product.status === "active" ? "活跃" : "非活跃"}
                </Badge>
              </div>
            </div>
          </div>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button 
                variant="ghost" 
                size="icon"
                onClick={(e) => e.stopPropagation()}
              >
                <MoreVertical className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem>编辑</DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem className="text-destructive">
                删除
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        {product.description && (
          <p className="text-sm text-muted-foreground">{product.description}</p>
        )}

        <div className="grid grid-cols-3 gap-4 text-center">
          <div>
            <div className="text-lg font-semibold">{product.requests}</div>
            <div className="text-xs text-muted-foreground">Requests</div>
          </div>
          <div>
            <div className="text-lg font-semibold">{product.errorRate}</div>
            <div className="text-xs text-muted-foreground">Error Rate</div>
          </div>
          <div>
            <div className="text-lg font-semibold">{product.avgLatency}</div>
            <div className="text-xs text-muted-foreground">Avg. Latency</div>
          </div>
        </div>

        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-muted-foreground">Created At</span>
            <span>{product.createdAt}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Labels</span>
            <span>{product.labels.length > 0 ? product.labels.join(", ") : "None"}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-muted-foreground">Custom Attributes</span>
            <span>None</span>
          </div>
        </div>
      </CardContent>
    </Card>
  )
})

ProductCard.displayName = 'ProductCard'

export default function ApiProductsPage() {
  const router = useRouter()
  const [apiProducts, setApiProducts] = useState<ApiProduct[]>(mockApiProducts)
  const [showCreateDialog, setShowCreateDialog] = useState(false)
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
    router.push(`/api-products/${productId}`)
  }, [router])

  const handleCreateApiProduct = useCallback((newProduct: Omit<ApiProduct, "id" | "status" | "requests" | "errorRate" | "avgLatency" | "createdAt">) => {
    const product: ApiProduct = {
      ...newProduct,
      id: Date.now().toString(),
      status: "active",
      requests: 0,
      errorRate: "0.00%",
      avgLatency: "0ms",
      createdAt: new Date().toLocaleDateString("en-US", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "numeric",
        minute: "2-digit"
      })
    }
    setApiProducts([...apiProducts, product])
    setShowCreateDialog(false)
  }, [apiProducts])

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">API Products</h1>
          <p className="text-muted-foreground mt-2">
            管理和配置您的 API 产品和服务
          </p>
        </div>
        <Button onClick={() => setShowCreateDialog(true)}>
          <Plus className="mr-2 h-4 w-4" />
          创建 API Product
        </Button>
      </div>

      {/* 过滤器 */}
      <div className="flex flex-wrap gap-4 items-center">
        <div className="flex items-center gap-2">
          <label className="text-sm font-medium">分类:</label>
          <div className="flex flex-wrap gap-1">
            {categories.map((category) => (
              <Button
                key={category}
                variant={selectedCategory === category ? "default" : "outline"}
                size="sm"
                onClick={() => handleCategoryChange(category)}
              >
                {category}
              </Button>
            ))}
          </div>
        </div>
        
        <div className="flex items-center gap-2">
          <label className="text-sm font-medium">类型:</label>
          <div className="flex flex-wrap gap-1">
            {types.map((type) => (
              <Button
                key={type}
                variant={selectedType === type ? "default" : "outline"}
                size="sm"
                onClick={() => handleTypeChange(type)}
              >
                {type}
              </Button>
            ))}
          </div>
        </div>
      </div>

      {/* 统计信息 */}
      <div className="text-sm text-muted-foreground">
        显示 {filteredProducts.length} 个产品，共 {apiProducts.length} 个
        {selectedCategory !== "All" && ` • 分类: ${selectedCategory}`}
        {selectedType !== "All" && ` • 类型: ${selectedType}`}
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

      {filteredProducts.length === 0 && (
        <div className="text-center py-12">
          <AlertCircle className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
          <h3 className="text-lg font-medium text-muted-foreground mb-2">没有找到匹配的API产品</h3>
          <p className="text-sm text-muted-foreground mb-4">
            尝试调整过滤条件或创建一个新的API产品
          </p>
          <Button onClick={() => setShowCreateDialog(true)}>
            <Plus className="mr-2 h-4 w-4" />
            创建 API Product
          </Button>
        </div>
      )}

      <CreateApiProductDialog
        open={showCreateDialog}
        onOpenChange={setShowCreateDialog}
        onSubmit={handleCreateApiProduct}
      />
    </div>
  )
} 