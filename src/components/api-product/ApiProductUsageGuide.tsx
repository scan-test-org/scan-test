import { Card, Button, Space, Tag, message } from 'antd'
import { SaveOutlined, EditOutlined, UploadOutlined } from '@ant-design/icons'
import { useEffect, useState, useRef } from 'react'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm';
import MdEditor from 'react-markdown-editor-lite'
import 'react-markdown-editor-lite/lib/index.css'
import type { ApiProduct } from '@/types/api-product'
import { apiProductApi } from '@/lib/api'

interface ApiProductUsageGuideProps {
  apiProduct: ApiProduct
  handleRefresh: () => void
}

export function ApiProductUsageGuide({ apiProduct, handleRefresh }: ApiProductUsageGuideProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [content, setContent] = useState(apiProduct.document || '')
  const fileInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    setContent(apiProduct.document || '')
  }, [apiProduct.document])

  const handleSave = () => {
    setIsEditing(false)
    apiProductApi.updateApiProduct(apiProduct.productId, {
      document: content
    }).then(() => {
      message.success('保存成功')
      handleRefresh();

    })
  }

  const handleEditorChange = ({ text }: { text: string }) => {
    setContent(text)
  }

  const handleFileImport = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      if (file.type !== 'text/markdown' && !file.name.endsWith('.md')) {
        message.error('请选择 Markdown 文件 (.md)')
        return
      }

      const reader = new FileReader()
      reader.onload = (e) => {
        const content = e.target?.result as string
        setContent(content)
        message.success('文件导入成功')
      }
      reader.readAsText(file)
    }
    // 清空 input 值，允许重复选择同一文件
    if (event.target) {
      event.target.value = ''
    }
  }

  const triggerFileInput = () => {
    fileInputRef.current?.click()
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold mb-2">使用指南</h1>
          <p className="text-gray-600">编辑和发布API使用指南</p>
        </div>
        <Space>
          {isEditing && (
            <Button icon={<UploadOutlined />} onClick={triggerFileInput}>
              导入文件
            </Button>
          )}
          {isEditing ? (
            <Button type="primary" icon={<SaveOutlined />} onClick={handleSave}>
              保存
            </Button>
          ) : (
            <Button icon={<EditOutlined />} onClick={() => setIsEditing(true)}>
              编辑
            </Button>
          )}
          {isEditing && (
            <Button onClick={() => setIsEditing(false)}>
              取消编辑
            </Button>
          )}
        </Space>
      </div>

      <Card>
        <div className="mb-4">
          <Space>
            <Tag color="blue">Markdown</Tag>
          </Space>
        </div>

        {isEditing ? (
          <div>
            <MdEditor
              value={content}
              onChange={handleEditorChange}
              style={{ height: '500px', minHeight: '300px', width: '100%' }}
              placeholder="请输入使用指南内容..."
              renderHTML={(text) => <ReactMarkdown remarkPlugins={[remarkGfm]}>{text}</ReactMarkdown>}
              canView={{ menu: true, md: true, html: true, both: true, fullScreen: false, hideMenu: false }}
              htmlClass="custom-html-style"
              markdownClass="custom-markdown-style"
            />
            <div className="mt-4 text-sm text-gray-500">
              支持Markdown格式，可以使用代码块、表格、链接等语法
            </div>
          </div>
        ) : (
          <div className="prose custom-html-style h-[500px] overflow-auto">
            {content ? (
              <ReactMarkdown remarkPlugins={[remarkGfm]}>{content}</ReactMarkdown>
            ) : (
              <div className="min-h-[300px] flex items-center justify-center text-gray-400 border-2 border-dashed border-gray-200 rounded-lg">
                <div className="text-center">
                  <p className="text-lg">暂无使用指南内容</p>
                  <p className="text-sm">点击编辑按钮开始编写使用指南</p>
                </div>
              </div>
            )}
          </div>
        )}
      </Card>

      {/* 隐藏的文件输入框 */}
      <input
        ref={fileInputRef}
        type="file"
        accept=".md,text/markdown"
        onChange={handleFileImport}
        style={{ display: 'none' }}
      />
    </div>
  )
} 