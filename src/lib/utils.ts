import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"
 
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
} 

// Cookie 工具函数
export const getCookie = (name: string): string | null => {
  const value = `; ${document.cookie}`
  const parts = value.split(`; ${name}=`)
  if (parts.length === 2) return parts.pop()?.split(';').shift() || null
  return null
}

export const removeCookie = (name: string): void => {
  document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;`
}

// Token 相关函数
export const getToken = (): string | null => {
  return getCookie('token')
}

export const removeToken = (): void => {
  removeCookie('token')
}

export const isAuthenticated = (): boolean => {
  return !!getToken()
}

export const getStatusBadgeVariant = (status: string) => {
  return status === "PENDING" ? "orange" : status === "READY" ? "blue" : "green"
}