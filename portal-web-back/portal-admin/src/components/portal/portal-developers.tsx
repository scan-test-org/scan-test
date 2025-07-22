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
import { Users, UserPlus } from "lucide-react"

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

interface Developer {
  id: string
  name: string
  email: string
  status: "approved" | "pending" | "rejected"
  registeredAt: string
  avatar?: string
}

interface PortalDevelopersProps {
  portal: Portal
}

const mockDevelopers: Developer[] = [
  {
    id: "1",
    name: "张三",
    email: "zhangsan@example.com",
    status: "approved",
    registeredAt: "2025-01-08",
    avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=zhangsan"
  },
  {
    id: "2", 
    name: "李四",
    email: "lisi@example.com",
    status: "pending",
    registeredAt: "2025-01-07",
    avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=lisi"
  }
]

export function PortalDevelopers({ portal }: PortalDevelopersProps) {
  const developers = mockDevelopers

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "approved":
        return <Badge variant="default">已批准</Badge>
      case "pending":
        return <Badge variant="secondary">待审核</Badge>
      case "rejected":
        return <Badge variant="destructive">已拒绝</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  if (developers.length === 0) {
    return (
      <div className="p-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold">Developers</h1>
            <p className="text-muted-foreground mt-1">
              管理注册到Portal的开发者
            </p>
          </div>
          <Button>
            <UserPlus className="mr-2 h-4 w-4" />
            邀请开发者
          </Button>
        </div>

        {/* 空状态 */}
        <Card className="border-2 border-dashed">
          <CardContent className="flex flex-col items-center justify-center py-16">
            <div className="w-16 h-16 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center mb-4">
              <Users className="h-8 w-8 text-blue-600" />
            </div>
            
            <h2 className="text-xl font-semibold mb-2">No developers yet</h2>
            
            <p className="text-muted-foreground text-center max-w-md mb-6">
              When developers register to your portal, they will appear here for approval and management.
            </p>
            
            <Button size="lg">
              <UserPlus className="mr-2 h-4 w-4" />
              邀请开发者
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold">Developers</h1>
          <p className="text-muted-foreground mt-1">
            管理注册到Portal的开发者 ({developers.length} 位开发者)
          </p>
        </div>
        <Button>
          <UserPlus className="mr-2 h-4 w-4" />
          邀请开发者
        </Button>
      </div>

      <Card>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>开发者</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>注册时间</TableHead>
              <TableHead className="w-[100px]">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {developers.map((developer) => (
              <TableRow key={developer.id}>
                <TableCell>
                  <div className="flex items-center gap-3">
                    <Avatar className="h-8 w-8">
                      <AvatarImage src={developer.avatar} />
                      <AvatarFallback>
                        {developer.name.charAt(0)}
                      </AvatarFallback>
                    </Avatar>
                    <div>
                      <div className="font-medium">{developer.name}</div>
                      <div className="text-sm text-muted-foreground">
                        {developer.email}
                      </div>
                    </div>
                  </div>
                </TableCell>
                <TableCell>
                  {getStatusBadge(developer.status)}
                </TableCell>
                <TableCell>
                  {new Date(developer.registeredAt).toLocaleDateString("zh-CN")}
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