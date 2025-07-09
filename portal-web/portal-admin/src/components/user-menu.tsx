"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { User, Settings, LogOut, Shield } from "lucide-react"
import { LoginDialog } from "@/components/login-dialog"
import { useAuth } from "@/hooks/use-auth"

export function UserMenu() {
  const [showLoginDialog, setShowLoginDialog] = useState(false)
  const { isLoggedIn, userInfo, loginWithPassword, logout } = useAuth()

  const handlePasswordLogin = async (username: string, password: string) => {
    try {
      await loginWithPassword(username, password)
      setShowLoginDialog(false)
    } catch (error) {
      console.error('Login failed:', error)
      throw error // 让LoginDialog处理错误显示
    }
  }

  if (!isLoggedIn) {
    return (
      <>
        <Button onClick={() => setShowLoginDialog(true)}>
          <User className="mr-2 h-4 w-4" />
          登录
        </Button>
        <LoginDialog
          open={showLoginDialog}
          onOpenChange={setShowLoginDialog}
          onLogin={handlePasswordLogin}
        />
      </>
    )
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="relative h-8 w-8 rounded-full">
          <Avatar className="h-8 w-8">
            <AvatarImage src={userInfo?.avatar} alt={userInfo?.name} />
            <AvatarFallback>
              {userInfo?.name.charAt(0).toUpperCase()}
            </AvatarFallback>
          </Avatar>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56" align="end" forceMount>
        <div className="flex flex-col space-y-1 p-2">
          <p className="text-sm font-medium leading-none">{userInfo?.name}</p>
          <p className="text-xs leading-none text-muted-foreground">
            {userInfo?.email}
          </p>
          <div className="flex items-center justify-between mt-1">
            <div className="flex items-center gap-1">
              <Shield className="h-3 w-3 text-muted-foreground" />
              <span className="text-xs text-muted-foreground">{userInfo?.role}</span>
            </div>
          </div>
        </div>
        <DropdownMenuSeparator />
        <DropdownMenuItem>
          <User className="mr-2 h-4 w-4" />
          <span>个人资料</span>
        </DropdownMenuItem>
        <DropdownMenuItem>
          <Settings className="mr-2 h-4 w-4" />
          <span>设置</span>
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem onClick={logout}>
          <LogOut className="mr-2 h-4 w-4" />
          <span>退出登录</span>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
} 