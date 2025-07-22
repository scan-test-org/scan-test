import { Card, Button, Input, Space, Tag, Divider } from 'antd'
import { SaveOutlined, EyeOutlined, EditOutlined } from '@ant-design/icons'
import { useState } from 'react'

const { TextArea } = Input

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

const mockApiDocs = `# Payment API Documentation

## Overview
This API provides payment processing capabilities for e-commerce applications.

## Authentication
All API requests require authentication using API keys.

## Endpoints

### POST /payments
Create a new payment transaction.

**Request Body:**
\`\`\`json
{
  "amount": 100.00,
  "currency": "USD",
  "payment_method": "card",
  "description": "Payment for order #123"
}
\`\`\`

**Response:**
\`\`\`json
{
  "id": "pay_123456789",
  "status": "succeeded",
  "amount": 100.00,
  "currency": "USD",
  "created_at": "2025-01-08T10:30:00Z"
}
\`\`\`

### GET /payments/{id}
Retrieve payment details by ID.

**Response:**
\`\`\`json
{
  "id": "pay_123456789",
  "status": "succeeded",
  "amount": 100.00,
  "currency": "USD",
  "created_at": "2025-01-08T10:30:00Z"
}
\`\`\`

## Error Codes
- 400: Bad Request
- 401: Unauthorized
- 404: Payment Not Found
- 500: Internal Server Error
`

export function ApiProductApiDocs({ apiProduct }: ApiProductApiDocsProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [content, setContent] = useState(mockApiDocs)

  const handleSave = () => {
    setIsEditing(false)
    // 这里可以调用API保存文档
  }

  const handlePreview = () => {
    // 这里可以打开预览模式
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold mb-2">API文档</h1>
          <p className="text-gray-600">编辑和发布API文档</p>
        </div>
        <Space>
          <Button icon={<EyeOutlined />} onClick={handlePreview}>
            预览
          </Button>
          {isEditing ? (
            <Button type="primary" icon={<SaveOutlined />} onClick={handleSave}>
              保存
            </Button>
          ) : (
            <Button icon={<EditOutlined />} onClick={() => setIsEditing(true)}>
              编辑
            </Button>
          )}
        </Space>
      </div>

      <Card>
        <div className="mb-4">
          <Space>
            <Tag color="blue">Markdown</Tag>
            <Tag color="green">已发布</Tag>
            <span className="text-gray-500">最后更新: 2025-01-08 10:30:00</span>
          </Space>
        </div>

        {isEditing ? (
          <div>
            <TextArea
              value={content}
              onChange={(e) => setContent(e.target.value)}
              rows={20}
              placeholder="请输入API文档内容..."
              className="font-mono"
            />
            <div className="mt-4 text-sm text-gray-500">
              支持Markdown格式，可以使用代码块、表格、链接等语法
            </div>
          </div>
        ) : (
          <div className="prose max-w-none">
            <pre className="bg-gray-100 p-4 rounded overflow-x-auto">
              <code>{content}</code>
            </pre>
          </div>
        )}
      </Card>

      <Card title="文档设置">
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <span>文档可见性</span>
            <Tag color="green">公开</Tag>
          </div>
          <div className="flex justify-between items-center">
            <span>版本控制</span>
            <Tag color="blue">启用</Tag>
          </div>
          <div className="flex justify-between items-center">
            <span>自动生成</span>
            <Tag color="orange">禁用</Tag>
          </div>
        </div>
      </Card>
    </div>
  )
} 