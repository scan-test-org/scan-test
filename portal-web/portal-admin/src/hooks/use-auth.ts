"use client"

import { useState, useEffect } from 'react'

interface UserInfo {
  name: string
  email: string
  avatar?: string
  role: string
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

  const loginWithPassword = async (username: string, password: string) => {
    setIsLoading(true)
    
    try {
      // 模拟密码验证
      await new Promise(resolve => setTimeout(resolve, 800))
      
      // 简单的模拟验证逻辑
      if (username === 'admin' && password === 'admin123') {
        setUserInfo({
          name: '管理员',
          email: 'admin@portal.com',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
          role: '管理员'
        })
        setIsLoggedIn(true)
      } else if (username === 'user' && password === 'user123') {
        setUserInfo({
          name: '用户',
          email: 'user@portal.com',
          avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=user',
          role: '普通用户'
        })
        setIsLoggedIn(true)
      } else {
        throw new Error('用户名或密码错误')
      }
    } catch (error) {
      console.error('Login failed:', error)
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
    loginWithPassword,
    logout
  }
} 