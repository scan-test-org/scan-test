import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { getToken, removeToken } from './utils'

const api: AxiosInstance = axios.create({
  baseURL: (import.meta as any).env.VITE_API_BASE_URL || 'http://47.117.149.200:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 确保跨域请求时携带 cookie
})

// 请求拦截器
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyVHlwZSI6ImFkbWluIiwidXNlcklkIjoiYWRtaW4tNjg4ODhmMmE1ODc0MGUxZjMwNmQ5OTgwIiwiaWF0IjoxNzUzNzg5Mjk3LCJleHAiOjE3NTM3OTY0OTd9.RALeWGs0uG5Bgd3bo1fPWEgXb42XAxZNMYEkq6nNXRs'
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      removeToken()
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
  },
  // 更新Portal设置
  updatePortalSettings: (portalId: string, settings: any) => {
    return api.put(`/portals/${portalId}/setting`, settings)
  }
} 