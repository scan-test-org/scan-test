"use client"

import { useState } from "react"
import { useParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Navigation } from "@/components/navigation"
import { Footer } from "@/components/footer"

import { 
  FileText, 
  BookOpen, 
  ArrowLeft,
  Globe,
  Code,
  Plus,
  Trash2,
  Users
} from "lucide-react"
import Link from "next/link"
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeHighlight from 'rehype-highlight'
import 'highlight.js/styles/github.css'

// 模拟Consumer数据
const mockConsumers = [
  {
    id: "consumer-1",
    name: "Web App",
    description: "主要的Web应用程序",
    type: "web",
    status: "active",
    createdAt: "2025-01-01"
  },
  {
    id: "consumer-2", 
    name: "Mobile App",
    description: "iOS和Android移动应用",
    type: "mobile",
    status: "active",
    createdAt: "2025-01-02"
  },
  {
    id: "consumer-3",
    name: "Analytics Service",
    description: "数据分析和统计服务",
    type: "service",
    status: "active", 
    createdAt: "2025-01-03"
  },
  {
    id: "consumer-4",
    name: "Partner API",
    description: "合作伙伴集成接口",
    type: "api",
    status: "inactive",
    createdAt: "2025-01-04"
  }
]

// 模拟绑定数据
const mockBindings = {
  "1": [
    {
      id: "binding-1",
      consumerId: "consumer-1",
      apiId: "1",
      bindingDate: "2025-01-05",
      permissions: ["read", "write"],
      rateLimit: "1000/hour"
    },
    {
      id: "binding-2", 
      consumerId: "consumer-2",
      apiId: "1",
      bindingDate: "2025-01-06",
      permissions: ["read"],
      rateLimit: "500/hour"
    }
  ]
}

// 模拟API数据
const mockApiData = {
  "1": {
    id: "1",
    name: "test",
    title: "Test API",
    description: "A comprehensive test API for demonstration purposes",
    version: "v1.0.0",
    category: "Testing",
    status: "available", // available, deprecated, maintenance
    baseUrl: "https://api.example.com/v1",
    documentation: `{
  "openapi": "3.0.0",
  "info": {
    "title": "Test API",
    "description": "A comprehensive test API for demonstration purposes",
    "version": "v1.0.0"
  },
  "servers": [
    {
      "url": "https://api.example.com/v1",
      "description": "Production server"
    }
  ],
  "paths": {
    "/test": {
      "get": {
        "summary": "Get test data",
        "description": "Retrieve test data from the API",
        "responses": {
          "200": {
            "description": "Successful response",
            "content": {
              "application/json": {
                "schema": {
                  "type": "object",
                  "properties": {
                    "message": {
                      "type": "string",
                      "example": "Hello World"
                    },
                    "timestamp": {
                      "type": "string",
                      "format": "date-time"
                    }
                  }
                }
              }
            }
          }
        }
      },
      "post": {
        "summary": "Create test data",
        "description": "Submit new test data to the API",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "type": "object",
                "properties": {
                  "name": {
                    "type": "string",
                    "example": "Test Item"
                  },
                  "value": {
                    "type": "string",
                    "example": "Test Value"
                  }
                }
              }
            }
          }
        },
        "responses": {
          "201": {
            "description": "Created successfully"
          }
        }
      }
    }
  }
}`,
    usageGuide: `# Test API 使用指南

## 概述

Test API 是一个用于演示的综合测试API，提供了基本的CRUD操作功能。

## 快速开始

### 1. 获取API访问权限

首先，您需要注册账户并获取API密钥：

1. 访问开发者控制台
2. 创建新的应用
3. 获取API密钥

### 2. 基础配置

在您的应用中配置API基础信息：

\`\`\`javascript
const config = {
  baseUrl: 'https://api.example.com/v1',
  apiKey: 'your_api_key_here'
}
\`\`\`

## 认证方式

API使用API Key认证方式。在每个请求的Header中添加：

\`\`\`
X-API-Key: your_api_key_here
\`\`\`

## 基本用法

### 获取测试数据

\`\`\`javascript
const response = await fetch('https://api.example.com/v1/test', {
  method: 'GET',
  headers: {
    'X-API-Key': 'your_api_key_here',
    'Content-Type': 'application/json'
  }
})

const data = await response.json()
console.log(data)
\`\`\`

### 创建测试数据

\`\`\`javascript
const response = await fetch('https://api.example.com/v1/test', {
  method: 'POST',
  headers: {
    'X-API-Key': 'your_api_key_here',
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: 'Test Item',
    value: 'Test Value'
  })
})

const result = await response.json()
console.log(result)
\`\`\`

## 错误处理

API返回标准的HTTP状态码：

- **200** - 请求成功
- **201** - 资源创建成功
- **400** - 请求参数错误
- **401** - 认证失败
- **404** - 资源未找到
- **500** - 服务器内部错误

### 错误响应格式

\`\`\`json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "请求参数不正确",
    "details": "具体错误描述"
  }
}
\`\`\`

## 速率限制

- 每分钟最多100个请求
- 每小时最多1000个请求
- 超出限制将返回429状态码

## 支持

如果您在使用过程中遇到问题，可以通过以下方式获得帮助：

- 📧 邮箱: support@example.com
- 📞 电话: +86 400-123-4567
- 💬 在线客服: [开发者控制台](https://developer.example.com)

---

*文档最后更新: 2025年1月8日*`
  },
  // 可以添加更多API数据...
}

export default function ApiDetailPage() {
  const params = useParams()
  const [activeTab, setActiveTab] = useState("documentation")
  const [showConsumerDialog, setShowConsumerDialog] = useState(false)
  const [bindings, setBindings] = useState(mockBindings)
  
  const apiId = params.id as string
  const apiData = mockApiData[apiId as keyof typeof mockApiData]
  const apiBindings = bindings[apiId as keyof typeof bindings] || []
  
  if (!apiData) {
    return (
      <div className="min-h-screen bg-white">
        <Navigation />
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="text-center">
            <h1 className="text-4xl font-bold text-gray-900 mb-4">API Not Found</h1>
            <p className="text-xl text-gray-600 mb-8">The requested API could not be found.</p>
            <Link href="/apis">
              <Button>
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to APIs
              </Button>
            </Link>
          </div>
        </main>
        <Footer />
      </div>
    )
  }

  // 切换绑定状态
  const toggleBinding = (consumerId: string) => {
    const isCurrentlyBound = apiBindings.some(binding => binding.consumerId === consumerId)
    
    if (isCurrentlyBound) {
      // 解绑
      setBindings(prev => ({
        ...prev,
        [apiId]: (prev[apiId as keyof typeof prev] || []).filter(binding => binding.consumerId !== consumerId)
      }))
    } else {
      // 绑定
      const newBinding = {
        id: `binding-${Date.now()}`,
        consumerId: consumerId,
        apiId: apiId,
        bindingDate: new Date().toISOString().split('T')[0],
        permissions: ["read"],
        rateLimit: "1000/hour"
      }
      
      setBindings(prev => ({
        ...prev,
        [apiId]: [...(prev[apiId as keyof typeof prev] || []), newBinding]
      }))
    }
  }

  // 获取API状态样式
  const getStatusBadge = (status: string) => {
    switch (status) {
      case "available":
        return <Badge className="bg-green-100 text-green-800 hover:bg-green-100">可用</Badge>
      case "deprecated":
        return <Badge className="bg-orange-100 text-orange-800 hover:bg-orange-100">已弃用</Badge>
      case "maintenance":
        return <Badge className="bg-yellow-100 text-yellow-800 hover:bg-yellow-100">维护中</Badge>
      default:
        return <Badge variant="secondary">未知</Badge>
    }
  }

  const menuItems = [
    {
      key: "documentation",
      label: "API Documentation", 
      icon: FileText,
      description: "Complete API reference"
    },
    {
      key: "usage-guide",
      label: "Usage Guide",
      icon: BookOpen,
      description: "Getting started guide"
    }
  ]

  const renderContent = () => {
    switch (activeTab) {
      case "documentation":
        return (
          <div className="space-y-6">
            <div className="prose prose-lg max-w-none">
              <h2>API Documentation</h2>
              <p>Complete OpenAPI 3.0 specification for the {apiData.name} API.</p>
            </div>
            <Card>
              <CardHeader>
                <CardTitle>OpenAPI Specification</CardTitle>
              </CardHeader>
              <CardContent>
                <pre className="bg-gray-50 p-4 rounded-lg text-sm overflow-auto max-h-96 whitespace-pre-wrap">
                  {apiData.documentation}
                </pre>
              </CardContent>
            </Card>
            
            {/* 快速信息 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <Card>
                <CardHeader className="flex flex-row items-center space-y-0 pb-2">
                  <Globe className="h-4 w-4 text-muted-foreground mr-2" />
                  <CardTitle className="text-sm font-medium">Base URL</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-sm font-mono bg-gray-100 p-2 rounded">
                    {apiData.baseUrl}
                  </div>
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader className="flex flex-row items-center space-y-0 pb-2">
                  <Code className="h-4 w-4 text-muted-foreground mr-2" />
                  <CardTitle className="text-sm font-medium">Version</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-sm font-mono bg-gray-100 p-2 rounded">
                    v{apiData.version}
                  </div>
                </CardContent>
              </Card>

            </div>
          </div>
        )
      case "usage-guide":
        return (
          <div className="space-y-6">
            <div className="prose prose-lg max-w-none">
              <h2>Usage Guide</h2>
              <p>Learn how to integrate and use the {apiData.name} API in your applications.</p>
            </div>
            <Card>
              <CardContent className="p-6">
                <div className="prose prose-slate max-w-none">
                  <ReactMarkdown
                    remarkPlugins={[remarkGfm]}
                    rehypePlugins={[rehypeHighlight]}
                    components={{
                      // 自定义代码块样式
                      code: ({ className, children, ...props }) => {
                        const match = /language-(\w+)/.exec(className || '')
                        return match ? (
                          <code className={className} {...props}>
                            {children}
                          </code>
                        ) : (
                          <code className="bg-gray-100 px-1 py-0.5 rounded text-sm font-mono" {...props}>
                            {children}
                          </code>
                        )
                      },
                      // 自定义表格样式
                      table: ({ children }) => (
                        <div className="overflow-x-auto">
                          <table className="min-w-full divide-y divide-gray-200">
                            {children}
                          </table>
                        </div>
                      ),
                      // 自定义链接样式
                      a: ({ children, href }) => (
                        <a 
                          href={href} 
                          target="_blank" 
                          rel="noopener noreferrer"
                          className="text-blue-600 hover:text-blue-800 underline"
                        >
                          {children}
                        </a>
                      )
                    }}
                  >
                    {apiData.usageGuide}
                  </ReactMarkdown>
                </div>
              </CardContent>
            </Card>
          </div>
        )
      default:
        return null
    }
  }

  return (
    <div className="min-h-screen bg-white">
      <Navigation />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 返回按钮 */}
        <div className="mb-6">
          <Link href="/apis">
            <Button variant="ghost" className="mb-4">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to APIs
            </Button>
          </Link>
        </div>

        {/* API标题信息 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <h1 className="text-4xl font-bold text-gray-900">{apiData.title}</h1>
              <Badge variant="secondary">{apiData.category}</Badge>
              <Badge variant="outline">v{apiData.version}</Badge>
              {getStatusBadge(apiData.status)}
            </div>
            <Button onClick={() => setShowConsumerDialog(true)}>
              <Users className="mr-2 h-4 w-4" />
              管理绑定 ({apiBindings.length})
            </Button>
          </div>
          <p className="text-xl text-gray-600 mb-4">{apiData.description}</p>
        </div>

        <div className="flex gap-8">
          {/* 左侧导航 */}
          <div className="w-64 flex-shrink-0">
            <div className="sticky top-8">
              <nav className="space-y-2">
                {menuItems.map((item) => (
                  <button
                    key={item.key}
                    onClick={() => setActiveTab(item.key)}
                    className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg text-left transition-colors ${
                      activeTab === item.key
                        ? "bg-blue-50 text-blue-700 border border-blue-200"
                        : "hover:bg-gray-50 text-gray-700"
                    }`}
                  >
                    <item.icon className="h-5 w-5" />
                    <div>
                      <div className="font-medium">{item.label}</div>
                      <div className="text-xs opacity-75">{item.description}</div>
                    </div>
                  </button>
                ))}
              </nav>
            </div>
          </div>

          {/* 右侧内容 */}
          <div className="flex-1 min-w-0">
            {renderContent()}
          </div>
        </div>
      </main>
      
      <Footer />

      {/* Consumer管理对话框 */}
      <Dialog open={showConsumerDialog} onOpenChange={setShowConsumerDialog}>
        <DialogContent className="sm:max-w-[800px]">
          <DialogHeader>
            <DialogTitle>管理Consumer绑定</DialogTitle>
            <DialogDescription>
              查看和管理所有Consumer对此API的访问权限
            </DialogDescription>
          </DialogHeader>
          
          <div className="space-y-4">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Consumer名称</TableHead>
                  <TableHead>类型</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead>绑定状态</TableHead>
                  <TableHead>绑定日期</TableHead>
                  <TableHead>操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {mockConsumers.map((consumer) => {
                  const binding = apiBindings.find(b => b.consumerId === consumer.id)
                  const isBound = !!binding
                  
                  return (
                    <TableRow key={consumer.id}>
                      <TableCell>
                        <div>
                          <div className="font-medium">{consumer.name}</div>
                          <div className="text-sm text-gray-500">{consumer.description}</div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline">{consumer.type}</Badge>
                      </TableCell>
                      <TableCell>
                        <Badge 
                          variant={consumer.status === "active" ? "default" : "secondary"}
                          className={consumer.status === "active" ? "bg-green-100 text-green-800" : ""}
                        >
                          {consumer.status === "active" ? "活跃" : "非活跃"}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge 
                          variant={isBound ? "default" : "outline"}
                          className={isBound ? "bg-blue-100 text-blue-800" : ""}
                        >
                          {isBound ? "已绑定" : "未绑定"}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        {binding ? binding.bindingDate : "-"}
                      </TableCell>
                      <TableCell>
                        <Button
                          variant={isBound ? "destructive" : "default"}
                          size="sm"
                          onClick={() => toggleBinding(consumer.id)}
                          disabled={consumer.status !== "active"}
                        >
                          {isBound ? (
                            <>
                              <Trash2 className="mr-1 h-3 w-3" />
                              解绑
                            </>
                          ) : (
                            <>
                              <Plus className="mr-1 h-3 w-3" />
                              绑定
                            </>
                          )}
                        </Button>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </div>

          <DialogFooter>
            <Button onClick={() => setShowConsumerDialog(false)}>
              关闭
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
} 