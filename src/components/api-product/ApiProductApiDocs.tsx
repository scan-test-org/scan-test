import { Card, Button, Input, Space, Tag, Divider } from 'antd'
import { SaveOutlined, EyeOutlined, EditOutlined } from '@ant-design/icons'
import { useState } from 'react'
import type { ApiProduct } from '@/types/api-product';

const { TextArea } = Input

interface ApiProductApiDocsProps {
  apiProduct: ApiProduct
}

export function ApiProductApiDocs({ apiProduct }: ApiProductApiDocsProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [content, setContent] = useState(apiProduct.apiSpec)
  

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
    </div>
  )
} 