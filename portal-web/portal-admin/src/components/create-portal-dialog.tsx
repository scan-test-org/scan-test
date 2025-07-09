"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

interface Portal {
  name: string
  description: string
  title: string
  url: string
  userAuth: string
  rbac: string
  authStrategy: string
  apiVisibility: string
  pageVisibility: string
  logo?: string
}

interface CreatePortalDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (portal: Omit<Portal, "id">) => void
}

export function CreatePortalDialog({
  open,
  onOpenChange,
  onSubmit,
}: CreatePortalDialogProps) {
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    title: "",
    url: "",
    userAuth: "Konnect Built-in",
    rbac: "Disabled",
    authStrategy: "key-auth",
    apiVisibility: "Private",
    pageVisibility: "Private",
    logo: ""
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit({
      ...formData,
      url: formData.url || `https://${Math.random().toString(36).substr(2, 12)}.us.kongportals.com`
    })
    setFormData({
      name: "",
      description: "",
      title: "",
      url: "",
      userAuth: "Konnect Built-in",
      rbac: "Disabled",
      authStrategy: "key-auth",
      apiVisibility: "Private",
      pageVisibility: "Private",
      logo: ""
    })
  }

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>创建新 Portal</DialogTitle>
          <DialogDescription>
            填写以下信息来创建一个新的开发者门户
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="name">Portal 名称 *</Label>
              <Input
                id="name"
                placeholder="输入Portal名称"
                value={formData.name}
                onChange={(e) => handleInputChange("name", e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="title">Portal 标题 *</Label>
              <Input
                id="title"
                placeholder="输入Portal标题"
                value={formData.title}
                onChange={(e) => handleInputChange("title", e.target.value)}
                required
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">描述</Label>
            <Textarea
              id="description"
              placeholder="输入Portal描述"
              value={formData.description}
              onChange={(e) => handleInputChange("description", e.target.value)}
              rows={3}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="url">Portal URL</Label>
            <Input
              id="url"
              placeholder="留空自动生成"
              value={formData.url}
              onChange={(e) => handleInputChange("url", e.target.value)}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="logo">Logo URL</Label>
            <Input
              id="logo"
              placeholder="输入Logo图片URL"
              value={formData.logo}
              onChange={(e) => handleInputChange("logo", e.target.value)}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="userAuth">用户认证</Label>
              <Select 
                value={formData.userAuth} 
                onValueChange={(value) => handleInputChange("userAuth", value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Konnect Built-in">Konnect Built-in</SelectItem>
                  <SelectItem value="OAuth2">OAuth2</SelectItem>
                  <SelectItem value="LDAP">LDAP</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="authStrategy">认证策略</Label>
              <Select 
                value={formData.authStrategy} 
                onValueChange={(value) => handleInputChange("authStrategy", value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="key-auth">key-auth</SelectItem>
                  <SelectItem value="oauth2">oauth2</SelectItem>
                  <SelectItem value="jwt">jwt</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label htmlFor="rbac">RBAC</Label>
              <Select 
                value={formData.rbac} 
                onValueChange={(value) => handleInputChange("rbac", value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Enabled">Enabled</SelectItem>
                  <SelectItem value="Disabled">Disabled</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="apiVisibility">API 可见性</Label>
              <Select 
                value={formData.apiVisibility} 
                onValueChange={(value) => handleInputChange("apiVisibility", value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Public">Public</SelectItem>
                  <SelectItem value="Private">Private</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="pageVisibility">页面可见性</Label>
              <Select 
                value={formData.pageVisibility} 
                onValueChange={(value) => handleInputChange("pageVisibility", value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Public">Public</SelectItem>
                  <SelectItem value="Private">Private</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              取消
            </Button>
            <Button type="submit">创建 Portal</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
} 