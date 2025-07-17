"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Globe, ExternalLink, Settings, Trash2, Plus } from "lucide-react"

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

interface ApiProductPortalProps {
  apiProduct: ApiProduct
}

export function ApiProductPortal({ apiProduct }: ApiProductPortalProps) {
  const mockPortals = [
    {
      id: "portal-001",
      name: "Company Portal",
      title: "企业门户",
      url: "https://company.portal.com",
      status: "published",
      publishedAt: "2025-01-05T10:00:00Z",
      visitors: 1250,
      apiCalls: 45600
    },
    {
      id: "portal-002",
      name: "Developer Portal",
      title: "开发者门户",
      url: "https://dev.portal.com",
      status: "draft",
      publishedAt: null,
      visitors: 0,
      apiCalls: 0
    },
    {
      id: "portal-003",
      name: "Public Portal",
      title: "公开门户",
      url: "https://public.portal.com",
      status: "published",
      publishedAt: "2025-01-03T14:30:00Z",
      visitors: 3200,
      apiCalls: 89000
    }
  ]

  const formatDate = (dateString: string | null) => {
    if (!dateString) return "未发布"
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
          <h1 className="text-2xl font-bold">Portal</h1>
          <p className="text-muted-foreground mt-1">
            发布的门户
          </p>
        </div>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          发布到门户
        </Button>
      </div>

      <div className="grid gap-4">
        {mockPortals.map((portal) => (
          <Card key={portal.id}>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <Globe className="h-5 w-5 text-muted-foreground" />
                  <div>
                    <CardTitle className="text-lg">{portal.title}</CardTitle>
                    <p className="text-sm text-muted-foreground">{portal.name}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant={portal.status === "published" ? "default" : "secondary"}>
                    {portal.status === "published" ? "已发布" : "草稿"}
                  </Badge>
                  <Button variant="ghost" size="icon">
                    <ExternalLink className="h-4 w-4" />
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
              <div className="grid grid-cols-4 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">门户地址:</span>
                  <div className="mt-1">
                    <a 
                      href={portal.url} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="text-blue-600 hover:underline"
                    >
                      {portal.url}
                    </a>
                  </div>
                </div>
                <div>
                  <span className="text-muted-foreground">发布时间:</span>
                  <div className="mt-1">{formatDate(portal.publishedAt)}</div>
                </div>
                <div>
                  <span className="text-muted-foreground">访问量:</span>
                  <div className="mt-1">{portal.visitors.toLocaleString()}</div>
                </div>
                <div>
                  <span className="text-muted-foreground">API调用:</span>
                  <div className="mt-1">{portal.apiCalls.toLocaleString()}</div>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  )
} 