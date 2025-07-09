"use client"

import { useState } from "react"
import { useParams } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Navigation } from "@/components/navigation"
import { Footer } from "@/components/footer"
import { 
  FileText, 
  BookOpen, 
  ArrowLeft,
  Globe,
  Code,
  Settings,
  Download,
  Play
} from "lucide-react"
import Link from "next/link"
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeHighlight from 'rehype-highlight'
import 'highlight.js/styles/github.css'

// 模拟MCP数据
const mockMcpData = {
  "1": {
    id: "1",
    name: "file-manager",
    title: "File Manager MCP",
    description: "Powerful file management and operations for modern applications",
    version: "v2.1.0",
    category: "File Management",
    status: "active",
    author: "Company Team",
    license: "MIT",
    homepage: "https://github.com/company/file-manager-mcp",
    repository: "https://github.com/company/file-manager-mcp.git",
    documentation: `# File Manager MCP

## Overview

The File Manager MCP provides a comprehensive set of tools for file operations, directory management, and file system interactions through the Model Context Protocol.

## Features

- **File Operations**: Create, read, update, and delete files
- **Directory Management**: Navigate and manipulate directory structures  
- **File Search**: Advanced search capabilities with filters
- **Permission Management**: Handle file permissions and access control
- **Batch Operations**: Perform operations on multiple files simultaneously

## Installation

\`\`\`bash
npm install @company/file-manager-mcp
\`\`\`

## Configuration

\`\`\`json
{
  "mcp": {
    "fileManager": {
      "rootPath": "/app/files",
      "allowedExtensions": [".txt", ".json", ".md"],
      "maxFileSize": "10MB"
    }
  }
}
\`\`\`

## Usage Examples

### Basic File Operations

\`\`\`javascript
// Read a file
const content = await mcp.fileManager.readFile('/path/to/file.txt');

// Write a file
await mcp.fileManager.writeFile('/path/to/file.txt', 'Hello World');

// Delete a file
await mcp.fileManager.deleteFile('/path/to/file.txt');
\`\`\`

### Directory Operations

\`\`\`javascript
// List directory contents
const files = await mcp.fileManager.listDirectory('/path/to/directory');

// Create directory
await mcp.fileManager.createDirectory('/path/to/new-directory');

// Remove directory
await mcp.fileManager.removeDirectory('/path/to/directory');
\`\`\`

### Advanced Features

\`\`\`javascript
// Search files
const results = await mcp.fileManager.searchFiles({
  pattern: '*.js',
  directory: '/src',
  recursive: true
});

// Batch operations
await mcp.fileManager.batchCopy([
  { from: '/src/file1.js', to: '/dist/file1.js' },
  { from: '/src/file2.js', to: '/dist/file2.js' }
]);
\`\`\`

## API Reference

### Methods

#### \`readFile(path: string): Promise<string>\`
Reads the contents of a file.

#### \`writeFile(path: string, content: string): Promise<void>\`
Writes content to a file.

#### \`deleteFile(path: string): Promise<void>\`
Deletes a file.

#### \`listDirectory(path: string): Promise<FileInfo[]>\`
Lists the contents of a directory.

### Error Handling

The MCP uses standard error codes for file operations:

- \`FILE_NOT_FOUND\`: The specified file doesn't exist
- \`PERMISSION_DENIED\`: Insufficient permissions for the operation
- \`INVALID_PATH\`: The provided path is invalid
- \`DISK_FULL\`: Not enough disk space for the operation

## License

MIT License - see LICENSE file for details.`,
    usageGuide: `# File Manager MCP - Usage Guide

## Getting Started

This guide will help you integrate and use the File Manager MCP in your applications.

### Prerequisites

- Node.js 16 or higher
- MCP Runtime environment
- Write permissions for file operations

### Step 1: Installation

Install the File Manager MCP package:

\`\`\`bash
npm install @company/file-manager-mcp
\`\`\`

### Step 2: Basic Setup

Initialize the MCP in your application:

\`\`\`javascript
import { FileManagerMcp } from '@company/file-manager-mcp';

const fileManager = new FileManagerMcp({
  rootPath: './data',
  permissions: {
    read: true,
    write: true,
    delete: true
  }
});
\`\`\`

### Step 3: Common Use Cases

#### File Upload Handler

\`\`\`javascript
async function handleFileUpload(file, destination) {
  try {
    const content = await file.text();
    await fileManager.writeFile(destination, content);
    console.log('File uploaded successfully');
  } catch (error) {
    console.error('Upload failed:', error);
  }
}
\`\`\`

#### File Processing Pipeline

\`\`\`javascript
async function processFiles(inputDir, outputDir) {
  const files = await fileManager.listDirectory(inputDir);
  
  for (const file of files) {
    const content = await fileManager.readFile(file.path);
    const processed = await processContent(content);
    await fileManager.writeFile(\`\${outputDir}/\${file.name}\`, processed);
  }
}
\`\`\`

### Best Practices

1. **Error Handling**: Always wrap file operations in try-catch blocks
2. **Path Validation**: Validate file paths before operations
3. **Permission Checks**: Verify permissions before attempting operations
4. **Resource Cleanup**: Close file handles and clean up temporary files

### Troubleshooting

Common issues and solutions:

- **Permission Errors**: Check file system permissions
- **Path Not Found**: Verify paths exist and are accessible
- **Memory Issues**: Use streaming for large files

For more help, visit our [support documentation](https://docs.company.com/mcp/file-manager).`
  },
  // 可以添加更多MCP数据...
}

export default function McpDetailPage() {
  const params = useParams()
  const [activeTab, setActiveTab] = useState("overview")
  
  const mcpId = params.id as string
  const mcpData = mockMcpData[mcpId as keyof typeof mockMcpData]
  
  if (!mcpData) {
    return (
      <div className="min-h-screen bg-white">
        <Navigation />
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="text-center">
            <h1 className="text-4xl font-bold text-gray-900 mb-4">MCP Service Not Found</h1>
            <p className="text-xl text-gray-600 mb-8">The requested MCP service could not be found.</p>
            <Link href="/mcp">
              <Button>
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to MCP Servers
              </Button>
            </Link>
          </div>
        </main>
        <Footer />
      </div>
    )
  }

  // 获取状态样式
  const getStatusBadge = (status: string) => {
    switch (status) {
      case "active":
        return <Badge className="bg-green-100 text-green-800 hover:bg-green-100">Active</Badge>
      case "beta":
        return <Badge className="bg-orange-100 text-orange-800 hover:bg-orange-100">Beta</Badge>
      case "deprecated":
        return <Badge className="bg-red-100 text-red-800 hover:bg-red-100">Deprecated</Badge>
      default:
        return <Badge variant="secondary">Unknown</Badge>
    }
  }

  const menuItems = [
    {
      key: "overview",
      label: "Overview", 
      icon: FileText,
      description: "Service overview and features"
    },
    {
      key: "documentation",
      label: "Documentation",
      icon: BookOpen,
      description: "Complete API documentation"
    },
    {
      key: "usage-guide",
      label: "Usage Guide",
      icon: Settings,
      description: "Integration and setup guide"
    }
  ]

  const renderContent = () => {
    switch (activeTab) {
      case "overview":
        return (
          <div className="space-y-6">
            <div className="prose prose-lg max-w-none">
              <h2>Service Overview</h2>
              <p>{mcpData.description}</p>
            </div>
            
            {/* 快速信息 */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <Card>
                <CardHeader className="flex flex-row items-center space-y-0 pb-2">
                  <Globe className="h-4 w-4 text-muted-foreground mr-2" />
                  <CardTitle className="text-sm font-medium">Repository</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-sm">
                    <a 
                      href={mcpData.repository} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="text-blue-600 hover:text-blue-800 break-all"
                    >
                      {mcpData.repository}
                    </a>
                  </div>
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader className="flex flex-row items-center space-y-0 pb-2">
                  <Code className="h-4 w-4 text-muted-foreground mr-2" />
                  <CardTitle className="text-sm font-medium">License</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-sm font-mono bg-gray-100 p-2 rounded">
                    {mcpData.license}
                  </div>
                </CardContent>
              </Card>
              
              <Card>
                <CardHeader className="flex flex-row items-center space-y-0 pb-2">
                  <Settings className="h-4 w-4 text-muted-foreground mr-2" />
                  <CardTitle className="text-sm font-medium">Author</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-sm">{mcpData.author}</div>
                </CardContent>
              </Card>
            </div>

            {/* 快速操作 */}
            <div className="flex gap-4">
              <Button className="flex items-center gap-2">
                <Download className="h-4 w-4" />
                Install Package
              </Button>
              <Button variant="outline" className="flex items-center gap-2">
                <Play className="h-4 w-4" />
                Try Online Demo
              </Button>
              <Button variant="outline" asChild>
                <a href={mcpData.homepage} target="_blank" rel="noopener noreferrer">
                  View on GitHub
                </a>
              </Button>
            </div>
          </div>
        )
      case "documentation":
        return (
          <div className="space-y-6">
            <div className="prose prose-lg max-w-none">
              <h2>Documentation</h2>
              <p>Complete API reference and documentation for the {mcpData.name} MCP service.</p>
            </div>
            <Card>
              <CardContent className="p-6">
                <div className="prose prose-slate max-w-none">
                  <ReactMarkdown
                    remarkPlugins={[remarkGfm]}
                    rehypePlugins={[rehypeHighlight]}
                    components={{
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
                    {mcpData.documentation}
                  </ReactMarkdown>
                </div>
              </CardContent>
            </Card>
          </div>
        )
      case "usage-guide":
        return (
          <div className="space-y-6">
            <div className="prose prose-lg max-w-none">
              <h2>Usage Guide</h2>
              <p>Learn how to integrate and use the {mcpData.name} MCP service in your applications.</p>
            </div>
            <Card>
              <CardContent className="p-6">
                <div className="prose prose-slate max-w-none">
                  <ReactMarkdown
                    remarkPlugins={[remarkGfm]}
                    rehypePlugins={[rehypeHighlight]}
                    components={{
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
                    {mcpData.usageGuide}
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
          <Link href="/mcp">
            <Button variant="ghost" className="mb-4">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to MCP Servers
            </Button>
          </Link>
        </div>

        {/* MCP标题信息 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <h1 className="text-4xl font-bold text-gray-900">{mcpData.title}</h1>
              <Badge variant="secondary">{mcpData.category}</Badge>
              <Badge variant="outline">v{mcpData.version}</Badge>
              {getStatusBadge(mcpData.status)}
            </div>
          </div>
          <p className="text-xl text-gray-600 mb-4">{mcpData.description}</p>
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
    </div>
  )
} 