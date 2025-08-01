import axios, { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { getToken, removeToken } from './utils'


const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 确保跨域请求时携带 cookie
})

// 请求拦截器
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
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
    if (error.response?.status === 403 || error.response?.status === 401) {
      removeToken()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api

// Portal相关API
export const portalApi = {
  // 获取portal列表
  getPortals: () => {
    return api.get('/api/v1/portals')
  },
  // 获取portal详情
  getPortalDetail: (portalId: string) => {
    return api.get(`/api/v1/portals/${portalId}`)
  },
  // 绑定域名
  bindDomain: (portalId: string, domainData: { domain: string; protocol: string; type: string }) => {
    return api.post(`/api/v1/portals/${portalId}/domains`, domainData)
  },
  // 解绑域名
  unbindDomain: (portalId: string, domain: string) => {
    const encodedDomain = encodeURIComponent(domain)
    return api.delete(`/api/v1/portals/${portalId}/domains/${encodedDomain}`)
  },
  // 更新Portal
  updatePortal: (portalId: string, data: any) => {
    return api.put(`/api/v1/portals/${portalId}`, data)
  },
  // 更新Portal设置
  updatePortalSettings: (portalId: string, settings: any) => {
    return api.put(`/api/v1/portals/${portalId}/setting`, settings)
  },
  // 获取Portal的开发者列表
  getDeveloperList: (portalId: string, pagination?: { page: number; size: number }) => {
    return api.get(`/api/v1/developers`, {
      params: {
        portalId,
        ...pagination
      }
    })
  },
  // 更新开发者状态
  updateDeveloperStatus: (portalId: string, developerId: string, status: string) => {
    return api.post(`/api/v1/developers/${developerId}/status`, {
      portalId,
      status
    })
  },
  deleteDeveloper: (developerId: string) => {
    return api.delete(`/api/v1/admin/${developerId}`)
  },
  getConsumerList: (portalId: string, developerId: string, pagination?: { page: number; size: number }) => {
    return api.get(`/api/v1/consumers`, {
      params: {
        portalId,
        developerId,
        ...pagination
      }
    })
  },
  // 审批consumer
  approveConsumer: (consumerId: string) => {
    return api.patch(`/api/v1/consumers/${consumerId}/status`)
  }
}

// API Product相关API
export const apiProductApi = {
  // 获取API产品列表
  getApiProducts: (params?: any) => {
    return api.get('/api/v1/products', { params })
  },
  // 获取API产品详情
  getApiProductDetail: (productId: string) => {
    return api.get(`/api/v1/products/${productId}`)
  },
  // 创建API产品
  createApiProduct: (data: any) => {
    return api.post('/api/v1/products', data)
  },
  // 删除API产品
  deleteApiProduct: (productId: string) => {
    return api.delete(`/api/v1/products/${productId}`)
  },
  // 获取API产品关联的服务
  getApiProductRef: (productId: string) => {
    return api.get(`/api/v1/products/${productId}/ref`)
  },
  // 创建API产品关联
  createApiProductRef: (productId: string, data: any) => {
    return api.post(`/api/v1/products/${productId}/ref`, data)
  },
  // 删除API产品关联
  deleteApiProductRef: (productId: string) => {
    return api.delete(`/api/v1/products/${productId}/ref`)
  },
  // 获取API产品已发布的门户列表
  getApiProductPublications: (productId: string) => {
    return api.get(`/api/v1/products/${productId}/publications`)
  },
  // 发布API产品到门户
  publishToPortal: (productId: string, portalId: string) => {
    return api.post(`/api/v1/products/${productId}/publications/${portalId}`)
  },
  // 取消发布API产品到门户
  cancelPublishToPortal: (productId: string, portalId: string) => {
    return api.delete(`/api/v1/products/${productId}/publications/${portalId}`)
  }
}

// Gateway相关API
export const gatewayApi = {
  // 获取网关列表
  getGateways: () => {
    return api.get('/api/v1/gateways')
  },
  // 创建APIG网关
  createApigGateway: (data: any) => {
    return api.post('/api/v1/gateways/apig', { ...data, gatewayType: 'APIG_API' })
  },
  // 获取网关的REST API列表
  getGatewayRestApis: (gatewayId: string) => {
    return api.get(`/api/v1/gateways/${gatewayId}/rest-apis`)
  },
  // 获取网关的MCP Server列表
  getGatewayMcpServers: (gatewayId: string) => {
    return api.get(`/api/v1/gateways/${gatewayId}/mcp-servers`)
  }
} 