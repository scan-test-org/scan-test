"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { 
  Globe,
  Link, 
  Shield, 
  Copy
} from "lucide-react"

interface ApiProduct {
  id: string
  name: string
  description: string
  version: string
  status: string
  visibility: string
  createdAt: string
  updatedAt: string
  portals: number
  linkedServices: number
  policies: number
}

const mockApiProduct: ApiProduct = {
  id: "api-001",
  name: "Payment API",
  description: "支付处理API，提供完整的支付解决方案",
  version: "v1.2.0",
  status: "published",
  visibility: "public",
  createdAt: "2025-01-01T10:00:00Z",
  updatedAt: "2025-01-08T15:30:00Z",
  portals: 3,
  linkedServices: 5,
  policies: 8
}

export default function ApiProductOverviewPage() {
  const [copied, setCopied] = useState(false)
  const apiProduct = mockApiProduct // 实际项目中从API获取
  
  const handleCopyId = async () => {
    await navigator.clipboard.writeText(apiProduct.id)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }
  
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("zh-CN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit"
    })
  }

  return (
    <div className="space-y-6 p-6">
      <div>
          <h1 className="text-2xl font-bold">Overview</h1>
          <p className="text-muted-foreground mt-1">
            概览
          </p>
        </div>
      {/* 统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">发布门户</CardTitle>
            <Globe className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{apiProduct.portals}</div>
            <p className="text-xs text-muted-foreground">个门户发布</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">关联服务</CardTitle>
            <Link className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{apiProduct.linkedServices}</div>
            <p className="text-xs text-muted-foreground">个网关服务</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">活跃策略</CardTitle>
            <Shield className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{apiProduct.policies}</div>
            <p className="text-xs text-muted-foreground">个策略规则</p>
          </CardContent>
        </Card>
      </div>

      {/* 基本信息 */}
      <Card>
        <CardHeader>
          <CardTitle>基本信息</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">产品ID</label>
              <div className="flex items-center gap-2 mt-1">
                <code className="bg-muted px-2 py-1 rounded text-sm">{apiProduct.id}</code>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleCopyId}
                  className="h-6 w-6 p-0"
                >
                  <Copy className="h-3 w-3" />
                </Button>
                {copied && <span className="text-xs text-green-600">已复制</span>}
              </div>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">版本</label>
              <div className="mt-1">
                <Badge variant="outline">{apiProduct.version}</Badge>
              </div>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">状态</label>
              <div className="mt-1">
                <Badge variant={apiProduct.status === "published" ? "default" : "secondary"}>
                  {apiProduct.status === "published" ? "已发布" : "草稿"}
                </Badge>
              </div>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">可见性</label>
              <div className="mt-1">
                <Badge variant={apiProduct.visibility === "public" ? "outline" : "secondary"}>
                  {apiProduct.visibility === "public" ? "公开" : "私有"}
                </Badge>
              </div>
            </div>
          </div>
          
          <div>
            <label className="text-sm font-medium text-muted-foreground">描述</label>
            <p className="mt-1 text-sm">{apiProduct.description}</p>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm font-medium text-muted-foreground">创建时间</label>
              <p className="mt-1 text-sm">{formatDate(apiProduct.createdAt)}</p>
            </div>
            <div>
              <label className="text-sm font-medium text-muted-foreground">最后更新</label>
              <p className="mt-1 text-sm">{formatDate(apiProduct.updatedAt)}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* 分析数据 */}
      <Card>
        <CardHeader>
          <CardTitle>分析数据</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-3 gap-6">
            <div className="text-center">
              <div className="text-2xl font-bold text-blue-600">12,456</div>
              <div className="text-sm text-muted-foreground">今日请求数</div>
              <div className="text-xs text-green-600 mt-1">+15% ↗</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-orange-600">1.2%</div>
              <div className="text-sm text-muted-foreground">错误率</div>
              <div className="text-xs text-red-600 mt-1">+0.3% ↗</div>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">245ms</div>
              <div className="text-sm text-muted-foreground">平均延迟</div>
              <div className="text-xs text-green-600 mt-1">-12ms ↘</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
} 