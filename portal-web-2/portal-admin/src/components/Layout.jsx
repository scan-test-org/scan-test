import React, { useState, useEffect } from 'react'
import { Outlet, Link, useLocation } from 'react-router-dom'
import { HomeOutlined, GlobalOutlined, AppstoreOutlined, DesktopOutlined, UserOutlined } from '@ant-design/icons';

const Layout = () => {
  const location = useLocation()
  const [sidebarVisible, setSidebarVisible] = useState(true)

  useEffect(() => {
    // 进入详情页自动收起侧边栏
    if (location.pathname.startsWith('/portals/detail')) {
      setSidebarVisible(false)
    } else {
      setSidebarVisible(true)
    }
  }, [location.pathname])

  const navigation = [
    { name: 'Portal', cn: '门户', href: '/portals', icon: GlobalOutlined },
    { name: 'API Products', cn: 'API产品', href: '/api-products', icon: AppstoreOutlined },
    { name: '网关实例', cn: '', href: '/consoles', icon: DesktopOutlined },
  ]

  return (
    <div className="min-h-screen bg-background">
      {/* 顶部导航栏 */}
      <header className="w-full h-16 flex items-center justify-between px-8 bg-white border-b shadow-sm">
        <div className="flex items-center space-x-2">
          <span className="text-2xl font-bold">API Portal</span>
        </div>
        <button className="flex items-center px-4 py-2 rounded bg-black text-white hover:bg-gray-800">
          <UserOutlined className="mr-2" /> 登录
        </button>
      </header>
      <div className="flex">
        {/* 侧边栏 */}
        {sidebarVisible && (
          <aside className="w-64 bg-white border-r min-h-screen pt-8">
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
                  >
                    <Icon className="mr-3 h-5 w-5" />
                    <div className="flex flex-col">
                      <span className="text-base leading-none">{item.name}</span>
                      {/* {item.cn && <span className="text-xs text-gray-400 leading-none">{item.cn}</span>} */}
                    </div>
                  </Link>
                )
              })}
            </nav>
          </aside>
        )}
        {/* 主内容区域 */}
        <div className={`flex-1 bg-gray-50 min-h-screen ${sidebarVisible ? '' : 'w-full'}`}>
          <main className="p-8">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  )
}

export default Layout 