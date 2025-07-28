import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

const api: AxiosInstance = axios.create({
  baseURL: (import.meta as any).env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 获取cookie中的token
const getTokenFromCookie = (): string | null => {
  const cookies = document.cookie.split(';')
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split('=')
    if (name === 'token') {
      return value
    }
  }
  return null
}

// 请求拦截器
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getTokenFromCookie()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 删除cookie中的token
const removeTokenFromCookie = (): void => {
  document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;'
}

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      removeTokenFromCookie()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api

// Portal相关API
export const portalApi = {
  // 获取portal详情
  getPortalDetail: (portalId: string) => {
    return api.get(`/portals/${portalId}`)
  },
  // 绑定域名
  bindDomain: (portalId: string, domainData: { domain: string; protocol: string; type: string }) => {
    return api.post(`/portals/${portalId}/domains`, domainData)
  },
  // 解绑域名
  unbindDomain: (portalId: string, domain: string) => {
    const encodedDomain = encodeURIComponent(domain)
    return api.delete(`/portals/${portalId}/domains/${encodedDomain}`)
  }
} 