import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"
import type { LinkedService } from '@/types/api-product'
 
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
} 


// Token 相关函数
export const getToken = (): string | null => {
  return localStorage.getItem('token')
}

export const removeToken = (): void => {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}

export const getStatusBadgeVariant = (status: string) => {
  return status === "PENDING" ? "orange" : status === "READY" ? "blue" : "green"
}

export const getServiceName = (linkedServiceParam: any) => {      
  if (linkedServiceParam?.apigRefConfig) {
    if ('apiName' in linkedServiceParam.apigRefConfig && linkedServiceParam.apigRefConfig.apiName) {
      return linkedServiceParam.apigRefConfig.apiName
    }
    if ('mcpServerName' in linkedServiceParam.apigRefConfig && linkedServiceParam.apigRefConfig.mcpServerName) {
      return linkedServiceParam.apigRefConfig.mcpServerName
    }
  }
  if (linkedServiceParam?.higressRefConfig) {
    return linkedServiceParam.higressRefConfig.mcpServerName
  }
  return '未知服务'
}