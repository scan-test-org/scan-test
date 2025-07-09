"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { 
  FileText, 
  Edit, 
  Eye, 
  Save, 
  Download, 
  Upload,
  RefreshCw
} from "lucide-react"

const mockApiDocumentation = `{
  "openapi": "3.0.0",
  "info": {
    "title": "Payment API",
    "description": "支付处理API，提供完整的支付解决方案",
    "version": "v1.2.0",
    "contact": {
      "name": "API Support",
      "email": "api-support@example.com"
    }
  },
  "servers": [
    {
      "url": "https://api.example.com/v1",
      "description": "Production server"
    },
    {
      "url": "https://staging-api.example.com/v1", 
      "description": "Staging server"
    }
  ],
  "paths": {
    "/payments": {
      "get": {
        "summary": "获取支付列表",
        "description": "获取用户的支付记录列表",
        "parameters": [
          {
            "name": "page",
            "in": "query",
            "description": "页码",
            "required": false,
            "schema": {
              "type": "integer",
              "default": 1
            }
          },
          {
            "name": "limit",
            "in": "query", 
            "description": "每页数量",
            "required": false,
            "schema": {
              "type": "integer",
              "default": 20
            }
          }
        ],
        "responses": {
          "200": {
            "description": "成功获取支付列表",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/PaymentList"
                }
              }
            }
          },
          "401": {
            "description": "认证失败"
          },
          "500": {
            "description": "服务器内部错误"
          }
        }
      },
      "post": {
        "summary": "创建支付",
        "description": "创建一个新的支付请求",
        "requestBody": {
          "required": true,
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/CreatePaymentRequest"
              }
            }
          }
        },
        "responses": {
          "201": {
            "description": "支付创建成功",
            "content": {
              "application/json": {
                "schema": {
                  "$ref": "#/components/schemas/Payment"
                }
              }
            }
          },
          "400": {
            "description": "请求参数错误"
          },
          "401": {
            "description": "认证失败"
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "Payment": {
        "type": "object",
        "properties": {
          "id": {
            "type": "string",
            "description": "支付ID"
          },
          "amount": {
            "type": "number",
            "description": "支付金额"
          },
          "currency": {
            "type": "string",
            "description": "货币类型"
          },
          "status": {
            "type": "string",
            "enum": ["pending", "completed", "failed"],
            "description": "支付状态"
          },
          "created_at": {
            "type": "string",
            "format": "date-time",
            "description": "创建时间"
          }
        }
      }
    }
  }
}`

export default function ApiDocsPage() {
  const [isEditing, setIsEditing] = useState(false)
  const [documentation, setDocumentation] = useState(mockApiDocumentation)
  const [unsavedChanges, setUnsavedChanges] = useState(false)

  const handleEdit = () => {
    setIsEditing(true)
  }

  const handleSave = () => {
    // 这里实际会调用API保存文档
    console.log("Saving documentation:", documentation)
    setIsEditing(false)
    setUnsavedChanges(false)
  }

  const handleCancel = () => {
    // 重置到原始内容
    setDocumentation(mockApiDocumentation)
    setIsEditing(false)
    setUnsavedChanges(false)
  }

  const handleDocumentationChange = (value: string) => {
    setDocumentation(value)
    setUnsavedChanges(true)
  }

  const handleExport = () => {
    const blob = new Blob([documentation], { type: 'application/json' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'api-documentation.json'
    a.click()
    URL.revokeObjectURL(url)
  }

  const handleImport = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        const content = e.target?.result as string
        setDocumentation(content)
        setUnsavedChanges(true)
      }
      reader.readAsText(file)
    }
  }

  const formatJson = () => {
    try {
      const parsed = JSON.parse(documentation)
      const formatted = JSON.stringify(parsed, null, 2)
      setDocumentation(formatted)
      setUnsavedChanges(true)
    } catch (error) {
      console.error("Invalid JSON format:", error)
    }
  }

  return (
    <div className="space-y-6 p-6">
      {/* 页面标题和操作 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">API 文档</h1>
          <p className="text-muted-foreground mt-1">
            编辑和管理 API 的 OpenAPI 规范文档
          </p>
        </div>
        <div className="flex items-center gap-2">
          {unsavedChanges && (
            <Badge variant="outline" className="text-orange-600">
              未保存
            </Badge>
          )}
          
          {!isEditing ? (
            <>
              <Button variant="outline" onClick={handleExport}>
                <Download className="mr-2 h-4 w-4" />
                导出
              </Button>
              <input
                type="file"
                accept=".json,.yaml,.yml"
                onChange={handleImport}
                className="hidden"
                id="import-file"
              />
              <Button variant="outline" onClick={() => document.getElementById('import-file')?.click()}>
                <Upload className="mr-2 h-4 w-4" />
                导入
              </Button>
              <Button onClick={handleEdit}>
                <Edit className="mr-2 h-4 w-4" />
                编辑文档
              </Button>
            </>
          ) : (
            <>
              <Button variant="outline" onClick={formatJson}>
                <RefreshCw className="mr-2 h-4 w-4" />
                格式化
              </Button>
              <Button variant="outline" onClick={handleCancel}>
                取消
              </Button>
              <Button onClick={handleSave}>
                <Save className="mr-2 h-4 w-4" />
                保存
              </Button>
            </>
          )}
        </div>
      </div>

      {/* 文档统计 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">API 端点</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">2</div>
            <p className="text-xs text-muted-foreground">个 API 端点</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">HTTP 方法</CardTitle>
            <Eye className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">2</div>
            <p className="text-xs text-muted-foreground">种请求方法</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">数据模型</CardTitle>
            <FileText className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">1</div>
            <p className="text-xs text-muted-foreground">个数据模型</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">文档状态</CardTitle>
            <Badge variant="default" className="h-4 w-4 p-0"></Badge>
          </CardHeader>
          <CardContent>
            <div className="text-sm font-bold text-green-600">已发布</div>
            <p className="text-xs text-muted-foreground">OpenAPI 3.0</p>
          </CardContent>
        </Card>
      </div>

      {/* 文档编辑器 */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              OpenAPI 规范
            </CardTitle>
            <div className="flex items-center gap-2">
              {isEditing ? (
                <Badge variant="outline">编辑模式</Badge>
              ) : (
                <Badge variant="secondary">预览模式</Badge>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isEditing ? (
            <div className="space-y-4">
              <div className="text-sm text-muted-foreground">
                编辑 OpenAPI 3.0 规范文档（JSON 格式）
              </div>
              <Textarea
                value={documentation}
                onChange={(e) => handleDocumentationChange(e.target.value)}
                className="min-h-[600px] font-mono text-sm"
                placeholder="请输入 OpenAPI 规范..."
              />
            </div>
          ) : (
            <div className="space-y-4">
              <div className="text-sm text-muted-foreground">
                当前 API 文档的 OpenAPI 规范
              </div>
              <pre className="bg-muted p-4 rounded-lg text-sm overflow-auto max-h-[600px] whitespace-pre-wrap">
                {documentation}
              </pre>
            </div>
          )}
        </CardContent>
      </Card>

      {/* 快速操作 */}
      <Card>
        <CardHeader>
          <CardTitle>快速操作</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Button variant="outline" className="justify-start">
              <FileText className="mr-2 h-4 w-4" />
              生成 SDK
            </Button>
            <Button variant="outline" className="justify-start">
              <Eye className="mr-2 h-4 w-4" />
              预览文档
            </Button>
            <Button variant="outline" className="justify-start">
              <RefreshCw className="mr-2 h-4 w-4" />
              验证规范
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
} 