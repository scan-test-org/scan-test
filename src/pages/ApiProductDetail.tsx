import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Button, Badge, Dropdown, MenuProps } from 'antd'
import {
  MoreOutlined,
  LeftOutlined
} from '@ant-design/icons'
import { ApiProductOverview } from '@/components/api-product/ApiProductOverview'
import { ApiProductLinkApi } from '@/components/api-product/ApiProductLinkApi'
import { ApiProductApiDocs } from '@/components/api-product/ApiProductApiDocs'
import { ApiProductUsageGuide } from '@/components/api-product/ApiProductUsageGuide'
import { ApiProductPortal } from '@/components/api-product/ApiProductPortal'
import { apiProductApi } from '@/lib/api';
import type { ApiProduct } from '@/types/api-product';


const menuItems = [
  {
    key: "overview",
    label: "Overview",
    description: "基本信息和统计"
  },
  {
    key: "link-api",
    label: "Link API",
    description: "关联的网关服务"
  },
  {
    key: "api-docs",
    label: "API Docs",
    description: "API文档编辑"
  },
  {
    key: "usage-guide",
    label: "Usage Guide",
    description: "使用指南编辑"
  },
  {
    key: "portal",
    label: "Portal",
    description: "发布的门户"
  }
]

export default function ApiProductDetail() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const [apiProduct, setApiProduct] = useState<ApiProduct>({} as ApiProduct)
  
  // 从URL query参数获取当前tab，默认为overview
  const currentTab = searchParams.get('tab') || 'overview'
  // 验证tab值是否有效，如果无效则使用默认值
  const validTab = menuItems.some(item => item.key === currentTab) ? currentTab : 'overview'
  const [activeTab, setActiveTab] = useState(validTab)

  useEffect(() => {
    const productId = searchParams.get('productId')
    if (productId) {
      apiProductApi.getApiProductDetail(productId).then((res: any) => {
        setApiProduct(res.data)
      })
    }
  }, [])

  // 同步URL参数和activeTab状态
  useEffect(() => {
    setActiveTab(validTab)
  }, [validTab, searchParams])

  const handleBackToApiProducts = () => {
    navigate('/api-products')
  }

  const handleTabChange = (tabKey: string) => {
    setActiveTab(tabKey)
    // 更新URL query参数
    const newSearchParams = new URLSearchParams(searchParams)
    newSearchParams.set('tab', tabKey)
    setSearchParams(newSearchParams)
  }

  const renderContent = () => {
    switch (activeTab) {
      case "overview":
        return <ApiProductOverview apiProduct={apiProduct} />
      case "link-api":
        return <ApiProductLinkApi apiProduct={apiProduct} handleRefresh={() => {}} />
      case "api-docs":
        return <ApiProductApiDocs apiProduct={apiProduct} />
      case "usage-guide":
        return <ApiProductUsageGuide apiProduct={apiProduct} />
      case "portal":
        return <ApiProductPortal apiProduct={apiProduct} />
      default:
        return <ApiProductOverview apiProduct={apiProduct} />
    }
  }

  const dropdownItems: MenuProps['items'] = [
    {
      key: 'edit',
      label: '编辑产品',
    },
    {
      key: 'copy',
      label: '复制产品',
    },
    {
      key: 'export',
      label: '导出配置',
    },
    {
      type: 'divider',
    },
    {
      key: 'delete',
      label: '删除产品',
      danger: true,
    },
  ]

  return (
    <div className="flex h-full">
      {/* API Product 详情侧边栏 */}
      <div className="w-64 border-r bg-white flex flex-col">
        {/* 返回按钮 */}
        <div className="pb-4 border-b">
          <Button
            type="text"
            // className="w-full justify-start"
            onClick={handleBackToApiProducts}
            icon={<LeftOutlined />}
          >
            返回 API Products
          </Button>
        </div>

        {/* API Product 信息 */}
        <div className="p-4 border-b">
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-lg font-semibold">{apiProduct.name}</h2>
            <Dropdown menu={{ items: dropdownItems }} trigger={['click']}>
              <Button type="text" icon={<MoreOutlined />} />
            </Dropdown>
          </div>
          <div className="space-y-2">
            {/* <div className="flex items-center gap-2">
              <Badge>{apiProduct.version}</Badge>
              <Badge status={apiProduct.status === "published" ? "success" : "default"}>
                {apiProduct.status === "published" ? "已发布" : "草稿"}
              </Badge>
            </div> */}
            <p className="text-sm text-gray-500">{apiProduct.description}</p>
          </div>
        </div>

        {/* 导航菜单 */}
        <nav className="flex-1 p-4 space-y-1">
          {menuItems.map((item) => (
            <button
              key={item.key}
              onClick={() => handleTabChange(item.key)}
              className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-left transition-colors ${
                activeTab === item.key
                  ? "bg-blue-500 text-white"
                  : "hover:bg-gray-100"
              }`}
            >
              {/*<item.icon className="h-4 w-4" />*/}
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
