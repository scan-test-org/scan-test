"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Shield, Plus, Settings, Trash2, Play, Pause } from "lucide-react"

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

interface ApiProductPolicyProps {
  apiProduct: ApiProduct
}

export function ApiProductPolicy({ apiProduct }: ApiProductPolicyProps) {
  const mockPolicies = [
    {
      id: "policy-001",
      name: "Rate Limiting",
      description: "限制API调用频率，防止滥用",
      type: "rate_limit",
      status: "active",
      config: {
        requests: 1000,
        window: "1h"
      },
      createdAt: "2025-01-01T10:00:00Z",
      updatedAt: "2025-01-08T15:30:00Z"
    },
    {
      id: "policy-002",
      name: "Authentication",
      description: "API密钥认证策略",
      type: "auth",
      status: "active",
      config: {
        authType: "key_auth",
        keyLocation: "header"
      },
      createdAt: "2025-01-01T10:00:00Z",
      updatedAt: "2025-01-05T12:20:00Z"
    },
    {
      id: "policy-003",
      name: "CORS",
      description: "跨域资源共享策略",
      type: "cors",
      status: "inactive",
      config: {
        origins: ["*"],
        methods: ["GET", "POST", "PUT", "DELETE"]
      },
      createdAt: "2025-01-02T14:00:00Z",
      updatedAt: "2025-01-06T09:15:00Z"
    },
    {
      id: "policy-004",
      name: "Logging",
      description: "API调用日志记录",
      type: "logging",
      status: "active",
      config: {
        logLevel: "info",
        retention: "30d"
      },
      createdAt: "2025-01-03T11:30:00Z",
      updatedAt: "2025-01-07T16:45:00Z"
    }
  ]

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("zh-CN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit"
    })
  }

  const getPolicyTypeLabel = (type: string) => {
    const typeMap: Record<string, string> = {
      rate_limit: "限流",
      auth: "认证",
      cors: "CORS",
      logging: "日志",
      transform: "转换",
      cache: "缓存"
    }
    return typeMap[type] || type
  }

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Policy</h1>
          <p className="text-muted-foreground mt-1">
            策略管理
          </p>
        </div>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          添加策略
        </Button>
      </div>

      <div className="grid gap-4">
        {mockPolicies.map((policy) => (
          <Card key={policy.id}>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Shield className="h-5 w-5 text-muted-foreground" />
                  <div>
                    <CardTitle className="text-lg">{policy.name}</CardTitle>
                    <p className="text-sm text-muted-foreground">{policy.description}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant="outline">{getPolicyTypeLabel(policy.type)}</Badge>
                  <Badge variant={policy.status === "active" ? "default" : "secondary"}>
                    {policy.status === "active" ? "活跃" : "非活跃"}
                  </Badge>
                  <Button variant="ghost" size="icon">
                    {policy.status === "active" ? (
                      <Pause className="h-4 w-4" />
                    ) : (
                      <Play className="h-4 w-4" />
                    )}
                  </Button>
                  <Button variant="ghost" size="icon">
                    <Settings className="h-4 w-4" />
                  </Button>
                  <Button variant="ghost" size="icon">
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-3 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">策略类型:</span>
                  <div className="mt-1">{getPolicyTypeLabel(policy.type)}</div>
                </div>
                <div>
                  <span className="text-muted-foreground">创建时间:</span>
                  <div className="mt-1">{formatDate(policy.createdAt)}</div>
                </div>
                <div>
                  <span className="text-muted-foreground">最后更新:</span>
                  <div className="mt-1">{formatDate(policy.updatedAt)}</div>
                </div>
              </div>
              
              {policy.config && (
                <div className="mt-4 p-3 bg-muted rounded-md">
                  <div className="text-sm font-medium mb-2">配置详情:</div>
                  <pre className="text-xs text-muted-foreground">
                    {JSON.stringify(policy.config, null, 2)}
                  </pre>
                </div>
              )}
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
} 