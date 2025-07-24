import React, { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import api from '../lib/api'
import aliyunIcon from '../assets/aliyun.png';
import githubIcon from '../assets/github.png';
import googleIcon from '../assets/google.png';

const providerIcons: Record<string, string> = {
  aliyun: aliyunIcon,
  github: githubIcon,
  google: googleIcon,
}

interface Provider {
  provider: string;
  displayName?: string;
}

interface Identity {
  provider: string;
  displayName: string;
  rawInfoJson: string;
}

interface UserProfile {
  avatar?: string;
  name?: string;
  email?: string;
  provider?: string;
}

const parseUserProfile = (identity: Identity): UserProfile => {
  if (!identity) return {};
  let raw: any = {};
  try {
    raw = JSON.parse(identity.rawInfoJson);
  } catch {}
  // 针对不同provider做兼容
  if (identity.provider === 'github') {
    return {
      avatar: raw.avatar_url,
      name: raw.name || raw.login,
      email: raw.email,
      provider: 'github',
    };
  } else if (identity.provider === 'google') {
    return {
      avatar: raw.picture,
      name: raw.name,
      email: raw.email,
      provider: 'google',
    };
  } else if (identity.provider === 'aliyun') {
    return {
      avatar: raw.avatar,
      name: raw.name,
      email: raw.email,
      provider: 'aliyun',
    };
  }
  return {};
};

const Profile: React.FC = () => {
  const [providers, setProviders] = useState<Provider[]>([])
  const [bindingStr, setBindingStr] = useState('')
  const [identities, setIdentities] = useState<Identity[]>([])
  const location = useLocation()
  const searchParams = new URLSearchParams(location.search)
  const portalId = searchParams.get('portalId') || ''
  const token = searchParams.get('token') || ''

  useEffect(() => {
    if (!portalId) return
    api.get('/oauth2/api/oauth/providers', { params: { portalId } })
      .then((res: Provider[] | { data: Provider[] }) => {
        if (Array.isArray(res)) setProviders(res)
        else setProviders(res.data || [])
      })
      .catch(() => setProviders([]))
  }, [portalId])

  useEffect(() => {
    if (!token) return
    api.get('/oauth2/list-identities', {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res: Identity[] | { data: Identity[] }) => {
        if (Array.isArray(res)) setIdentities(res)
        else setIdentities(res.data || [])
      })
      .catch(() => setIdentities([]))
  }, [token])

  // 生成随机串
  const randomStr = () => Math.random().toString(36).slice(2, 10)

  // 拼接BINDING串
  const handleBinding = (provider: string) => {
    const rand = randomStr()
    const str = `BINDING|${rand}|${portalId}|${provider}|${token}`
    setBindingStr(str)
    // 跳转到OIDC授权接口
    const state = encodeURIComponent(str)
    const url = `${api.defaults.baseURL}/oauth2/api/oauth/authorize?portalId=${portalId}&provider=${provider}&state=${state}&frontendRedirectUrl=http://${window.location.host}`
    window.location.href = url
  }

  // 判断provider是否已绑定
  const isBound = (provider: string) =>
    identities.some((id) => id.provider === provider)

  // 取第一个已绑定身份展示个人信息
  const mainIdentity = identities[0];
  const userProfile = mainIdentity ? parseUserProfile(mainIdentity) : null;

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-white">
      <div className="bg-white p-8 rounded-xl shadow-2xl w-full max-w-md flex flex-col items-center border border-gray-100">
        <h2 className="text-2xl font-bold mb-6 text-gray-900 text-center">个人中心</h2>
        {/* 个人信息展示区 */}
        {userProfile && (
          <div className="flex flex-col items-center mb-8">
            {userProfile.avatar && <img src={userProfile.avatar} alt="avatar" className="w-16 h-16 rounded-full mb-2" />}
            <div className="text-lg font-semibold text-gray-900">{userProfile.name}</div>
            {userProfile.email && <div className="text-gray-500 text-sm">{userProfile.email}</div>}
            {userProfile.provider && <div className="text-gray-400 text-xs mt-1">来自 {userProfile.provider} 账号</div>}
          </div>
        )}
        <div className="w-full flex flex-col gap-3 mb-6">
          {providers.length === 0 ? (
            <div className="text-gray-400 text-center">暂无可用第三方</div>
          ) : (
            providers.map((provider) => {
              const bound = isBound(provider.provider)
              const icon = providerIcons[provider.provider] || ''
              return (
                <div
                  key={provider.provider}
                  className={`w-full flex items-center gap-2 py-2 rounded border text-base font-medium shadow-sm px-3 ${bound ? 'bg-green-50 border-green-200 text-green-700' : 'bg-white border-gray-200 text-gray-900'}`}
                >
                  {icon && <img src={icon} alt={provider.provider} className="w-6 h-6" />}
                  <span className="flex-1">{provider.displayName || provider.provider}</span>
                  {bound ? (
                    <span className="px-2 py-1 rounded bg-green-100 text-green-700 text-xs font-semibold">已绑定</span>
                  ) : (
                    <button
                      onClick={() => handleBinding(provider.provider)}
                      className="px-3 py-1 rounded bg-blue-500 text-white hover:bg-blue-600 text-xs font-semibold transition-colors"
                    >
                      绑定
                    </button>
                  )}
                </div>
              )
            })
          )}
        </div>
        {bindingStr && (
          <div className="w-full break-all bg-gray-100 p-4 rounded text-gray-800 text-sm border border-gray-200">
            {bindingStr}
          </div>
        )}
      </div>
    </div>
  )
}

export default Profile 