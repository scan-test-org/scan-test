"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Link, Plus, Settings, Trash2 } from "lucide-react"

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

interface ApiProductLinkApiProps {
  apiProduct: ApiProduct
}

export function ApiProductLinkApi({ apiProduct }: ApiProductLinkApiProps) {
  const mockLinkedServices = [
    {
      id: "service-001",
      name: "Payment Gateway",
      url: "https://api.payment.com/v1",
      status: "active",
      type: "REST",
      version: "v1.0",
      lastSync: "2025-01-08T10:30:00Z"
    },
    {
      id: "service-002", 
      name: "User Management",
      url: "https://api.users.com/v2",
      status: "active",
      type: "GraphQL",
      version: "v2.1",
      lastSync: "2025-01-07T15:45:00Z"
    },
    {
      id: "service-003",
      name: "Notification Service", 
      url: "https://api.notify.com/v1",
      status: "inactive",
      type: "REST",
      version: "v1.5",
      lastSync: "2025-01-05T09:20:00Z"
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

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Link API</h1>
          <p className="text-muted-foreground mt-1">
            关联的网关服务
          </p>
        </div>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          添加服务
        </Button>
      </div>

      <div className="grid gap-4">
        {mockLinkedServices.map((service) => (
          <Card key={service.id}>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Link className="h-5 w-5 text-muted-foreground" />
                  <div>
                    <CardTitle className="text-lg">{service.name}</CardTitle>
                    <p className="text-sm text-muted-foreground">{service.url}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant={service.status === "active" ? "default" : "secondary"}>
                    {service.status === "active" ? "活跃" : "非活跃"}
                  </Badge>
                  <Badge variant="outline">{service.type}</Badge>
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
                  <span className="text-muted-foreground">版本:</span>
                  <span className="ml-2">{service.version}</span>
                </div>
                <div>
                  <span className="text-muted-foreground">最后同步:</span>
                  <span className="ml-2">{formatDate(service.lastSync)}</span>
                </div>
                <div>
                  <span className="text-muted-foreground">状态:</span>
                  <span className="ml-2">{service.status === "active" ? "正常" : "异常"}</span>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
} 