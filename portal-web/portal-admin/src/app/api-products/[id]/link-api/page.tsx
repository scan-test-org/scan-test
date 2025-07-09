"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Plus, MoreVertical, Link, Settings, ExternalLink } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

interface LinkedService {
  id: string
  name: string
  gateway: string
  protocol: string
  host: string
  port: number
  path: string
  status: "active" | "inactive" | "error"
  methods: string[]
  lastUpdated: string
}

const mockLinkedServices: LinkedService[] = [
  {
    id: "svc-001",
    name: "Payment Processing Service",
    gateway: "Production Gateway",
    protocol: "HTTP",
    host: "payment-api.internal",
    port: 8080,
    path: "/api/v1/payments",
    status: "active",
    methods: ["GET", "POST", "PUT", "DELETE"],
    lastUpdated: "2025-01-08T10:30:00Z"
  },
  {
    id: "svc-002", 
    name: "User Authentication Service",
    gateway: "Production Gateway",
    protocol: "HTTPS",
    host: "auth-api.internal",
    port: 8443,
    path: "/api/v1/auth",
    status: "active",
    methods: ["POST", "PUT"],
    lastUpdated: "2025-01-07T15:45:00Z"
  },
  {
    id: "svc-003",
    name: "Notification Service",
    gateway: "Staging Gateway",
    protocol: "HTTP",
    host: "notification-api.internal",
    port: 8080,
    path: "/api/v1/notifications",
    status: "inactive",
    methods: ["POST"],
    lastUpdated: "2025-01-06T09:20:00Z"
  }
]

export default function LinkApiPage() {
  const [linkedServices, setLinkedServices] = useState<LinkedService[]>(mockLinkedServices)
  const [showLinkDialog, setShowLinkDialog] = useState(false)
  const [newService, setNewService] = useState({
    name: "",
    gateway: "",
    protocol: "HTTP",
    host: "",
    port: 80,
    path: ""
  })

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case "active":
        return "default"
      case "inactive":
        return "secondary"
      case "error":
        return "destructive"
      default:
        return "secondary"
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case "active":
        return "活跃"
      case "inactive":
        return "非活跃"
      case "error":
        return "错误"
      default:
        return status
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

  const handleLinkService = () => {
    const service: LinkedService = {
      ...newService,
      id: `svc-${Date.now()}`,
      status: "active" as const,
      methods: ["GET", "POST"],
      lastUpdated: new Date().toISOString()
    }
    setLinkedServices([...linkedServices, service])
    setShowLinkDialog(false)
    setNewService({
      name: "",
      gateway: "",
      protocol: "HTTP",
      host: "",
      port: 80,
      path: ""
    })
  }

  return (
    <div className="space-y-6 p-6">
      {/* 页面标题和操作 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">关联的网关服务</h1>
          <p className="text-muted-foreground mt-1">
            管理 API Product 关联的网关服务配置
          </p>
        </div>
        <Dialog open={showLinkDialog} onOpenChange={setShowLinkDialog}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              关联服务
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[425px]">
            <DialogHeader>
              <DialogTitle>关联网关服务</DialogTitle>
              <DialogDescription>
                添加一个新的网关服务到当前 API Product
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="name" className="text-right">
                  服务名称
                </Label>
                <Input
                  id="name"
                  value={newService.name}
                  onChange={(e) => setNewService({...newService, name: e.target.value})}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="gateway" className="text-right">
                  网关实例
                </Label>
                <Select
                  value={newService.gateway}
                  onValueChange={(value) => setNewService({...newService, gateway: value})}
                >
                  <SelectTrigger className="col-span-3">
                    <SelectValue placeholder="选择网关实例" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="production">Production Gateway</SelectItem>
                    <SelectItem value="staging">Staging Gateway</SelectItem>
                    <SelectItem value="development">Dev Gateway</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="protocol" className="text-right">
                  协议
                </Label>
                <Select
                  value={newService.protocol}
                  onValueChange={(value) => setNewService({...newService, protocol: value})}
                >
                  <SelectTrigger className="col-span-3">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="HTTP">HTTP</SelectItem>
                    <SelectItem value="HTTPS">HTTPS</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="host" className="text-right">
                  主机地址
                </Label>
                <Input
                  id="host"
                  value={newService.host}
                  onChange={(e) => setNewService({...newService, host: e.target.value})}
                  placeholder="api.example.com"
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="port" className="text-right">
                  端口
                </Label>
                <Input
                  id="port"
                  type="number"
                  value={newService.port}
                  onChange={(e) => setNewService({...newService, port: parseInt(e.target.value) || 80})}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="path" className="text-right">
                  路径
                </Label>
                <Input
                  id="path"
                  value={newService.path}
                  onChange={(e) => setNewService({...newService, path: e.target.value})}
                  placeholder="/api/v1"
                  className="col-span-3"
                />
              </div>
            </div>
            <DialogFooter>
              <Button type="submit" onClick={handleLinkService}>
                关联服务
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* 服务统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总服务数</CardTitle>
            <Link className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{linkedServices.length}</div>
            <p className="text-xs text-muted-foreground">个关联服务</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">活跃服务</CardTitle>
            <Settings className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {linkedServices.filter(s => s.status === "active").length}
            </div>
            <p className="text-xs text-muted-foreground">个活跃服务</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">网关实例</CardTitle>
            <ExternalLink className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {new Set(linkedServices.map(s => s.gateway)).size}
            </div>
            <p className="text-xs text-muted-foreground">个网关实例</p>
          </CardContent>
        </Card>
      </div>

      {/* 服务列表 */}
      <Card>
        <CardHeader>
          <CardTitle>关联的服务</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>服务名称</TableHead>
                <TableHead>网关</TableHead>
                <TableHead>地址</TableHead>
                <TableHead>状态</TableHead>
                <TableHead>方法</TableHead>
                <TableHead>最后更新</TableHead>
                <TableHead className="w-[50px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {linkedServices.map((service) => (
                <TableRow key={service.id}>
                  <TableCell>
                    <div>
                      <div className="font-medium">{service.name}</div>
                      <div className="text-sm text-muted-foreground">
                        {service.id}
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">{service.gateway}</div>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">
                      <div>{service.protocol}://{service.host}:{service.port}</div>
                      <div className="text-muted-foreground">{service.path}</div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant={getStatusBadgeVariant(service.status)}>
                      {getStatusText(service.status)}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="flex gap-1 flex-wrap">
                      {service.methods.map((method) => (
                        <Badge key={method} variant="outline" className="text-xs">
                          {method}
                        </Badge>
                      ))}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">{formatDate(service.lastUpdated)}</div>
                  </TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem>编辑配置</DropdownMenuItem>
                        <DropdownMenuItem>测试连接</DropdownMenuItem>
                        <DropdownMenuItem>查看日志</DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem className="text-destructive">
                          取消关联
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
} 