import { Card, Button, Input, Space, Tag, message } from 'antd'
import { SaveOutlined, EyeOutlined, EditOutlined } from '@ant-design/icons'
import { useEffect, useMemo, useState } from 'react'
import type { ApiProduct } from '@/types/api-product';


interface ApiProductApiDocsProps {
  apiProduct: ApiProduct
  handleRefresh: () => void
}

export function ApiProductApiDocs({ apiProduct }: ApiProductApiDocsProps) {
  
  const [content, setContent] = useState('')
  useEffect(() => {    
    if (apiProduct.apiConfig) {
      setContent(apiProduct.apiConfig.spec)
    } else if (apiProduct.mcpConfig) {
      setContent(apiProduct.mcpConfig.tools)
    }
  }, [apiProduct])


  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold mb-2">API文档</h1>
          <p className="text-gray-600">编辑和发布API文档</p>
        </div>
        
      </div>

      <Card>
        <div className="mb-4">
          <Space>
            <Tag color="blue">{apiProduct.apiConfig?.meta.source || apiProduct.mcpConfig?.meta.source}</Tag>
            <Tag color="green">{apiProduct.apiConfig?.meta.type || apiProduct.mcpConfig?.meta.fromType}</Tag>
          </Space>
        </div>

        <div className="h-[500px] overflow-y-auto">
          <div className="prose max-w-none">
              <pre className="bg-gray-100 p-4 rounded overflow-x-auto">
                <code>{content}</code>
              </pre>
            </div>
        </div>
      </Card>
    </div>
  )
} 