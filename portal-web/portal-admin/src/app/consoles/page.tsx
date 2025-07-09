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

interface Console {
  id: string
  name: string
  description: string
  type: "web" | "mobile" | "desktop"
  status: "active" | "inactive" | "maintenance"
  endpoint: string
  version: string
  lastUpdated: string
  environment: "production" | "staging" | "development"
}

const mockConsoles: Console[] = [
  {
    id: "1",
    name: "Admin Console",
    description: "主管理控制台",
    type: "web",
    status: "active",
    endpoint: "https://admin.example.com",
    version: "1.0.0",
    lastUpdated: "2025-01-08T10:30:00Z",
    environment: "production"
  },
  {
    id: "2",
    name: "Developer Portal",
    description: "开发者门户控制台",
    type: "web",
    status: "active",
    endpoint: "https://dev.example.com",
    version: "2.1.0",
    lastUpdated: "2025-01-07T15:45:00Z",
    environment: "staging"
  }
]

export default function ConsolesPage() {
  const [consoles, setConsoles] = useState<Console[]>(mockConsoles)
  const [showCreateDialog, setShowCreateDialog] = useState(false)

  const handleCreateConsole = (newConsole: Omit<Console, "id" | "lastUpdated">) => {
    const console: Console = {
      ...newConsole,
      id: Date.now().toString(),
      lastUpdated: new Date().toISOString()
    }
    setConsoles([...consoles, console])
    setShowCreateDialog(false)
  }

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case "active":
        return "default"
      case "inactive":
        return "secondary"
      case "maintenance":
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
      case "maintenance":
        return "维护中"
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
          <h1 className="text-3xl font-bold tracking-tight">Console</h1>
          <p className="text-muted-foreground mt-2">
            管理和配置您的Console实例
          </p>
        </div>
        <Button onClick={() => setShowCreateDialog(true)}>
          <Plus className="mr-2 h-4 w-4" />
          创建 Console
        </Button>
      </div>

      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>名称</TableHead>
              <TableHead>最后更新</TableHead>
              <TableHead className="w-[50px]"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {consoles.map((console) => (
              <TableRow key={console.id}>
                <TableCell>
                  <div>
                    <div className="font-medium">{console.name}</div>
                    <div className="text-sm text-muted-foreground">
                      {console.description}
                    </div>
                  </div>
                </TableCell>
                <TableCell>
                  {formatDate(console.lastUpdated)}
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
        onSubmit={handleCreateConsole}
      />
    </div>
  )
} 