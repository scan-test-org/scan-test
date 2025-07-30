import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"
 
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