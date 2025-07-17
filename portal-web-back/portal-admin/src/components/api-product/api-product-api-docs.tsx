"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Textarea } from "@/components/ui/textarea"
import { FileText, Save, Eye, Download } from "lucide-react"

interface ApiProduct {
  id: string
  name: string
  description: string
  version: string
  status: string
  visibility: string
  createdAt: string
  updatedAt: string
  portals: number
  linkedServices: number
  policies: number
}

interface ApiProductApiDocsProps {
  apiProduct: ApiProduct
}

export function ApiProductApiDocs({ apiProduct }: ApiProductApiDocsProps) {
  const mockApiDocs = {
    openapi: "3.0.0",
    info: {
      title: "Payment API",
      description: "支付处理API，提供完整的支付解决方案",
      version: "v1.2.0"
    },
    paths: {
      "/payments": {
        "get": {
          "summary": "获取支付列表",
          "description": "获取所有支付记录",
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
            }
          ],
          "responses": {
            "200": {
              "description": "成功",
              "content": {
                "application/json": {
                  "schema": {
                    "type": "object",
                    "properties": {
                      "data": {
                        "type": "array",
                        "items": {
                          "$ref": "#/components/schemas/Payment"
                        }
                      },
                      "total": {
                        "type": "integer"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    components: {
      schemas: {
        Payment: {
          type: "object",
          properties: {
            id: {
              type: "string"
            },
            amount: {
              type: "number"
            },
            currency: {
              type: "string"
            },
            status: {
              type: "string",
              enum: ["pending", "completed", "failed"]
            }
          }
        }
      }
    }
  }

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">API Docs</h1>
          <p className="text-muted-foreground mt-1">
            API文档编辑
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline">
            <Eye className="mr-2 h-4 w-4" />
            预览
          </Button>
          <Button variant="outline">
            <Download className="mr-2 h-4 w-4" />
            导出
          </Button>
          <Button>
            <Save className="mr-2 h-4 w-4" />
            保存
          </Button>
        </div>
      </div>

      <div className="grid gap-6">
        <Card>
          <CardHeader>
            <div className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              <CardTitle>OpenAPI 规范</CardTitle>
              <Badge variant="outline">v3.0.0</Badge>
            </div>
          </CardHeader>
          <CardContent>
            <Textarea
              value={JSON.stringify(mockApiDocs, null, 2)}
              className="min-h-[400px] font-mono text-sm"
              placeholder="输入OpenAPI规范..."
            />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>文档设置</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium">文档标题</label>
                <input
                  type="text"
                  defaultValue={mockApiDocs.info.title}
                  className="mt-1 w-full px-3 py-2 border rounded-md"
                />
              </div>
              <div>
                <label className="text-sm font-medium">版本</label>
                <input
                  type="text"
                  defaultValue={mockApiDocs.info.version}
                  className="mt-1 w-full px-3 py-2 border rounded-md"
                />
              </div>
            </div>
            <div>
              <label className="text-sm font-medium">描述</label>
              <textarea
                defaultValue={mockApiDocs.info.description}
                className="mt-1 w-full px-3 py-2 border rounded-md"
                rows={3}
              />
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
} 