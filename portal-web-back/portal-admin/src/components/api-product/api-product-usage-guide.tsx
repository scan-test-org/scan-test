"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Textarea } from "@/components/ui/textarea"
import { BookOpen, Save, Eye, Download } from "lucide-react"

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

interface ApiProductUsageGuideProps {
  apiProduct: ApiProduct
}

export function ApiProductUsageGuide({ apiProduct }: ApiProductUsageGuideProps) {
  const mockUsageGuide = `# Payment API 使用指南

## 概述
Payment API 是一个完整的支付处理解决方案，支持多种支付方式和货币。

## 快速开始

### 1. 安装 SDK
\`\`\`bash
npm install @payment/api-sdk
\`\`\`

### 2. 初始化客户端
\`\`\`javascript
import { PaymentClient } from '@payment/api-sdk';

const client = new PaymentClient({
  apiKey: 'your-api-key',
  environment: 'production'
});
\`\`\`

### 3. 创建支付
\`\`\`javascript
const payment = await client.payments.create({
  amount: 1000,
  currency: 'USD',
  description: '订单支付',
  customer: {
    email: 'customer@example.com'
  }
});
\`\`\`

## API 参考

### 支付相关
- \`POST /payments\` - 创建支付
- \`GET /payments\` - 获取支付列表
- \`GET /payments/{id}\` - 获取支付详情

### 退款相关
- \`POST /refunds\` - 创建退款
- \`GET /refunds\` - 获取退款列表

## 错误处理
API 使用标准的 HTTP 状态码表示请求结果：
- 200: 成功
- 400: 请求参数错误
- 401: 认证失败
- 500: 服务器错误

## 最佳实践
1. 始终在生产环境中使用 HTTPS
2. 妥善保管 API 密钥
3. 实现适当的错误处理
4. 监控 API 调用频率`

  return (
    <div className="space-y-6 p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Usage Guide</h1>
          <p className="text-muted-foreground mt-1">
            使用指南编辑
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
              <BookOpen className="h-5 w-5" />
              <CardTitle>Markdown 编辑器</CardTitle>
              <Badge variant="outline">支持 Markdown</Badge>
            </div>
          </CardHeader>
          <CardContent>
            <Textarea
              value={mockUsageGuide}
              className="min-h-[500px] font-mono text-sm"
              placeholder="输入使用指南内容..."
            />
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>指南设置</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="text-sm font-medium">指南标题</label>
                <input
                  type="text"
                  defaultValue="Payment API 使用指南"
                  className="mt-1 w-full px-3 py-2 border rounded-md"
                />
              </div>
              <div>
                <label className="text-sm font-medium">版本</label>
                <input
                  type="text"
                  defaultValue="v1.2.0"
                  className="mt-1 w-full px-3 py-2 border rounded-md"
                />
              </div>
            </div>
            <div>
              <label className="text-sm font-medium">描述</label>
              <textarea
                defaultValue="Payment API 的完整使用指南，包含快速开始、API 参考和最佳实践"
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