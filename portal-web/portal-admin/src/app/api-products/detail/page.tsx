"use client"

import { useState, useEffect } from "react"
import { useParams, useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { 
  LayoutDashboard, 
  FileText, 
  BookOpen,
  Link, 
  Globe,
  Shield, 
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
import { ApiProductOverview } from "@/components/api-product/api-product-overview"
import { ApiProductLinkApi } from "@/components/api-product/api-product-link-api"
import { ApiProductApiDocs } from "@/components/api-product/api-product-api-docs"
import { ApiProductUsageGuide } from "@/components/api-product/api-product-usage-guide"
import { ApiProductPortal } from "@/components/api-product/api-product-portal"
import { ApiProductPolicy } from "@/components/api-product/api-product-policy"

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

const mockApiProduct: ApiProduct = {
  id: "api-001",
  name: "Payment API",
  description: "支付处理API，提供完整的支付解决方案",
  version: "v1.2.0",
  status: "published",
  visibility: "public",
  createdAt: "2025-01-01T10:00:00Z",
  updatedAt: "2025-01-08T15:30:00Z",
  portals: 3,
  linkedServices: 5,
  policies: 8
}

const menuItems = [
  {
    key: "overview",
    label: "Overview",
    icon: LayoutDashboard,
    description: "基本信息和统计"
  },
  {
    key: "link-api",
    label: "Link API",
    icon: Link,
    description: "关联的网关服务"
  },
  {
    key: "api-docs",
    label: "API Docs",
    icon: FileText,
    description: "API文档编辑"
  },
  {
    key: "usage-guide",
    label: "Usage Guide",
    icon: BookOpen,
    description: "使用指南编辑"
  },
  {
    key: "portal",
    label: "Portal",
    icon: Globe,
    description: "发布的门户"
  },
  {
    key: "policy",
    label: "Policy",
    icon: Shield,
    description: "策略管理"
  }
]

export default function ApiProductDetailPage() {
  const params = useParams()
  const router = useRouter()
  const { open, setOpen } = useSidebar()
  const [activeTab, setActiveTab] = useState("overview")
  const [isInitialized, setIsInitialized] = useState(false)
  const apiProduct = mockApiProduct // 实际项目中从API获取

  // 页面进入时收起主侧边栏，离开时自动恢复
  useEffect(() => {
    // 保存当前侧边栏状态到sessionStorage中，以便在返回时恢复
    const currentSidebarState = open
    sessionStorage.setItem('api-product-detail-previous-sidebar-state', JSON.stringify(currentSidebarState))
    
    // 收起侧边栏
    if (open) {
      setOpen(false)
    }
    
    setIsInitialized(true)

    // 清理函数：页面卸载时恢复侧边栏状态
    return () => {
      const savedState = sessionStorage.getItem('api-product-detail-previous-sidebar-state')
      if (savedState) {
        try {
          const previousState = JSON.parse(savedState)
          setOpen(previousState)
          sessionStorage.removeItem('api-product-detail-previous-sidebar-state')
        } catch (error) {
          console.error('Failed to restore sidebar state:', error)
          setOpen(true) // 默认展开
        }
      }
    }
  }, []) // 空依赖数组，只在挂载和卸载时执行

  const handleBackToApiProducts = () => {
    // 先恢复侧边栏状态
    const savedState = sessionStorage.getItem('api-product-detail-previous-sidebar-state')
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
    sessionStorage.removeItem('api-product-detail-previous-sidebar-state')
    
    // 延迟导航，确保状态更新完成
    setTimeout(() => {
      router.push('/api-products')
    }, 100)
  }

  const renderContent = () => {
    switch (activeTab) {
      case "overview":
        return <ApiProductOverview apiProduct={apiProduct} />
      case "link-api":
        return <ApiProductLinkApi apiProduct={apiProduct} />
      case "api-docs":
        return <ApiProductApiDocs apiProduct={apiProduct} />
      case "usage-guide":
        return <ApiProductUsageGuide apiProduct={apiProduct} />
      case "portal":
        return <ApiProductPortal apiProduct={apiProduct} />
      case "policy":
        return <ApiProductPolicy apiProduct={apiProduct} />
      default:
        return <ApiProductOverview apiProduct={apiProduct} />
    }
  }

  // 等待初始化完成再渲染
  if (!isInitialized) {
    return <div className="flex h-full items-center justify-center">Loading...</div>
  }

  return (
    <div className="flex h-full">
      {/* API Product 详情侧边栏 */}
      <div className="w-64 border-r bg-card flex flex-col">
        {/* 返回按钮 */}
        <div className="p-4 border-b">
          <Button 
            variant="ghost" 
            className="w-full justify-start"
            onClick={handleBackToApiProducts}
          >
            <ChevronLeft className="mr-2 h-4 w-4" />
            返回 API Products
          </Button>
        </div>

        {/* API Product 信息 */}
        <div className="p-4 border-b">
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-lg font-semibold">{apiProduct.name}</h2>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon">
                  <MoreVertical className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem>编辑产品</DropdownMenuItem>
                <DropdownMenuItem>复制产品</DropdownMenuItem>
                <DropdownMenuItem>导出配置</DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem className="text-destructive">
                  删除产品
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <Badge variant="outline">{apiProduct.version}</Badge>
              <Badge variant={apiProduct.status === "published" ? "default" : "secondary"}>
                {apiProduct.status === "published" ? "已发布" : "草稿"}
              </Badge>
            </div>
            <p className="text-sm text-muted-foreground">{apiProduct.description}</p>
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