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
import { Badge } from "@/components/ui/badge"
import { X } from "lucide-react"

// 预定义的分类选项
const CATEGORIES = [
  "Testing",
  "Finance", 
  "Authentication",
  "Communication",
  "Analytics",
  "E-commerce",
  "Social",
  "Utility",
  "Healthcare",
  "Education",
  "Other"
]

interface ApiProduct {
  name: string
  description: string
  type: "restApi" | "mcpServer"
  category: string
  labels: string[]
}

interface CreateApiProductDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (product: Omit<ApiProduct, "id">) => void
}

export function CreateApiProductDialog({
  open,
  onOpenChange,
  onSubmit,
}: CreateApiProductDialogProps) {
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    type: "restApi" as "restApi" | "mcpServer",
    category: "",
    labels: [] as string[]
  })
  const [newLabel, setNewLabel] = useState("")

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!formData.name.trim() || !formData.category) {
      return
    }
    onSubmit(formData)
    setFormData({
      name: "",
      description: "",
      type: "restApi",
      category: "",
      labels: []
    })
    setNewLabel("")
  }

  const isFormValid = formData.name.trim() && formData.category

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleTypeChange = (value: string) => {
    setFormData(prev => ({ ...prev, type: value as "restApi" | "mcpServer" }))
  }

  const addLabel = () => {
    if (newLabel.trim() && !formData.labels.includes(newLabel.trim())) {
      setFormData(prev => ({
        ...prev,
        labels: [...prev.labels, newLabel.trim()]
      }))
      setNewLabel("")
    }
  }

  const removeLabel = (labelToRemove: string) => {
    setFormData(prev => ({
      ...prev,
      labels: prev.labels.filter(label => label !== labelToRemove)
    }))
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      e.preventDefault()
      addLabel()
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>创建新 API Product</DialogTitle>
          <DialogDescription>
            填写以下信息来创建一个新的 API 产品
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">产品名称 *</Label>
            <Input
              id="name"
              placeholder="输入API Product名称"
              value={formData.name}
              onChange={(e) => handleInputChange("name", e.target.value)}
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="type">产品类型 *</Label>
            <Select value={formData.type} onValueChange={handleTypeChange}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="restApi">REST API</SelectItem>
                <SelectItem value="mcpServer">MCP Server</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="category">分类 *</Label>
            <Select 
              value={formData.category} 
              onValueChange={(value) => handleInputChange("category", value)}
            >
              <SelectTrigger>
                <SelectValue placeholder="选择分类" />
              </SelectTrigger>
              <SelectContent>
                {CATEGORIES.map((category) => (
                  <SelectItem key={category} value={category}>
                    {category}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">描述</Label>
            <Textarea
              id="description"
              placeholder="输入API Product描述"
              value={formData.description}
              onChange={(e) => handleInputChange("description", e.target.value)}
              rows={3}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="labels">标签</Label>
            <div className="flex gap-2">
              <Input
                id="labels"
                placeholder="输入标签后按回车添加"
                value={newLabel}
                onChange={(e) => setNewLabel(e.target.value)}
                onKeyPress={handleKeyPress}
              />
              <Button type="button" onClick={addLabel} variant="outline">
                添加
              </Button>
            </div>
            {formData.labels.length > 0 && (
              <div className="flex flex-wrap gap-2 mt-2">
                {formData.labels.map((label, index) => (
                  <Badge key={index} variant="secondary" className="flex items-center gap-1">
                    {label}
                    <X
                      className="h-3 w-3 cursor-pointer"
                      onClick={() => removeLabel(label)}
                    />
                  </Badge>
                ))}
              </div>
            )}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              取消
            </Button>
            <Button type="submit" disabled={!isFormValid}>
              创建 API Product
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
} 