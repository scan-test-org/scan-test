"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Github, Cloud, Lock, ExternalLink } from "lucide-react"

interface LoginDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onLogin: (provider: 'github' | 'aliyun', userInfo: any) => void
}

export function LoginDialog({ open, onOpenChange, onLogin }: LoginDialogProps) {
  const [isLoading, setIsLoading] = useState<string | null>(null)

  const handleOAuthLogin = async (provider: 'github' | 'aliyun') => {
    setIsLoading(provider)
    
    try {
      // 模拟OAuth登录流程
      await new Promise(resolve => setTimeout(resolve, 1500))
      
      // 模拟不同提供商的用户信息
      const mockUserData = {
        github: {
          name: "GitHub User",
          email: "user@github.com",
          avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=github",
          role: "开发者",
          provider: "GitHub"
        },
        aliyun: {
          name: "阿里云用户",
          email: "user@aliyun.com", 
          avatar: "https://api.dicebear.com/7.x/avataaars/svg?seed=aliyun",
          role: "管理员",
          provider: "阿里云"
        }
      }
      
      onLogin(provider, mockUserData[provider])
    } catch (error) {
      console.error(`${provider} login failed:`, error)
    } finally {
      setIsLoading(null)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[400px]">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Lock className="h-5 w-5" />
            登录到 API Portal
          </DialogTitle>
          <DialogDescription>
            选择您的账号提供商以登录管理系统
          </DialogDescription>
        </DialogHeader>
        
        <div className="space-y-4 py-4">
          <Button
            variant="outline"
            className="w-full h-12 flex items-center gap-3"
            onClick={() => handleOAuthLogin('github')}
            disabled={isLoading !== null}
          >
            {isLoading === 'github' ? (
              <div className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin" />
            ) : (
              <Github className="h-5 w-5" />
            )}
            <span className="flex-1 text-left">
              {isLoading === 'github' ? '正在登录...' : '使用 GitHub 登录'}
            </span>
            <ExternalLink className="h-4 w-4 text-muted-foreground" />
          </Button>

          <Button
            variant="outline"
            className="w-full h-12 flex items-center gap-3"
            onClick={() => handleOAuthLogin('aliyun')}
            disabled={isLoading !== null}
          >
            {isLoading === 'aliyun' ? (
              <div className="w-5 h-5 border-2 border-current border-t-transparent rounded-full animate-spin" />
            ) : (
              <Cloud className="h-5 w-5 text-blue-600" />
            )}
            <span className="flex-1 text-left">
              {isLoading === 'aliyun' ? '正在登录...' : '使用阿里云登录'}
            </span>
            <ExternalLink className="h-4 w-4 text-muted-foreground" />
          </Button>

          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <span className="w-full border-t" />
            </div>
            <div className="relative flex justify-center text-xs uppercase">
              <span className="bg-background px-2 text-muted-foreground">
                安全登录
              </span>
            </div>
          </div>

          <div className="text-center space-y-2">
            <p className="text-xs text-muted-foreground">
              点击登录即表示您同意我们的服务条款和隐私政策
            </p>
            <div className="flex items-center justify-center gap-4 text-xs text-muted-foreground">
              <span className="flex items-center gap-1">
                <Lock className="h-3 w-3" />
                安全加密
              </span>
              <span>•</span>
              <span>零密码存储</span>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
} 