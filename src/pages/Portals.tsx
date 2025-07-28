import { useState, useCallback, memo, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, Badge, Avatar, Dropdown, Space } from 'antd'
import { PlusOutlined, MoreOutlined, LinkOutlined } from '@ant-design/icons'
import type { MenuProps } from 'antd'
import api from '../lib/api'

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


// 优化的Portal卡片组件
const PortalCard = memo(({ portal, onNavigate }: {
  portal: Portal;
  onNavigate: (id: string) => void;
}) => {
  const handleCardClick = useCallback(() => {
    onNavigate(portal.id)
  }, [portal.id, onNavigate])

  const handleLinkClick = useCallback((e: React.MouseEvent) => {
    e.stopPropagation()
  }, [])

  const dropdownItems: MenuProps['items'] = [
    {
      key: 'edit',
      label: '编辑',
    },
    {
      key: 'copy',
      label: '复制',
    },
    {
      type: 'divider',
    },
    {
      key: 'delete',
      label: '删除',
      danger: true,
    },
  ]

  return (
    <Card
      className="cursor-pointer hover:shadow-lg transition-all duration-200 hover:scale-[1.02] border-2 hover:border-blue-200"
      onClick={handleCardClick}
      bodyStyle={{ padding: '16px' }}
    >
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-3">
          <Avatar size={40} className="bg-blue-500">
            {portal.title.charAt(0).toUpperCase()}
          </Avatar>
          <div className="ml-2">
            <h3 className="text-lg font-semibold">{portal.title}</h3>
          </div>
        </div>
        <Dropdown menu={{ items: dropdownItems }} trigger={['click']}>
          <Button type="text" icon={<MoreOutlined />} onClick={(e) => e.stopPropagation()} />
        </Dropdown>
      </div>

      <div className="space-y-4">
        <div className="space-y-2">
          <div className="text-sm font-medium">{portal.name}</div>
          <div className="flex items-center space-x-2 text-sm text-gray-500">
            <LinkOutlined className="h-3 w-3" />
            <a
              href={portal.url || 'http://localhost:5173?portalId=test_portal'}
              target="_blank"
              rel="noopener noreferrer"
              className="hover:underline text-blue-600"
              onClick={handleLinkClick}
            >
              {portal.url || 'http://localhost:5173'}
            </a>
          </div>
        </div>

        <div className="space-y-2">
          <div className="flex justify-between text-sm">
            <span className="text-gray-500">User authentication</span>
            <span className="text-blue-600">{portal.userAuth}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-gray-500">RBAC</span>
            <Badge status="default" text={portal.rbac} />
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-gray-500">Authentication strategy</span>
            <span className="text-blue-600">{portal.authStrategy}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-gray-500">Default API visibility</span>
            <Badge status="processing" text={portal.apiVisibility} />
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-gray-500">Default page visibility</span>
            <Badge status="processing" text={portal.pageVisibility} />
          </div>
        </div>

        <div className="text-xs text-gray-400 text-center pt-2 border-t">
          点击查看详情
        </div>
      </div>
    </Card>
  )
})

PortalCard.displayName = 'PortalCard'

export default function Portals() {
  const navigate = useNavigate()
  const [portals, setPortals] = useState<Portal[]>([])
  const [loading, setLoading] = useState<boolean>(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    api.get('/portals')
      .then((res: any) => {
        // 兼容后端返回结构
        const list = res?.data?.content || []
        const portals: Portal[] = list.map((item: any) => ({
          id: item.portalId,
          name: item.name,
          description: item.description,
          title: item.name,
          url: '', // 如有url字段可补充
          userAuth: item.portalSettingConfig?.builtinAuthEnabled ? '内置认证' : (item.portalSettingConfig?.oidcAuthEnabled ? 'OIDC' : '未知'),
          rbac: item.portalSettingConfig?.rbacEnabled ? 'Enabled' : 'Disabled',
          authStrategy: item.portalSettingConfig?.authStrategy || '-',
          apiVisibility: item.portalSettingConfig?.apiVisibility || '-',
          pageVisibility: item.portalSettingConfig?.pageVisibility || '-',
          logo: item.portalUiConfig?.logo || undefined,
        }))
        setPortals(portals)
      })
      .catch((err: any) => {
        setError(err?.message || '加载失败')
      })
      .finally(() => setLoading(false))
  }, [])

  const handleCreatePortal = useCallback((newPortal: Omit<Portal, "id">) => {
    const portal: Portal = {
      ...newPortal,
      id: Date.now().toString()
    }
    setPortals([...portals, portal])
  }, [portals])

  const handlePortalClick = useCallback((portalId: string) => {
    navigate(`/portals/detail?id=${portalId}`)
  }, [navigate])

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Portal</h1>
          <p className="text-gray-500 mt-2">
            管理和配置您的开发者门户
          </p>
        </div>
        <Button type="primary" icon={<PlusOutlined />}>
          创建 Portal
        </Button>
      </div>
      {loading && <div>加载中...</div>}
      {error && <div className="text-red-500">{error}</div>}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {portals.map((portal) => (
          <PortalCard
            key={portal.id}
            portal={portal}
            onNavigate={handlePortalClick}
          />
        ))}
      </div>
    </div>
  )
}
