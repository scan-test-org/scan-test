"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { 
  Settings, 
  Palette, 
  Shield, 
  FileText, 
  X,
  ExternalLink 
} from "lucide-react"

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

interface PortalOverviewProps {
  portal: Portal
}

export function PortalOverview({ portal }: PortalOverviewProps) {
  return (
    <div className="p-6 space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">{portal.name}</h1>
          <p className="text-muted-foreground mt-1">Portal概览和快速设置</p>
        </div>
        <Button>
          <ExternalLink className="mr-2 h-4 w-4" />
          访问Portal
        </Button>
      </div>

      {/* 欢迎提示卡片 */}
      <Card className="border-blue-200 bg-blue-50 dark:bg-blue-950/30 dark:border-blue-900">
        <CardHeader className="pb-3">
          <div className="flex items-center justify-between">
            <CardTitle className="text-lg">Welcome to your portal overview</CardTitle>
            <Button variant="ghost" size="icon">
              <X className="h-4 w-4" />
            </Button>
          </div>
          <p className="text-sm text-muted-foreground">
            Here are some next steps to finish setting up your portal
          </p>
        </CardHeader>
        <CardContent className="grid gap-4 md:grid-cols-3">
          <Card className="border-cyan-200 bg-cyan-50 dark:bg-cyan-950/30 dark:border-cyan-900">
            <CardContent className="p-4">
              <div className="flex items-start gap-3">
                <div className="p-2 bg-cyan-100 dark:bg-cyan-900 rounded-md">
                  <Palette className="h-4 w-4 text-cyan-600" />
                </div>
                <div className="flex-1">
                  <h3 className="font-medium mb-1">Design your portal</h3>
                  <p className="text-xs text-muted-foreground">
                    Design layouts, define styles and create content for your portal.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border-purple-200 bg-purple-50 dark:bg-purple-950/30 dark:border-purple-900">
            <CardContent className="p-4">
              <div className="flex items-start gap-3">
                <div className="p-2 bg-purple-100 dark:bg-purple-900 rounded-md">
                  <Shield className="h-4 w-4 text-purple-600" />
                </div>
                <div className="flex-1">
                  <h3 className="font-medium mb-1">Configure your portal security</h3>
                  <p className="text-xs text-muted-foreground">
                    Define how you want developers to access and consume APIs on your portal.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="border-green-200 bg-green-50 dark:bg-green-950/30 dark:border-green-900">
            <CardContent className="p-4">
              <div className="flex items-start gap-3">
                <div className="p-2 bg-green-100 dark:bg-green-900 rounded-md">
                  <FileText className="h-4 w-4 text-green-600" />
                </div>
                <div className="flex-1">
                  <h3 className="font-medium mb-1">Publish more APIs</h3>
                  <p className="text-xs text-muted-foreground">
                    Empower developers access API Specification and documentation.
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </CardContent>
      </Card>

      {/* Portal 信息卡片 */}
      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Settings className="h-5 w-5" />
              Portal Information
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Portal URL</span>
              <a 
                href={portal.url} 
                target="_blank" 
                rel="noopener noreferrer"
                className="text-sm text-blue-600 hover:underline flex items-center gap-1"
              >
                {portal.url.replace('https://', '')}
                <ExternalLink className="h-3 w-3" />
              </a>
            </div>
            
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">ID</span>
                <code className="text-sm bg-muted px-2 py-1 rounded">26fe0661...</code>
              </div>

              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">User authentication</span>
                <span className="text-sm text-blue-600">{portal.userAuth}</span>
              </div>

              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">RBAC</span>
                <Badge variant="secondary">{portal.rbac}</Badge>
              </div>

              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">Default authentication strategy</span>
                <span className="text-sm text-blue-600">{portal.authStrategy}</span>
              </div>

              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">Auto approve developers</span>
                <Badge variant="secondary">Disabled</Badge>
              </div>

              <div className="flex justify-between">
                <span className="text-sm text-muted-foreground">Auto approve applications</span>
                <Badge variant="secondary">Disabled</Badge>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="space-y-6">
          {/* Published APIs 卡片 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="h-5 w-5" />
                Published APIs
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground text-sm mb-4">
                A summary of APIs published to your portal will show up here.
              </p>
              <div className="text-center py-4">
                <p className="text-2xl font-bold">0</p>
                <p className="text-xs text-muted-foreground">Published APIs</p>
              </div>
            </CardContent>
          </Card>

          {/* Developers 卡片 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <FileText className="h-5 w-5" />
                Developers
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground text-sm mb-4">
                When developers register to your portal, a summary of pending requests and approved developers will show up here.
              </p>
              <div className="text-center py-4">
                <p className="text-2xl font-bold">0</p>
                <p className="text-xs text-muted-foreground">Registered Developers</p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
} 