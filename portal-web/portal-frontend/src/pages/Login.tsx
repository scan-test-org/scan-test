import React, { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import api from '../lib/api'

const Login: React.FC = () => {
  const [providers, setProviders] = useState<{ provider: string; displayName?: string }[]>([])
  const location = useLocation()
  // 从 query 获取 portalId
  const searchParams = new URLSearchParams(location.search)
  const portalId = searchParams.get('portalId') || ''

  // 获取 OIDC provider
  useEffect(() => {
    if (!portalId) return
    api.get('/oauth2/api/oauth/providers', { params: { portalId } })
      .then((data: any) => setProviders(data))
      .catch(() => setProviders([]))
  }, [portalId])

  // 跳转到 OIDC 授权
  const handleOidcLogin = (provider: string) => {
    const stateRaw = `LOGIN|${portalId}|${provider}`
    const state = encodeURIComponent(stateRaw)
    window.location.href = `${api.defaults.baseURL}/oauth2/api/oauth/authorize?portalId=${portalId}&provider=${provider}&state=${state}`
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50">
      <div className="bg-white p-8 rounded shadow-md w-96 flex flex-col items-center">
        <h2 className="text-2xl font-bold mb-6">登录 API Portal</h2>
        {providers.length === 0 ? (
          <div className="text-gray-400">暂无可用登录方式</div>
        ) : (
          providers.map((provider) => (
            <button
              key={provider.provider}
              onClick={() => handleOidcLogin(provider.provider)}
              className="w-full mb-4 px-4 py-2 rounded bg-black text-white text-center hover:bg-gray-800 transition-colors"
            >
              使用{provider.displayName || provider.provider}登录
            </button>
          ))
        )}
      </div>
    </div>
  )
}

export default Login 