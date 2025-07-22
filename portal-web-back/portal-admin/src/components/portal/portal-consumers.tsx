"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Key, Plus, Monitor } from "lucide-react"

interface Portal {
  id: string
  name: string
  title: string
  description: string
  url: string
  userAuth: string
  rbac: string
  authStrategy: string
  apiVisibility: string
  pageVisibility: string
  logo?: string
}

interface Consumer {
  id: string
  name: string
  description: string
  developerId: string
  developerName: string
  status: "active" | "inactive" | "suspended"
  createdAt: string
  lastUsed: string
  requestCount: number
}

interface PortalConsumersProps {
  portal: Portal
}

const mockConsumers: Consumer[] = [
  {
    id: "1",
    name: "Mobile App Consumer",
    description: "移动应用的API消费者",
    developerId: "1",
    developerName: "张三",
    status: "active",
    createdAt: "2025-01-08",
    lastUsed: "2025-01-08T10:30:00Z",
    requestCount: 1250
  },
  {
    id: "2",
    name: "Web Dashboard",
    description: "Web仪表板应用",
    developerId: "1", 
    developerName: "张三",
    status: "active",
    createdAt: "2025-01-07",
    lastUsed: "2025-01-08T09:15:00Z",
    requestCount: 856
  },
  {
    id: "3",
    name: "Test Consumer",
    description: "测试用消费者",
    developerId: "2",
    developerName: "李四",
    status: "inactive",
    createdAt: "2025-01-06",
    lastUsed: "2025-01-06T16:20:00Z",
    requestCount: 23
  }
]

export function PortalConsumers({ portal }: PortalConsumersProps) {
  const consumers = mockConsumers

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "active":
        return <Badge variant="default">活跃</Badge>
      case "inactive":
        return <Badge variant="secondary">非活跃</Badge>
      case "suspended":
        return <Badge variant="destructive">已暂停</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
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

  if (consumers.length === 0) {
    return (
      <div className="p-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold">Consumers</h1>
            <p className="text-muted-foreground mt-1">
              管理开发者注册的API消费者
            </p>
          </div>
        </div>

        {/* 空状态 */}
        <Card className="border-2 border-dashed">
          <CardContent className="flex flex-col items-center justify-center py-16">
            <div className="w-16 h-16 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center mb-4">
              <Key className="h-8 w-8 text-blue-600" />
            </div>
            
            <h2 className="text-xl font-semibold mb-2">No consumers yet</h2>
            
            <p className="text-muted-foreground text-center max-w-md mb-6">
              When developers register applications as consumers, they will appear here for management and monitoring.
            </p>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold">Consumers</h1>
          <p className="text-muted-foreground mt-1">
            管理开发者注册的API消费者 ({consumers.length} 个消费者)
          </p>
        </div>
      </div>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>消费者</TableHead>
              <TableHead>开发者</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>请求数量</TableHead>
              <TableHead>最后使用</TableHead>
              <TableHead>创建时间</TableHead>
              <TableHead className="w-[100px]">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {consumers.map((consumer) => (
              <TableRow key={consumer.id}>
                <TableCell>
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-blue-100 dark:bg-blue-900/30 rounded-md">
                      <Monitor className="h-4 w-4 text-blue-600" />
                    </div>
                    <div>
                      <div className="font-medium">{consumer.name}</div>
                      <div className="text-sm text-muted-foreground">
                        {consumer.description}
                      </div>
                    </div>
                  </div>
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <Avatar className="h-6 w-6">
                      <AvatarFallback className="text-xs">
                        {consumer.developerName.charAt(0)}
                      </AvatarFallback>
                    </Avatar>
                    <span className="text-sm">{consumer.developerName}</span>
                  </div>
                </TableCell>
                <TableCell>
                  {getStatusBadge(consumer.status)}
                </TableCell>
                <TableCell>
                  <span className="font-mono">{consumer.requestCount.toLocaleString()}</span>
                </TableCell>
                <TableCell>
                  <span className="text-sm">{formatDate(consumer.lastUsed)}</span>
                </TableCell>
                <TableCell>
                  <span className="text-sm">{new Date(consumer.createdAt).toLocaleDateString("zh-CN")}</span>
                </TableCell>
                <TableCell>
                  <Button variant="outline" size="sm">
                    管理
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Card>
    </div>
  )
} 