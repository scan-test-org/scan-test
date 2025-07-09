"use client"

import { useState, useEffect } from 'react'

interface UserInfo {
  name: string
  email: string
  avatar?: string
  role: string
  provider?: string
}

export function useAuth() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null)
  const [isLoading, setIsLoading] = useState(false)

  // 从localStorage恢复登录状态
  useEffect(() => {
    const savedAuth = localStorage.getItem('auth-storage')
    if (savedAuth) {
      try {
        const { isLoggedIn, userInfo } = JSON.parse(savedAuth)
        setIsLoggedIn(isLoggedIn)
        setUserInfo(userInfo)
      } catch (error) {
        console.error('Failed to parse auth storage:', error)
      }
    }
  }, [])

  // 保存状态到localStorage
  useEffect(() => {
    localStorage.setItem('auth-storage', JSON.stringify({
      isLoggedIn,
      userInfo
    }))
  }, [isLoggedIn, userInfo])

  const loginWithOAuth = async (provider: 'github' | 'aliyun', userData: UserInfo) => {
    setIsLoading(true)
    
    try {
      // 模拟OAuth验证和用户信息获取
      await new Promise(resolve => setTimeout(resolve, 500))
      
      setUserInfo({
        ...userData,
        provider
      })
      setIsLoggedIn(true)
    } catch (error) {
      console.error('OAuth login failed:', error)
      throw error
    } finally {
      setIsLoading(false)
    }
  }

  const logout = () => {
    setIsLoggedIn(false)
    setUserInfo(null)
    // 清除localStorage
    localStorage.removeItem('auth-storage')
  }

  return {
    isLoggedIn,
    userInfo,
    isLoading,
    loginWithOAuth,
    logout
  }
} 