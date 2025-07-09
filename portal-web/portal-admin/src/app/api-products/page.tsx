"use client"

import { useState } from "react"
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
    status: "active",
    requests: 0,
    errorRate: "0.00%",
    avgLatency: "0ms",
    createdAt: "Jul 8, 2025, 5:00 PM",
    labels: []
  }
]

export default function ApiProductsPage() {
  const [apiProducts, setApiProducts] = useState<ApiProduct[]>(mockApiProducts)
  const [showCreateDialog, setShowCreateDialog] = useState(false)

  const handleCreateApiProduct = (newProduct: Omit<ApiProduct, "id" | "status" | "requests" | "errorRate" | "avgLatency" | "createdAt">) => {
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
  }

  const getTypeIcon = (type: string) => {
    return type === "restApi" ? <Activity className="h-4 w-4" /> : <Clock className="h-4 w-4" />
  }

  const getTypeBadgeVariant = (type: string) => {
    return type === "restApi" ? "default" : "secondary"
  }

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

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {apiProducts.map((product) => (
          <Card key={product.id} className="relative">
            <CardHeader className="pb-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                    {getTypeIcon(product.type)}
                  </div>
                  <div>
                    <CardTitle className="text-lg">{product.name}</CardTitle>
                    <div className="flex items-center gap-2 mt-1">
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
                    <Button variant="ghost" size="icon">
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
        ))}
      </div>

      <CreateApiProductDialog
        open={showCreateDialog}
        onOpenChange={setShowCreateDialog}
        onSubmit={handleCreateApiProduct}
      />
    </div>
  )
} 