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
}

export function CreateConsoleDialog({
  open,
  onOpenChange,
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
          <DialogTitle>导入网关实例</DialogTitle>
          
        </DialogHeader>
        <form className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="name">网关名称 *</Label>
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

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              取消
            </Button>
            <Button type="submit">导入</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
} 