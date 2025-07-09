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
import { 
  Plus, 
  MoreVertical, 
  Shield, 
  Clock, 
  Key,
  Globe,
  FileText,
  ToggleLeft,
  ToggleRight,
  Settings
} from "lucide-react"
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
import { Textarea } from "@/components/ui/textarea"

interface Policy {
  id: string
  name: string
  type: "rate-limiting" | "authentication" | "cors" | "ip-restriction" | "request-size-limiting"
  enabled: boolean
  priority: number
  description: string
  config: Record<string, any>
  createdAt: string
  updatedAt: string
}

const mockPolicies: Policy[] = [
  {
    id: "policy-001",
    name: "Rate Limiting",
    type: "rate-limiting",
    enabled: true,
    priority: 1,
    description: "限制API调用频率",
    config: {
      requests_per_minute: 100,
      burst_size: 50,
      key_type: "ip"
    },
    createdAt: "2025-01-05T10:00:00Z",
    updatedAt: "2025-01-07T14:30:00Z"
  },
  {
    id: "policy-002",
    name: "Key Authentication",
    type: "authentication",
    enabled: true,
    priority: 2,
    description: "API密钥认证",
    config: {
      key_names: ["X-API-Key", "apikey"],
      hide_credentials: true
    },
    createdAt: "2025-01-05T10:00:00Z",
    updatedAt: "2025-01-05T10:00:00Z"
  },
  {
    id: "policy-003",
    name: "CORS Policy",
    type: "cors",
    enabled: true,
    priority: 3,
    description: "跨域资源共享策略",
    config: {
      origins: ["https://example.com", "https://app.example.com"],
      methods: ["GET", "POST", "PUT", "DELETE"],
      headers: ["Content-Type", "Authorization"]
    },
    createdAt: "2025-01-04T09:15:00Z",
    updatedAt: "2025-01-06T16:45:00Z"
  },
  {
    id: "policy-004",
    name: "IP Restriction",
    type: "ip-restriction",
    enabled: false,
    priority: 4,
    description: "IP地址访问限制",
    config: {
      allow: ["192.168.1.0/24", "10.0.0.0/8"],
      deny: []
    },
    createdAt: "2025-01-03T15:20:00Z",
    updatedAt: "2025-01-03T15:20:00Z"
  },
  {
    id: "policy-005",
    name: "Request Size Limiting",
    type: "request-size-limiting",
    enabled: true,
    priority: 5,
    description: "请求大小限制",
    config: {
      allowed_payload_size: 10,
      size_unit: "megabytes"
    },
    createdAt: "2025-01-02T11:30:00Z",
    updatedAt: "2025-01-02T11:30:00Z"
  }
]

export default function PolicyPage() {
  const [policies, setPolicies] = useState<Policy[]>(mockPolicies)
  const [showAddDialog, setShowAddDialog] = useState(false)
  const [selectedPolicy, setSelectedPolicy] = useState<Policy | null>(null)
  const [showConfigDialog, setShowConfigDialog] = useState(false)
  const [newPolicy, setNewPolicy] = useState({
    name: "",
    type: "",
    description: "",
    config: "{}"
  })

  const getPolicyTypeIcon = (type: string) => {
    switch (type) {
      case "rate-limiting":
        return Clock
      case "authentication":
        return Key
      case "cors":
        return Globe
      case "ip-restriction":
        return Shield
      case "request-size-limiting":
        return FileText
      default:
        return Settings
    }
  }

  const getPolicyTypeName = (type: string) => {
    switch (type) {
      case "rate-limiting":
        return "限流策略"
      case "authentication":
        return "认证策略"
      case "cors":
        return "CORS策略"
      case "ip-restriction":
        return "IP限制"
      case "request-size-limiting":
        return "请求大小限制"
      default:
        return type
    }
  }

  const getPolicyTypeBadgeVariant = (type: string) => {
    switch (type) {
      case "rate-limiting":
        return "default"
      case "authentication":
        return "secondary"
      case "cors":
        return "outline"
      case "ip-restriction":
        return "destructive"
      case "request-size-limiting":
        return "secondary"
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

  const handleTogglePolicy = (policyId: string) => {
    setPolicies(policies.map(policy => 
      policy.id === policyId 
        ? { ...policy, enabled: !policy.enabled, updatedAt: new Date().toISOString() }
        : policy
    ))
  }

  const handleAddPolicy = () => {
    try {
      const config = JSON.parse(newPolicy.config)
      const policy: Policy = {
        id: `policy-${Date.now()}`,
        name: newPolicy.name,
        type: newPolicy.type as Policy["type"],
        enabled: true,
        priority: policies.length + 1,
        description: newPolicy.description,
        config,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      }
      setPolicies([...policies, policy])
      setShowAddDialog(false)
      setNewPolicy({
        name: "",
        type: "",
        description: "",
        config: "{}"
      })
    } catch (error) {
      console.error("Invalid JSON config:", error)
    }
  }

  const handleDeletePolicy = (policyId: string) => {
    setPolicies(policies.filter(policy => policy.id !== policyId))
  }

  const handleConfigurePolicy = (policy: Policy) => {
    setSelectedPolicy(policy)
    setShowConfigDialog(true)
  }

  const getEnabledPoliciesCount = () => {
    return policies.filter(p => p.enabled).length
  }

  const getPolicyTypeCount = (type: string) => {
    return policies.filter(p => p.type === type).length
  }

  return (
    <div className="space-y-6 p-6">
      {/* 页面标题和操作 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">策略管理</h1>
          <p className="text-muted-foreground mt-1">
            管理 API Product 的访问策略和安全配置
          </p>
        </div>
        <Dialog open={showAddDialog} onOpenChange={setShowAddDialog}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              添加策略
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[500px]">
            <DialogHeader>
              <DialogTitle>添加新策略</DialogTitle>
              <DialogDescription>
                为当前 API Product 添加一个新的策略配置
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="policy-name" className="text-right">
                  策略名称
                </Label>
                <Input
                  id="policy-name"
                  value={newPolicy.name}
                  onChange={(e) => setNewPolicy({...newPolicy, name: e.target.value})}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="policy-type" className="text-right">
                  策略类型
                </Label>
                <Select
                  value={newPolicy.type}
                  onValueChange={(value) => setNewPolicy({...newPolicy, type: value})}
                >
                  <SelectTrigger className="col-span-3">
                    <SelectValue placeholder="选择策略类型" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="rate-limiting">限流策略</SelectItem>
                    <SelectItem value="authentication">认证策略</SelectItem>
                    <SelectItem value="cors">CORS策略</SelectItem>
                    <SelectItem value="ip-restriction">IP限制</SelectItem>
                    <SelectItem value="request-size-limiting">请求大小限制</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="policy-description" className="text-right">
                  描述
                </Label>
                <Input
                  id="policy-description"
                  value={newPolicy.description}
                  onChange={(e) => setNewPolicy({...newPolicy, description: e.target.value})}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="policy-config" className="text-right">
                  配置 (JSON)
                </Label>
                <Textarea
                  id="policy-config"
                  value={newPolicy.config}
                  onChange={(e) => setNewPolicy({...newPolicy, config: e.target.value})}
                  placeholder='{"key": "value"}'
                  className="col-span-3 font-mono text-sm"
                />
              </div>
            </div>
            <DialogFooter>
              <Button type="submit" onClick={handleAddPolicy}>
                添加策略
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* 策略统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总策略数</CardTitle>
            <Shield className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{policies.length}</div>
            <p className="text-xs text-muted-foreground">个配置策略</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">启用策略</CardTitle>
            <ToggleRight className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{getEnabledPoliciesCount()}</div>
            <p className="text-xs text-muted-foreground">个启用策略</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">认证策略</CardTitle>
            <Key className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{getPolicyTypeCount("authentication")}</div>
            <p className="text-xs text-muted-foreground">个认证规则</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">限流策略</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{getPolicyTypeCount("rate-limiting")}</div>
            <p className="text-xs text-muted-foreground">个限流规则</p>
          </CardContent>
        </Card>
      </div>

      {/* 策略列表 */}
      <Card>
        <CardHeader>
          <CardTitle>策略配置</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>策略名称</TableHead>
                <TableHead>类型</TableHead>
                <TableHead>状态</TableHead>
                <TableHead>优先级</TableHead>
                <TableHead>最后更新</TableHead>
                <TableHead className="w-[50px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {policies
                .sort((a, b) => a.priority - b.priority)
                .map((policy) => {
                  const IconComponent = getPolicyTypeIcon(policy.type)
                  return (
                    <TableRow key={policy.id}>
                      <TableCell>
                        <div className="flex items-center gap-3">
                          <IconComponent className="h-5 w-5 text-muted-foreground" />
                          <div>
                            <div className="font-medium">{policy.name}</div>
                            <div className="text-sm text-muted-foreground">
                              {policy.description}
                            </div>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant={getPolicyTypeBadgeVariant(policy.type)}>
                          {getPolicyTypeName(policy.type)}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleTogglePolicy(policy.id)}
                            className="p-0 h-auto"
                          >
                            {policy.enabled ? (
                              <ToggleRight className="h-5 w-5 text-green-600" />
                            ) : (
                              <ToggleLeft className="h-5 w-5 text-gray-400" />
                            )}
                          </Button>
                          <span className="text-sm">
                            {policy.enabled ? "启用" : "禁用"}
                          </span>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline">{policy.priority}</Badge>
                      </TableCell>
                      <TableCell>
                        <div className="text-sm">{formatDate(policy.updatedAt)}</div>
                      </TableCell>
                      <TableCell>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon">
                              <MoreVertical className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => handleConfigurePolicy(policy)}>
                              配置策略
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => handleTogglePolicy(policy.id)}>
                              {policy.enabled ? "禁用" : "启用"}
                            </DropdownMenuItem>
                            <DropdownMenuItem>复制策略</DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem 
                              className="text-destructive"
                              onClick={() => handleDeletePolicy(policy.id)}
                            >
                              删除策略
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  )
                })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* 策略配置对话框 */}
      <Dialog open={showConfigDialog} onOpenChange={setShowConfigDialog}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>配置策略</DialogTitle>
            <DialogDescription>
              编辑 {selectedPolicy?.name} 的配置参数
            </DialogDescription>
          </DialogHeader>
          {selectedPolicy && (
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label>策略类型</Label>
                <div className="text-sm">{getPolicyTypeName(selectedPolicy.type)}</div>
              </div>
              <div className="space-y-2">
                <Label>当前配置 (JSON)</Label>
                <Textarea
                  value={JSON.stringify(selectedPolicy.config, null, 2)}
                  readOnly
                  className="min-h-[200px] font-mono text-sm"
                />
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowConfigDialog(false)}>
              取消
            </Button>
            <Button>保存配置</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 快速操作 */}
      <Card>
        <CardHeader>
          <CardTitle>快速操作</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Button variant="outline" className="justify-start">
              <Shield className="mr-2 h-4 w-4" />
              批量启用
            </Button>
            <Button variant="outline" className="justify-start">
              <Settings className="mr-2 h-4 w-4" />
              策略模板
            </Button>
            <Button variant="outline" className="justify-start">
              <FileText className="mr-2 h-4 w-4" />
              导出配置
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
} 