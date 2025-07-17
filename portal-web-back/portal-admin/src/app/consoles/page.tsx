"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Plus, MoreVertical, Settings } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { CreateConsoleDialog } from "@/components/create-console-dialog"

interface Gateway {
  id: string
  name: string
  description: string
  type: "kong" | "nginx" | "envoy" | "traefik"
  status: "running" | "stopped" | "error"
  endpoint: string
  version: string
  lastUpdated: string
  environment: "production" | "staging" | "development"
  services: number
  plugins: number
}

const mockGateways: Gateway[] = [
  {
    id: "1",
    name: "Production Gateway",
    description: "生产环境网关实例",
    type: "kong",
    status: "running",
    endpoint: "https://api.example.com",
    version: "3.5.0",
    lastUpdated: "2025-01-08T10:30:00Z",
    environment: "production",
    services: 12,
    plugins: 8
  },
  {
    id: "2",
    name: "Staging Gateway",
    description: "测试环境网关实例",
    type: "kong",
    status: "running",
    endpoint: "https://staging-api.example.com",
    version: "3.5.0",
    lastUpdated: "2025-01-07T15:45:00Z",
    environment: "staging",
    services: 8,
    plugins: 5
  },
  {
    id: "3",
    name: "Dev Gateway",
    description: "开发环境网关实例",
    type: "kong",
    status: "stopped",
    endpoint: "https://dev-api.example.com",
    version: "3.4.0",
    lastUpdated: "2025-01-06T09:20:00Z",
    environment: "development",
    services: 3,
    plugins: 2
  }
]

export default function ConsolesPage() {
  const [gateways, setGateways] = useState<Gateway[]>(mockGateways)
  const [showCreateDialog, setShowCreateDialog] = useState(false)

  const handleCreateGateway = (newGateway: Omit<Gateway, "id" | "lastUpdated">) => {
    const gateway: Gateway = {
      ...newGateway,
      id: Date.now().toString(),
      lastUpdated: new Date().toISOString()
    }
    setGateways([...gateways, gateway])
    setShowCreateDialog(false)
  }

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case "running":
        return "default"
      case "stopped":
        return "secondary"
      case "error":
        return "destructive"
      default:
        return "secondary"
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case "running":
        return "运行中"
      case "stopped":
        return "已停止"
      case "error":
        return "错误"
      default:
        return status
    }
  }

  const getEnvironmentBadgeVariant = (env: string) => {
    switch (env) {
      case "production":
        return "default"
      case "staging":
        return "secondary"
      case "development":
        return "outline"
      default:
        return "outline"
    }
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
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">网关实例</h1>
          <p className="text-muted-foreground mt-2">
            管理和配置您的网关实例
          </p>
        </div>
        <Button onClick={() => setShowCreateDialog(true)}>
          <Plus className="mr-2 h-4 w-4" />
          导入网关实例
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>名称</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>环境</TableHead>
              <TableHead>服务数</TableHead>
              <TableHead>最后更新</TableHead>
              <TableHead className="w-[50px]"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {gateways.map((gateway) => (
              <TableRow key={gateway.id}>
                <TableCell>
                  <div>
                    <div className="font-medium">{gateway.name}</div>
                    <div className="text-sm text-muted-foreground">
                      {gateway.description}
                    </div>
                    <div className="text-xs text-muted-foreground mt-1">
                      {gateway.endpoint}
                    </div>
                  </div>
                </TableCell>
                <TableCell>
                  <Badge variant={getStatusBadgeVariant(gateway.status)}>
                    {getStatusText(gateway.status)}
                  </Badge>
                </TableCell>
                <TableCell>
                  <Badge variant={getEnvironmentBadgeVariant(gateway.environment)}>
                    {gateway.environment}
                  </Badge>
                </TableCell>
                <TableCell>
                  <div className="text-sm">
                    <div>{gateway.services} 个服务</div>
                    <div className="text-muted-foreground">{gateway.plugins} 个插件</div>
                  </div>
                </TableCell>
                <TableCell>
                  {formatDate(gateway.lastUpdated)}
                </TableCell>
                <TableCell>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon">
                        <MoreVertical className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem>编辑</DropdownMenuItem>
                      <DropdownMenuItem>查看详情</DropdownMenuItem>
                      <DropdownMenuItem>复制配置</DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem className="text-destructive">
                        删除
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      <CreateConsoleDialog
        open={showCreateDialog}
        onOpenChange={setShowCreateDialog}
      />
    </div>
  )
} 