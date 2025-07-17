"use client"

import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Package, Plus } from "lucide-react"

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

interface PortalPublishedApisProps {
  portal: Portal
}

export function PortalPublishedApis({ portal }: PortalPublishedApisProps) {
  return (
    <div className="p-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold">Published APIs</h1>
          <p className="text-muted-foreground mt-1">
            管理发布到Portal的API列表
          </p>
        </div>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Publish API
        </Button>
      </div>

      {/* 空状态 */}
      <Card className="border-2 border-dashed">
        <CardContent className="flex flex-col items-center justify-center py-16">
          <div className="w-16 h-16 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center mb-4">
            <Package className="h-8 w-8 text-blue-600" />
          </div>
          
          <h2 className="text-xl font-semibold mb-2">Publish your first API</h2>
          
          <p className="text-muted-foreground text-center max-w-md mb-6">
            Publish an API to your dev portal to give developers easy access to your services, 
            documentation, and version details.
          </p>
          
          <Button size="lg">
            <Plus className="mr-2 h-4 w-4" />
            Publish API
          </Button>
        </CardContent>
      </Card>
    </div>
  )
} 