import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Button, Dropdown, MenuProps, Typography, Spin } from 'antd'
import { 
  LinkOutlined,
  MoreOutlined,
  LeftOutlined,
  GlobalOutlined,
  FileTextOutlined,
  TeamOutlined,
  UserOutlined,
  SettingOutlined
} from '@ant-design/icons'
import { PortalOverview } from '@/components/portal/PortalOverview'
import { PortalPublishedApis } from '@/components/portal/PortalPublishedApis'
import { PortalDevelopers } from '@/components/portal/PortalDevelopers'
import { PortalConsumers } from '@/components/portal/PortalConsumers'
import { PortalSettings } from '@/components/portal/PortalSettings'
import { portalApi } from '@/lib/api'
import { Portal } from '@/types'

const { Title, Paragraph } = Typography

// 移除mockPortal，使用真实API数据

const menuItems = [
  {
    key: "overview",
    label: "Overview",
    icon: GlobalOutlined,
    description: "Portal概览和快速设置"
  },
  {
    key: "published-apis",
    label: "Published APIs",
    icon: FileTextOutlined,
    description: "已发布的API管理"
  },
  {
    key: "developers",
    label: "Developers",
    icon: TeamOutlined,
    description: "开发者管理"
  },
  {
    key: "consumers",
    label: "Consumers",
    icon: UserOutlined,
    description: "消费者管理"
  },
  {
    key: "settings",
    label: "Settings",
    icon: SettingOutlined,
    description: "门户设置"
  }
]

export default function PortalDetail() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [activeTab, setActiveTab] = useState("overview")
  const [portal, setPortal] = useState<Portal | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchPortalData = async () => {
    try {
      setLoading(true)
      const portalId = searchParams.get('id') || 'portal-6882e06f4fd0c963020e3485'
      const response = await portalApi.getPortalDetail(portalId)
      if (response && response.code === 'SUCCESS') {
        setPortal(response.data)
      } else {
        setError(response?.message || '获取Portal信息失败')
      }
    } catch (err) {
      console.error('获取Portal信息失败:', err)
      setError('获取Portal信息失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchPortalData()
  }, [searchParams])

  const handleBackToPortals = () => {
    navigate('/portals')
  }

  const renderContent = () => {
    if (!portal) return null
    
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
        return <PortalSettings portal={portal} onRefresh={fetchPortalData} />
      default:
        return <PortalOverview portal={portal} />
    }
  }

  const dropdownItems: MenuProps['items'] = [
    {
      key: 'edit',
      label: '编辑Portal',
    },
    {
      key: 'copy',
      label: '复制Portal',
    },
    {
      type: 'divider',
    },
    {
      key: 'delete',
      label: '删除Portal',
      danger: true,
    },
  ]

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spin size="large" />
      </div>
    )
  }

  if (error || !portal) {
    return (
      <div className="flex h-full items-center justify-center">
        <div className="text-center">
          <p className="text-red-500 mb-4">{error || 'Portal信息不存在'}</p>
          <Button onClick={() => navigate('/portals')}>返回Portal列表</Button>
        </div>
      </div>
    )
  }

  return (
    <div className="flex h-full">
      {/* Portal详情侧边栏 */}
      <div className="w-64 border-r bg-white flex flex-col">
        {/* 返回按钮 */}
        <div className="pb-4 border-b">
          <Button 
            type="text"
            // className="w-full justify-start text-gray-600 hover:text-gray-900"
            onClick={handleBackToPortals}
            icon={<LeftOutlined />}
          >
            返回 Portal 列表
          </Button>
        </div>

        {/* Portal 信息 */}
        <div className="p-4 border-b">
          <div className="flex items-center justify-between mb-3">
            <Title level={5} className="mb-0">{portal.title}</Title>
            <Dropdown menu={{ items: dropdownItems }} trigger={['click']}>
              <Button type="text" icon={<MoreOutlined />} size="small" />
            </Dropdown>
          </div>
          <Paragraph className="text-gray-600 mb-3">{portal.name}</Paragraph>
          <div className="flex items-center gap-2 text-sm text-gray-500">
            <LinkOutlined className="h-3 w-3" />
            <a 
              href={`http://${portal.portalDomainConfig[0]?.domain}`} 
              target="_blank" 
              rel="noopener noreferrer"
              className="hover:underline truncate text-blue-600"
            >
              {portal.portalDomainConfig[0]?.domain}
            </a>
          </div>
        </div>

        {/* 导航菜单 */}
        <nav className="flex-1 p-4 space-y-2">
          {menuItems.map((item) => {
            const Icon = item.icon
            return (
              <button
                key={item.key}
                onClick={() => setActiveTab(item.key)}
                className={`w-full flex items-center gap-3 px-3 py-3 rounded-lg text-left transition-colors ${
                  activeTab === item.key
                    ? "bg-blue-50 text-blue-600 border border-blue-200"
                    : "hover:bg-gray-50 text-gray-700"
                }`}
              >
                <Icon className="h-4 w-4 flex-shrink-0" />
                <div className="flex-1">
                  <div className="font-medium text-sm">{item.label}</div>
                  <div className="text-xs text-gray-500 mt-1">{item.description}</div>
                </div>
              </button>
            )
          })}
        </nav>
      </div>

      {/* 主内容区域 */}
      <div className="flex-1 overflow-auto">
      {renderContent()}
      </div>
    </div>
  )
} 