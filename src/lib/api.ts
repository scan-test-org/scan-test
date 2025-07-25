import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { getTokenFromCookie } from './utils';
import { message } from 'antd';

const api: AxiosInstance = axios.create({
  baseURL: (import.meta as any).env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getTokenFromCookie();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response.data;
  },
  (error) => {
    const status = error.response?.status;
    switch (status) {
      case 401:
        message.error('未登录或登录已过期，请重新登录');
        window.location.href = '/login';
        break;
      case 403:
        message.error('无权限访问该资源');
        break;
      case 404:
        message.error('请求的资源不存在');
        break;
      case 500:
        message.error('服务器异常，请稍后再试');
        break;
      default:
        message.error(error.response?.data?.message || '请求发生错误');
    }
    return Promise.reject(error);
  }
)

export default api 