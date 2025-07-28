import React, { useState, useEffect } from 'react'
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { HomeOutlined, GlobalOutlined, AppstoreOutlined, DesktopOutlined, UserOutlined, MenuOutlined } from '@ant-design/icons'
import { Button } from 'antd'

interface NavigationItem {
  name: string
  cn: string
  href: string
  icon: React.ComponentType<any>
}

const Layout: React.FC = () => {
  const location = useLocation()
  const navigate = useNavigate()
  const [sidebarCollapsed, setSidebarCollapsed] = useState<boolean>(false)

  useEffect(() => {
    // 进入详情页自动折叠侧边栏
    if (location.pathname.startsWith('/portals/detail') || location.pathname.startsWith('/api-products/detail')) {
      setSidebarCollapsed(true)
    } else {
      setSidebarCollapsed(false)
    }
  }, [location.pathname])

  const navigation: NavigationItem[] = [
    { name: 'Portal', cn: '门户', href: '/portals', icon: GlobalOutlined },
    { name: 'API Products', cn: 'API产品', href: '/api-products', icon: AppstoreOutlined },
    { name: '网关实例', cn: '', href: '/consoles', icon: DesktopOutlined },
  ]

  const toggleSidebar = () => {
    setSidebarCollapsed(!sidebarCollapsed)
  }

  return (
    <div className="min-h-screen bg-background">
      {/* 顶部导航栏 */}
      <header className="w-full h-16 flex items-center justify-between px-8 bg-white border-b shadow-sm">
        <div className="flex items-center space-x-2">
        <div className="bg-white">
              <Button
                type="text"
                icon={<MenuOutlined />}
                onClick={toggleSidebar}
                className="hover:bg-gray-100"
              />
            </div>
          <span className="text-2xl font-bold">API Portal</span>
        </div>
        {/* 顶部右侧用户信息或登录按钮 */}
        {true ? ( // mock: true 表示已登录，false 表示未登录
          <div className="flex items-center space-x-2">
            <UserOutlined className="mr-2 text-lg" />
            <span>admin</span>
            <button
              onClick={() => alert('已退出登录（mock）')}
              className="ml-2 px-2 py-1 rounded bg-gray-200 hover:bg-gray-300"
            >
              退出
            </button>
          </div>
        ) : (
          <button onClick={() => navigate('/login')} className="flex items-center px-4 py-2 rounded bg-black text-white hover:bg-gray-800">
            <UserOutlined className="mr-2" /> 登录
          </button>
        )}
      </header>
      <div className="flex">
        {/* 侧边栏 */}
        <aside className={`bg-white border-r min-h-screen pt-8 transition-all duration-300 ${
          sidebarCollapsed ? 'w-16' : 'w-64'
        }`}>
          <nav className="flex flex-col space-y-2 px-4">
            {navigation.map((item) => {
              const Icon = item.icon
              const isActive = location.pathname === item.href
              return (
                <Link
                  key={item.name}
                  to={item.href}
                  className={`flex items-center px-3 py-3 rounded-lg transition-colors duration-150 ${
                    isActive
                      ? 'bg-gray-100 text-black font-semibold'
                      : 'text-gray-500 hover:text-black hover:bg-gray-50'
                  }`}
                  title={sidebarCollapsed ? item.name : ''}
                >
                  <Icon className="mr-3 h-5 w-5 flex-shrink-0" />
                  {!sidebarCollapsed && (
                    <div className="flex flex-col">
                      <span className="text-base leading-none">{item.name}</span>
                    </div>
                  )}
                </Link>
              )
            })}
          </nav>
        </aside>

        {/* 主内容区域 */}
        <div className="flex-1 min-h-screen">

          <main className="p-8">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  )
}

export default Layout
