import { Card, Button, Input, Space, Tag } from 'antd'
import { SaveOutlined, EyeOutlined, EditOutlined } from '@ant-design/icons'
import { useState } from 'react'
import type { ApiProduct } from '@/types/api-product'

const { TextArea } = Input


interface ApiProductUsageGuideProps {
  apiProduct: ApiProduct
}

const mockUsageGuide = `# Payment API 使用指南

## 快速开始

### 1. 获取API密钥
首先，您需要在开发者门户中注册并获取API密钥。

### 2. 安装SDK
\`\`\`bash
npm install payment-api-sdk
\`\`\`

### 3. 初始化客户端
\`\`\`javascript
import { PaymentAPI } from 'payment-api-sdk';

const client = new PaymentAPI({
  apiKey: 'your-api-key',
  environment: 'production'
});
\`\`\`

## 基本用法

### 创建支付
\`\`\`javascript
const payment = await client.payments.create({
  amount: 100.00,
  currency: 'USD',
  payment_method: 'card',
  description: 'Payment for order #123'
});

console.log(payment.id); // pay_123456789
\`\`\`

### 查询支付状态
\`\`\`javascript
const payment = await client.payments.retrieve('pay_123456789');
console.log(payment.status); // succeeded
\`\`\`

## 错误处理
\`\`\`javascript
try {
  const payment = await client.payments.create({
    amount: -100.00, // 无效金额
    currency: 'USD'
  });
} catch (error) {
  console.error('Payment failed:', error.message);
}
\`\`\`

## 最佳实践
1. 始终验证API响应
2. 实现适当的错误处理
3. 使用webhook接收支付通知
4. 定期检查API状态

## 支持
如有问题，请联系我们的技术支持团队。
`

export function ApiProductUsageGuide({ apiProduct }: ApiProductUsageGuideProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [content, setContent] = useState(mockUsageGuide)

  const handleSave = () => {
    setIsEditing(false)
    // 这里可以调用API保存使用指南
  }

  const handlePreview = () => {
    // 这里可以打开预览模式
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold mb-2">使用指南</h1>
          <p className="text-gray-600">编辑和发布API使用指南</p>
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
              placeholder="请输入使用指南内容..."
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

      <Card title="指南设置">
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <span>指南可见性</span>
            <Tag color="green">公开</Tag>
          </div>
          <div className="flex justify-between items-center">
            <span>多语言支持</span>
            <Tag color="blue">中文</Tag>
          </div>
          <div className="flex justify-between items-center">
            <span>版本控制</span>
            <Tag color="blue">启用</Tag>
          </div>
        </div>
      </Card>
    </div>
  )
} 