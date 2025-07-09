"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"

interface Portal {
  id: string
  name: string
  title: string
  description: string
  url: string
  userAuth: string
  rbac: string
  authStrategy: string
  apiVisibility: string
  pageVisibility: string
  logo?: string
}

interface PortalSettingsProps {
  portal: Portal
}

export function PortalSettings({ portal }: PortalSettingsProps) {
  const [activeTab, setActiveTab] = useState("general")
  const [formData, setFormData] = useState({
    name: portal.name,
    displayName: portal.title,
    description: portal.description
  })

  const tabs = [
    { key: "general", label: "General" },
    { key: "custom-domains", label: "Custom domains" },
    { key: "security", label: "Security" },
    { key: "team-mapping", label: "Team mapping" },
    { key: "audit-logs", label: "Audit logs" }
  ]

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleSave = () => {
    // 保存逻辑
    console.log("Saving settings:", formData)
  }

  const handleDiscard = () => {
    // 重置表单
    setFormData({
      name: portal.name,
      displayName: portal.title,
      description: portal.description
    })
  }

  return (
    <div className="p-6">
      {/* 页面标题 */}
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Settings</h1>
        <p className="text-muted-foreground mt-1">
          管理Portal的配置和设置
        </p>
      </div>

      {/* 标签导航 */}
      <div className="border-b mb-6">
        <nav className="flex space-x-8">
          {tabs.map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
                activeTab === tab.key
                  ? "border-primary text-primary"
                  : "border-transparent text-muted-foreground hover:text-foreground hover:border-muted-foreground"
              }`}
            >
              {tab.label}
            </button>
          ))}
        </nav>
      </div>

      {/* 内容区域 */}
      {activeTab === "general" && (
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="name">
                    Name <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    id="name"
                    value={formData.name}
                    onChange={(e) => handleInputChange("name", e.target.value)}
                    className="max-w-sm"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="displayName">Display name</Label>
                  <Input
                    id="displayName"
                    value={formData.displayName}
                    onChange={(e) => handleInputChange("displayName", e.target.value)}
                    className="max-w-sm"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  value={formData.description}
                  onChange={(e) => handleInputChange("description", e.target.value)}
                  rows={4}
                  className="max-w-2xl"
                />
              </div>

              <div className="flex gap-2 pt-4">
                <Button onClick={handleSave}>Save changes</Button>
                <Button variant="outline" onClick={handleDiscard}>
                  Discard
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {activeTab === "custom-domains" && (
        <Card>
          <CardHeader>
            <CardTitle>Custom Domains</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-muted-foreground">
              配置Portal的自定义域名设置
            </p>
          </CardContent>
        </Card>
      )}

      {activeTab === "security" && (
        <Card>
          <CardHeader>
            <CardTitle>Security Settings</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-6">
              <div>
                <Label className="text-sm font-medium">User Authentication</Label>
                <p className="text-sm text-muted-foreground mb-2">
                  Current: <Badge variant="outline">{portal.userAuth}</Badge>
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">RBAC</Label>
                <p className="text-sm text-muted-foreground mb-2">
                  Current: <Badge variant="outline">{portal.rbac}</Badge>
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">Authentication Strategy</Label>
                <p className="text-sm text-muted-foreground mb-2">
                  Current: <Badge variant="outline">{portal.authStrategy}</Badge>
                </p>
              </div>
              <div>
                <Label className="text-sm font-medium">API Visibility</Label>
                <p className="text-sm text-muted-foreground mb-2">
                  Current: <Badge variant="outline">{portal.apiVisibility}</Badge>
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {activeTab === "team-mapping" && (
        <Card>
          <CardHeader>
            <CardTitle>Team Mapping</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-muted-foreground">
              配置团队权限和角色映射
            </p>
          </CardContent>
        </Card>
      )}

      {activeTab === "audit-logs" && (
        <Card>
          <CardHeader>
            <CardTitle>Audit Logs</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-muted-foreground">
              查看Portal的操作审计日志
            </p>
          </CardContent>
        </Card>
      )}
    </div>
  )
} 