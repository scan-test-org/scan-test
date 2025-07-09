"use client"

import { useState, useEffect, useRef } from "react"
import { useParams, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { 
  LayoutDashboard, 
  Database, 
  Users, 
  UserCheck, 
  Settings, 
  ExternalLink,
  MoreVertical,
  ChevronLeft
} from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { useSidebar } from "@/components/ui/sidebar"
import { PortalOverview } from "@/components/portal/portal-overview"
import { PortalPublishedApis } from "@/components/portal/portal-published-apis"
import { PortalDevelopers } from "@/components/portal/portal-developers"
import { PortalConsumers } from "@/components/portal/portal-consumers"
import { PortalSettings } from "@/components/portal/portal-settings"

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

const mockPortal: Portal = {
  id: "1",
  name: "test",
  title: "Company",
  description: "测试公司门户",
  url: "https://3995a4355203.us.kongportals.com",
  userAuth: "Konnect Built-in",
  rbac: "Disabled",
  authStrategy: "Key-Auth",
  apiVisibility: "Private",
  pageVisibility: "Private"
}

const menuItems = [
  {
    key: "overview",
    label: "Overview",
    icon: LayoutDashboard,
  },
  {
    key: "published-apis",
    label: "Published APIs",
    icon: Database,
  },
  {
    key: "developers",
    label: "Developers",
    icon: Users,
  },
  {
    key: "consumers",
    label: "Consumers",
    icon: UserCheck,
  },
  {
    key: "settings",
    label: "Settings",
    icon: Settings,
  }
]

export default function PortalDetailPage() {
  const params = useParams()
  const router = useRouter()
  const { open, setOpen } = useSidebar()
  const [activeTab, setActiveTab] = useState("overview")
  const [isInitialized, setIsInitialized] = useState(false)
  const portal = mockPortal // 实际项目中从API获取

  // 页面进入时收起主侧边栏，离开时自动恢复
  useEffect(() => {
    // 保存当前侧边栏状态到sessionStorage中，以便在返回时恢复
    const currentSidebarState = open
    sessionStorage.setItem('portal-detail-previous-sidebar-state', JSON.stringify(currentSidebarState))
    
    // 收起侧边栏
    if (open) {
      setOpen(false)
    }
    
    setIsInitialized(true)

    // 清理函数：页面卸载时恢复侧边栏状态
    return () => {
      const savedState = sessionStorage.getItem('portal-detail-previous-sidebar-state')
      if (savedState) {
        try {
          const previousState = JSON.parse(savedState)
          setOpen(previousState)
          sessionStorage.removeItem('portal-detail-previous-sidebar-state')
        } catch (error) {
          console.error('Failed to restore sidebar state:', error)
          setOpen(true) // 默认展开
        }
      }
    }
  }, []) // 空依赖数组，只在挂载和卸载时执行

  const handleBackToPortals = () => {
    // 先恢复侧边栏状态
    const savedState = sessionStorage.getItem('portal-detail-previous-sidebar-state')
    if (savedState) {
      try {
        const previousState = JSON.parse(savedState)
        setOpen(previousState)
      } catch (error) {
        setOpen(true) // 默认展开
      }
    } else {
      setOpen(true) // 默认展开
    }
    
    // 清除保存的状态
    sessionStorage.removeItem('portal-detail-previous-sidebar-state')
    
    // 延迟导航，确保状态更新完成
    setTimeout(() => {
      router.push('/portals')
    }, 100)
  }

  const renderContent = () => {
    switch (activeTab) {
      case "overview":
        return <PortalOverview portal={portal} />
      case "published-apis":
        return <PortalPublishedApis portal={portal} />
      case "developers":
        return <PortalDevelopers portal={portal} />
      case "consumers":
        return <PortalConsumers portal={portal} />
      case "settings":
        return <PortalSettings portal={portal} />
      default:
        return <PortalOverview portal={portal} />
    }
  }

  // 等待初始化完成再渲染
  if (!isInitialized) {
    return <div className="flex h-full items-center justify-center">Loading...</div>
  }

  return (
    <div className="flex h-full">
      {/* Portal详情侧边栏 */}
      <div className="w-64 border-r bg-card flex flex-col">
        {/* 返回按钮 */}
        <div className="p-4 border-b">
          <Button 
            variant="ghost" 
            className="w-full justify-start"
            onClick={handleBackToPortals}
          >
            <ChevronLeft className="mr-2 h-4 w-4" />
            返回 Portal 列表
          </Button>
        </div>

        {/* Portal 信息 */}
        <div className="p-4 border-b">
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-lg font-semibold">{portal.title}</h2>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon">
                  <MoreVertical className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem>编辑Portal</DropdownMenuItem>
                <DropdownMenuItem>复制Portal</DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem className="text-destructive">
                  删除Portal
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
          <p className="text-sm text-muted-foreground mb-2">{portal.name}</p>
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <ExternalLink className="h-3 w-3" />
            <a 
              href={portal.url} 
              target="_blank" 
              rel="noopener noreferrer"
              className="hover:underline truncate"
            >
              {portal.url}
            </a>
          </div>
        </div>

        {/* 导航菜单 */}
        <nav className="flex-1 p-4 space-y-1">
          {menuItems.map((item) => (
            <button
              key={item.key}
              onClick={() => setActiveTab(item.key)}
              className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-left transition-colors ${
                activeTab === item.key
                  ? "bg-primary text-primary-foreground"
                  : "hover:bg-muted"
              }`}
            >
              <item.icon className="h-4 w-4" />
              <div>
                <div className="font-medium">{item.label}</div>
                <div className="text-xs opacity-70">{item.description}</div>
              </div>
            </button>
          ))}
        </nav>
      </div>

      {/* 主内容区域 */}
      <div className="flex-1 overflow-auto">
        {renderContent()}
      </div>
    </div>
  )
} 