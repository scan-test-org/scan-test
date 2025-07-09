"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { 
  BookOpen, 
  Edit, 
  Eye, 
  Save, 
  Download, 
  Upload,
  Type
} from "lucide-react"
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeHighlight from 'rehype-highlight'
import 'highlight.js/styles/github.css'

const mockUsageGuide = `# Payment API 使用指南

## 概述
Payment API 是一个完整的支付处理解决方案，支持多种支付方式和货币类型。

## 快速开始
### 1. 获取API密钥
首先在开发者控制台获取API密钥。

### 2. 基础配置
配置API基础信息：

\`\`\`javascript
const config = {
  baseUrl: 'https://api.example.com/v1',
  apiKey: process.env.API_KEY
}
\`\`\`

## 主要功能
### 创建支付
\`\`\`javascript
const payment = await fetch('/api/v1/payments', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'X-API-Key': config.apiKey
  },
  body: JSON.stringify({
    amount: 100.00,
    currency: 'USD'
  })
})
\`\`\`

## 联系支持
- 📧 邮箱: api-support@example.com
- 📞 电话: +86 400-123-4567`

export default function UsageGuidePage() {
  const [isEditing, setIsEditing] = useState(false)
  const [guide, setGuide] = useState(mockUsageGuide)
  const [unsavedChanges, setUnsavedChanges] = useState(false)

  const handleEdit = () => {
    setIsEditing(true)
  }

  const handleSave = () => {
    console.log("Saving usage guide:", guide)
    setIsEditing(false)
    setUnsavedChanges(false)
  }

  const handleCancel = () => {
    setGuide(mockUsageGuide)
    setIsEditing(false)
    setUnsavedChanges(false)
  }

  const handleGuideChange = (value: string) => {
    setGuide(value)
    setUnsavedChanges(true)
  }

  const handleExport = () => {
    const blob = new Blob([guide], { type: 'text/markdown' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'usage-guide.md'
    a.click()
    URL.revokeObjectURL(url)
  }

  const handleImport = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        const content = e.target?.result as string
        setGuide(content)
        setUnsavedChanges(true)
      }
      reader.readAsText(file)
    }
  }

  return (
    <div className="space-y-6 p-6">
      {/* 页面标题和操作 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">使用指南</h1>
          <p className="text-muted-foreground mt-1">
            编辑和管理 API 的使用指南文档
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
                accept=".md,.txt"
                onChange={handleImport}
                className="hidden"
                id="import-guide"
              />
              <Button variant="outline" onClick={() => document.getElementById('import-guide')?.click()}>
                <Upload className="mr-2 h-4 w-4" />
                导入
              </Button>
              <Button onClick={handleEdit}>
                <Edit className="mr-2 h-4 w-4" />
                编辑指南
              </Button>
            </>
          ) : (
            <>
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

      {/* 指南统计 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">字符数</CardTitle>
            <Type className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{guide.length.toLocaleString()}</div>
            <p className="text-xs text-muted-foreground">个字符</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">章节数</CardTitle>
            <BookOpen className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {(guide.match(/^#+ /gm) || []).length}
            </div>
            <p className="text-xs text-muted-foreground">个章节</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">代码块</CardTitle>
            <Type className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {Math.floor((guide.match(/```/g) || []).length / 2)}
            </div>
            <p className="text-xs text-muted-foreground">个代码示例</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">状态</CardTitle>
            <Badge variant="default" className="h-4 w-4 p-0"></Badge>
          </CardHeader>
          <CardContent>
            <div className="text-sm font-bold text-green-600">已发布</div>
            <p className="text-xs text-muted-foreground">Markdown</p>
          </CardContent>
        </Card>
      </div>

      {/* 指南编辑器 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 编辑器 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BookOpen className="h-5 w-5" />
              Markdown 编辑器
            </CardTitle>
          </CardHeader>
          <CardContent>
            {isEditing ? (
              <Textarea
                value={guide}
                onChange={(e) => handleGuideChange(e.target.value)}
                className="min-h-[600px] font-mono text-sm"
                placeholder="请输入 Markdown 格式的使用指南..."
              />
            ) : (
              <pre className="bg-muted p-4 rounded-lg text-sm overflow-auto max-h-[600px] whitespace-pre-wrap">
                {guide}
              </pre>
            )}
          </CardContent>
        </Card>

        {/* 预览 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Eye className="h-5 w-5" />
              预览
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="bg-muted p-4 rounded-lg max-h-[600px] overflow-auto">
              <div className="prose prose-sm prose-slate dark:prose-invert max-w-none">
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
                        <code className="bg-gray-100 dark:bg-gray-800 px-1 py-0.5 rounded text-sm" {...props}>
                          {children}
                        </code>
                      )
                    },
                    // 自定义表格样式
                    table: ({ children }) => (
                      <div className="overflow-x-auto">
                        <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
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
                        className="text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
                      >
                        {children}
                      </a>
                    )
                  }}
                >
                  {guide}
                </ReactMarkdown>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>


    </div>
  )
} 