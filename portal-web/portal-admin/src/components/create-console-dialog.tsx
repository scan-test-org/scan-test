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

interface Console {
  name: string
  description: string
  type: "web" | "mobile" | "desktop"
  status: "active" | "inactive" | "maintenance"
  endpoint: string
  version: string
  environment: "production" | "staging" | "development"
}

interface CreateConsoleDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (console: Omit<Console, "id" | "lastUpdated">) => void
}

export function CreateConsoleDialog({
  open,
  onOpenChange,
  onSubmit,
}: CreateConsoleDialogProps) {
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    type: "web" as "web" | "mobile" | "desktop",
    status: "active" as "active" | "inactive" | "maintenance",
    endpoint: "",
    version: "1.0.0",
    environment: "development" as "production" | "staging" | "development"
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit(formData)
    setFormData({
      name: "",
      description: "",
      type: "web",
      status: "active",
      endpoint: "",
      version: "1.0.0",
      environment: "development"
    })
  }

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>创建新 Console</DialogTitle>
          <DialogDescription>
            填写以下信息来创建一个新的控制台实例
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="name">Console 名称 *</Label>
              <Input
                id="name"
                placeholder="输入Console名称"
                value={formData.name}
                onChange={(e) => handleInputChange("name", e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="version">版本 *</Label>
              <Input
                id="version"
                placeholder="如: 1.0.0"
                value={formData.version}
                onChange={(e) => handleInputChange("version", e.target.value)}
                required
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">描述</Label>
            <Textarea
              id="description"
              placeholder="输入Console描述"
              value={formData.description}
              onChange={(e) => handleInputChange("description", e.target.value)}
              rows={3}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="endpoint">端点 URL *</Label>
            <Input
              id="endpoint"
              placeholder="https://example.com"
              value={formData.endpoint}
              onChange={(e) => handleInputChange("endpoint", e.target.value)}
              required
            />
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label htmlFor="type">类型</Label>
              <Select 
                value={formData.type} 
                onValueChange={(value) => handleInputChange("type", value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="web">Web</SelectItem>
                  <SelectItem value="mobile">Mobile</SelectItem>
                  <SelectItem value="desktop">Desktop</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="status">状态</Label>
              <Select 
                value={formData.status} 
                onValueChange={(value) => handleInputChange("status", value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="active">活跃</SelectItem>
                  <SelectItem value="inactive">非活跃</SelectItem>
                  <SelectItem value="maintenance">维护中</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="environment">环境</Label>
              <Select 
                value={formData.environment} 
                onValueChange={(value) => handleInputChange("environment", value)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="development">Development</SelectItem>
                  <SelectItem value="staging">Staging</SelectItem>
                  <SelectItem value="production">Production</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              取消
            </Button>
            <Button type="submit">创建 Console</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
} 