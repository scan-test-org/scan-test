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
  Globe, 
  Users, 
  ExternalLink,
  TrendingUp,
  Calendar,
  CheckCircle,
  XCircle
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

interface PublishedPortal {
  id: string
  name: string
  title: string
  url: string
  status: "published" | "draft" | "unpublished"
  publishedAt: string
  visibility: "public" | "private"
  developers: number
  apiCalls: number
  description: string
}

const mockPublishedPortals: PublishedPortal[] = [
  {
    id: "portal-001",
    name: "company-portal",
    title: "Company Developer Portal",
    url: "https://developer.company.com",
    status: "published",
    publishedAt: "2025-01-05T10:00:00Z",
    visibility: "public",
    developers: 145,
    apiCalls: 12456,
    description: "面向外部开发者的公开门户"
  },
  {
    id: "portal-002", 
    name: "partner-portal",
    title: "Partner API Portal",
    url: "https://partner-api.company.com",
    status: "published",
    publishedAt: "2025-01-03T14:30:00Z",
    visibility: "private",
    developers: 28,
    apiCalls: 3421,
    description: "合作伙伴专用的私有门户"
  },
  {
    id: "portal-003",
    name: "internal-portal",
    title: "Internal API Portal",
    url: "https://internal-api.company.com",
    status: "draft",
    publishedAt: "",
    visibility: "private",
    developers: 0,
    apiCalls: 0,
    description: "内部团队使用的API门户"
  }
]

export default function PortalPage() {
  const [publishedPortals, setPublishedPortals] = useState<PublishedPortal[]>(mockPublishedPortals)
  const [showPublishDialog, setShowPublishDialog] = useState(false)
  const [newPortal, setNewPortal] = useState({
    portalId: "",
    description: ""
  })

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case "published":
        return "default"
      case "draft":
        return "secondary"
      case "unpublished":
        return "outline"
      default:
        return "secondary"
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case "published":
        return "已发布"
      case "draft":
        return "草稿"
      case "unpublished":
        return "已下线"
      default:
        return status
    }
  }

  const getVisibilityBadgeVariant = (visibility: string) => {
    switch (visibility) {
      case "public":
        return "outline"
      case "private":
        return "secondary"
      default:
        return "secondary"
    }
  }

  const getVisibilityText = (visibility: string) => {
    switch (visibility) {
      case "public":
        return "公开"
      case "private":
        return "私有"
      default:
        return visibility
    }
  }

  const formatDate = (dateString: string) => {
    if (!dateString) return "-"
    return new Date(dateString).toLocaleDateString("zh-CN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit"
    })
  }

  const handlePublishToPortal = () => {
    // 这里实际会调用API发布到门户
    console.log("Publishing to portal:", newPortal)
    setShowPublishDialog(false)
    setNewPortal({
      portalId: "",
      description: ""
    })
  }

  const handleUnpublish = (portalId: string) => {
    setPublishedPortals(portals => 
      portals.map(portal => 
        portal.id === portalId 
          ? { ...portal, status: "unpublished" as const }
          : portal
      )
    )
  }

  const handleRepublish = (portalId: string) => {
    setPublishedPortals(portals => 
      portals.map(portal => 
        portal.id === portalId 
          ? { ...portal, status: "published" as const, publishedAt: new Date().toISOString() }
          : portal
      )
    )
  }

  const getTotalDevelopers = () => {
    return publishedPortals
      .filter(p => p.status === "published")
      .reduce((sum, portal) => sum + portal.developers, 0)
  }

  const getTotalApiCalls = () => {
    return publishedPortals
      .filter(p => p.status === "published")
      .reduce((sum, portal) => sum + portal.apiCalls, 0)
  }

  const getPublishedCount = () => {
    return publishedPortals.filter(p => p.status === "published").length
  }

  return (
    <div className="space-y-6 p-6">
      {/* 页面标题和操作 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">发布的门户</h1>
          <p className="text-muted-foreground mt-1">
            管理 API Product 在各个开发者门户的发布状态
          </p>
        </div>
        <Dialog open={showPublishDialog} onOpenChange={setShowPublishDialog}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              发布到门户
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[425px]">
            <DialogHeader>
              <DialogTitle>发布到门户</DialogTitle>
              <DialogDescription>
                选择要发布当前 API Product 的开发者门户
              </DialogDescription>
            </DialogHeader>
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="portal" className="text-right">
                  选择门户
                </Label>
                <Select
                  value={newPortal.portalId}
                  onValueChange={(value) => setNewPortal({...newPortal, portalId: value})}
                >
                  <SelectTrigger className="col-span-3">
                    <SelectValue placeholder="选择门户" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="company-portal">Company Developer Portal</SelectItem>
                    <SelectItem value="partner-portal">Partner API Portal</SelectItem>
                    <SelectItem value="internal-portal">Internal API Portal</SelectItem>
                    <SelectItem value="test-portal">Test Portal</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="grid grid-cols-4 items-center gap-4">
                <Label htmlFor="description" className="text-right">
                  发布说明
                </Label>
                <Textarea
                  id="description"
                  value={newPortal.description}
                  onChange={(e) => setNewPortal({...newPortal, description: e.target.value})}
                  placeholder="可选的发布说明..."
                  className="col-span-3"
                />
              </div>
            </div>
            <DialogFooter>
              <Button type="submit" onClick={handlePublishToPortal}>
                发布
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* 门户统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">发布门户</CardTitle>
            <Globe className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{getPublishedCount()}</div>
            <p className="text-xs text-muted-foreground">个门户发布</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总开发者</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{getTotalDevelopers()}</div>
            <p className="text-xs text-muted-foreground">个注册开发者</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">API 调用</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{getTotalApiCalls().toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">总调用次数</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">最新发布</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-sm font-bold">
              {publishedPortals
                .filter(p => p.publishedAt)
                .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())[0]
                ?.publishedAt ? formatDate(publishedPortals
                  .filter(p => p.publishedAt)
                  .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())[0]
                  .publishedAt) : "无"
              }
            </div>
            <p className="text-xs text-muted-foreground">最近发布时间</p>
          </CardContent>
        </Card>
      </div>

      {/* 门户列表 */}
      <Card>
        <CardHeader>
          <CardTitle>门户发布状态</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>门户名称</TableHead>
                <TableHead>状态</TableHead>
                <TableHead>可见性</TableHead>
                <TableHead>开发者</TableHead>
                <TableHead>API调用</TableHead>
                <TableHead>发布时间</TableHead>
                <TableHead className="w-[50px]"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {publishedPortals.map((portal) => (
                <TableRow key={portal.id}>
                  <TableCell>
                    <div>
                      <div className="font-medium">{portal.title}</div>
                      <div className="text-sm text-muted-foreground">
                        {portal.description}
                      </div>
                      {portal.url && (
                        <div className="flex items-center gap-1 text-xs text-blue-600 mt-1">
                          <ExternalLink className="h-3 w-3" />
                          <a href={portal.url} target="_blank" rel="noopener noreferrer" className="hover:underline">
                            {portal.url}
                          </a>
                        </div>
                      )}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Badge variant={getStatusBadgeVariant(portal.status)}>
                        {getStatusText(portal.status)}
                      </Badge>
                      {portal.status === "published" && (
                        <CheckCircle className="h-4 w-4 text-green-600" />
                      )}
                      {portal.status === "unpublished" && (
                        <XCircle className="h-4 w-4 text-red-600" />
                      )}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant={getVisibilityBadgeVariant(portal.visibility)}>
                      {getVisibilityText(portal.visibility)}
                    </Badge>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">
                      {portal.developers.toLocaleString()}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">
                      {portal.apiCalls.toLocaleString()}
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="text-sm">{formatDate(portal.publishedAt)}</div>
                  </TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        {portal.status === "published" ? (
                          <DropdownMenuItem onClick={() => handleUnpublish(portal.id)}>
                            下线
                          </DropdownMenuItem>
                        ) : (
                          <DropdownMenuItem onClick={() => handleRepublish(portal.id)}>
                            重新发布
                          </DropdownMenuItem>
                        )}
                        <DropdownMenuItem>编辑配置</DropdownMenuItem>
                        <DropdownMenuItem>查看统计</DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem className="text-destructive">
                          移除
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

      {/* 快速操作 */}
      <Card>
        <CardHeader>
          <CardTitle>快速操作</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Button variant="outline" className="justify-start">
              <Globe className="mr-2 h-4 w-4" />
              批量发布
            </Button>
            <Button variant="outline" className="justify-start">
              <TrendingUp className="mr-2 h-4 w-4" />
              查看分析
            </Button>
            <Button variant="outline" className="justify-start">
              <Users className="mr-2 h-4 w-4" />
              管理开发者
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
} 