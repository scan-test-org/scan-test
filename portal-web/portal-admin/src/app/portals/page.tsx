"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Plus, ExternalLink, MoreVertical } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { CreatePortalDialog } from "@/components/create-portal-dialog"

interface Portal {
  id: string
  name: string
  description: string
  title: string
  url: string
  userAuth: string
  rbac: string
  authStrategy: string
  apiVisibility: string
  pageVisibility: string
  logo?: string
}

const mockPortals: Portal[] = [
  {
    id: "1",
    name: "test",
    description: "测试公司门户",
    title: "Company",
    url: "https://3995a4355203.us.kongportals.com",
    userAuth: "Konnect Built-in",
    rbac: "Disabled",
    authStrategy: "key-auth",
    apiVisibility: "Private",
    pageVisibility: "Private",
    logo: undefined
  }
]

export default function PortalsPage() {
  const router = useRouter()
  const [portals, setPortals] = useState<Portal[]>(mockPortals)
  const [showCreateDialog, setShowCreateDialog] = useState(false)

  const handleCreatePortal = (newPortal: Omit<Portal, "id">) => {
    const portal: Portal = {
      ...newPortal,
      id: Date.now().toString()
    }
    setPortals([...portals, portal])
    setShowCreateDialog(false)
  }

  const handlePortalClick = (portalId: string) => {
    router.push(`/portals/${portalId}`)
  }

  const handleDropdownClick = (e: React.MouseEvent) => {
    e.stopPropagation() // 阻止事件冒泡，避免触发卡片点击
  }

  const handleLinkClick = (e: React.MouseEvent) => {
    e.stopPropagation() // 阻止事件冒泡
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Portal</h1>
          <p className="text-muted-foreground mt-2">
            管理和配置您的开发者门户
          </p>
        </div>
        <Button onClick={() => setShowCreateDialog(true)}>
          <Plus className="mr-2 h-4 w-4" />
          创建 Portal
        </Button>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {portals.map((portal) => (
          <Card 
            key={portal.id} 
            className="relative cursor-pointer transition-all hover:shadow-lg hover:scale-[1.02] border-2 hover:border-primary/20"
            onClick={() => handlePortalClick(portal.id)}
          >
            <CardHeader className="pb-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <Avatar className="h-10 w-10">
                    <AvatarImage src={portal.logo} />
                    <AvatarFallback>
                      {portal.title.charAt(0).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <CardTitle className="text-lg">{portal.title}</CardTitle>
                  </div>
                </div>
                <div onClick={handleDropdownClick}>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon">
                        <MoreVertical className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem>编辑</DropdownMenuItem>
                      <DropdownMenuItem>复制</DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem className="text-destructive">
                        删除
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <div className="text-sm font-medium">{portal.name}</div>
                <div className="flex items-center space-x-2 text-sm text-muted-foreground">
                  <ExternalLink className="h-3 w-3" />
                  <a 
                    href={portal.url} 
                    target="_blank" 
                    rel="noopener noreferrer"
                    className="hover:underline text-blue-600"
                    onClick={handleLinkClick}
                  >
                    {portal.url}
                  </a>
                </div>
              </div>

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">User authentication</span>
                  <span className="text-blue-600">{portal.userAuth}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">RBAC</span>
                  <Badge variant="secondary">{portal.rbac}</Badge>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Authentication strategy</span>
                  <span className="text-blue-600">{portal.authStrategy}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Default API visibility</span>
                  <Badge variant="outline">{portal.apiVisibility}</Badge>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-muted-foreground">Default page visibility</span>
                  <Badge variant="outline">{portal.pageVisibility}</Badge>
                </div>
              </div>

              {/* 点击提示 */}
              <div className="text-xs text-muted-foreground text-center pt-2 border-t">
                点击查看详情
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <CreatePortalDialog
        open={showCreateDialog}
        onOpenChange={setShowCreateDialog}
        onSubmit={handleCreatePortal}
      />
    </div>
  )
} 