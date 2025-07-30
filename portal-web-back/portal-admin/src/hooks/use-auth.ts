"use client"

import { useState, useEffect } from "react"

interface UserInfo {
  name: string
  email: string
  avatar: string
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
      // 调用后端登录API
      const response = await fetch('/admins/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
        credentials: 'include' // 包含cookies
      })

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error('用户名或密码错误')
        } else {
          throw new Error('登录失败，请重试')
        }
      }

      // 登录成功，设置用户信息
      setUserInfo({
        name: '管理员',
        email: `${username}@portal.com`,
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin',
        role: '管理员'
      })
      setIsLoggedIn(true)
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    } finally {
      setIsLoading(false)
    }
  }

  const logout = async () => {
    try {
      // 调用后端登出API
      await fetch('/admins/logout', {
        method: 'POST',
        credentials: 'include'
      })
    } catch (error) {
      console.error('Logout failed:', error)
    } finally {
      setIsLoggedIn(false)
      setUserInfo(null)
      // 清除localStorage
      localStorage.removeItem('auth-storage')
    }
  }

  return {
    isLoggedIn,
    userInfo,
    isLoading,
    loginWithPassword,
    logout
  }
} 