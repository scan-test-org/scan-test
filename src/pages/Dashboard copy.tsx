import React from 'react'
import { getToken, isAuthenticated } from '../lib/utils'

const Dashboard: React.FC = () => {
  const hasToken = isAuthenticated()
  const token = getToken()

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-lg shadow">
        <h1 className="text-2xl font-bold mb-4">仪表板</h1>
        
        {/* 认证状态测试区域 */}
        <div className="bg-gray-50 p-4 rounded-lg mb-6">
          <h2 className="text-lg font-semibold mb-3">认证状态测试</h2>
          <p className="text-xs text-gray-500 mb-3">Token 由后端通过 Set-Cookie 响应头设置</p>
          <div className="space-y-2 text-sm">
            <div className="flex items-center">
              <span className="font-medium w-24">Cookie Token:</span>
              <span className={`px-2 py-1 rounded text-xs ${hasToken ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                {hasToken ? '存在' : '不存在'}
              </span>
            </div>
            {token && (
              <div className="flex items-start">
                <span className="font-medium w-24">Token:</span>
                <span className="text-gray-600 text-xs break-all">{token}</span>
              </div>
            )}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-blue-50 p-4 rounded-lg">
            <h3 className="font-semibold text-blue-900">门户</h3>
            <p className="text-blue-700 text-sm mt-2">管理 API 门户</p>
          </div>
          <div className="bg-green-50 p-4 rounded-lg">
            <h3 className="font-semibold text-green-900">API 产品</h3>
            <p className="text-green-700 text-sm mt-2">管理 API 产品</p>
          </div>
          <div className="bg-purple-50 p-4 rounded-lg">
            <h3 className="font-semibold text-purple-900">网关实例</h3>
            <p className="text-purple-700 text-sm mt-2">管理网关实例</p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard 