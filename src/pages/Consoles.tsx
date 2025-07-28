import { useState } from 'react'
import { Button, Table, Badge, Dropdown, Space } from 'antd'
import { PlusOutlined, MoreOutlined } from '@ant-design/icons'
import type { MenuProps } from 'antd'

interface Gateway {
  id: string
  name: string
  description: string
  type: "kong" | "nginx" | "envoy" | "traefik"
  status: "running" | "stopped" | "error"
  endpoint: string
  version: string
  lastUpdated: string
  environment: "production" | "staging" | "development"
  services: number
  plugins: number
}

const mockGateways: Gateway[] = [
  {
    id: "1",
    name: "Production Gateway",
    description: "生产环境网关实例",
    type: "kong",
    status: "running",
    endpoint: "https://api.example.com",
    version: "3.5.0",
    lastUpdated: "2025-01-08T10:30:00Z",
    environment: "production",
    services: 12,
    plugins: 8
  },
  {
    id: "2",
    name: "Staging Gateway",
    description: "测试环境网关实例",
    type: "kong",
    status: "running",
    endpoint: "https://staging-api.example.com",
    version: "3.5.0",
    lastUpdated: "2025-01-07T15:45:00Z",
    environment: "staging",
    services: 8,
    plugins: 5
  },
  {
    id: "3",
    name: "Dev Gateway",
    description: "开发环境网关实例",
    type: "kong",
    status: "stopped",
    endpoint: "https://dev-api.example.com",
    version: "3.4.0",
    lastUpdated: "2025-01-06T09:20:00Z",
    environment: "development",
    services: 3,
    plugins: 2
  }
]

export default function Consoles() {
  const [gateways, setGateways] = useState<Gateway[]>(mockGateways)

  const getStatusBadgeVariant = (status: string) => {
    switch (status) {
      case "running":
        return "success"
      case "stopped":
        return "default"
      case "error":
        return "error"
      default:
        return "default"
    }
  }

  const getStatusText = (status: string) => {
    switch (status) {
      case "running":
        return "运行中"
      case "stopped":
        return "已停止"
      case "error":
        return "错误"
      default:
        return status
    }
  }

  const getEnvironmentBadgeVariant = (env: string) => {
    switch (env) {
      case "production":
        return "red"
      case "staging":
        return "orange"
      case "development":
        return "blue"
      default:
        return "default"
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("zh-CN", {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit"
    })
  }

  const dropdownItems: MenuProps['items'] = [
    {
      key: 'edit',
      label: '编辑',
    },
    {
      key: 'view',
      label: '查看详情',
    },
    {
      key: 'copy',
      label: '复制配置',
    },
    {
      type: 'divider',
    },
    {
      key: 'delete',
      label: '删除',
      danger: true,
    },
  ]

  const columns = [
    {
      title: '名称',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: Gateway) => (
        <div>
          <div className="font-medium">{name}</div>
          <div className="text-sm text-gray-500">
            {record.description}
          </div>
          <div className="text-xs text-gray-400 mt-1">
            {record.endpoint}
          </div>
        </div>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Badge status={getStatusBadgeVariant(status)} text={getStatusText(status)} />
      )
    },
    {
      title: '环境',
      dataIndex: 'environment',
      key: 'environment',
      render: (environment: string) => (
        <Badge color={getEnvironmentBadgeVariant(environment)} text={environment} />
      )
    },
    {
      title: '服务数',
      dataIndex: 'services',
      key: 'services',
      render: (services: number, record: Gateway) => (
        <div className="text-sm">
          <div>{services} 个服务</div>
          <div className="text-gray-500">{record.plugins} 个插件</div>
        </div>
      )
    },
    {
      title: '最后更新',
      dataIndex: 'lastUpdated',
      key: 'lastUpdated',
      render: (date: string) => formatDate(date)
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Gateway) => (
        <Dropdown menu={{ items: dropdownItems }} trigger={['click']}>
          <Button type="text" icon={<MoreOutlined />} />
        </Dropdown>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">网关实例</h1>
          <p className="text-gray-500 mt-2">
            管理和配置您的网关实例
          </p>
        </div>
        <Button type="primary" icon={<PlusOutlined />}>
          导入网关实例
        </Button>
      </div>

      <div className="bg-white rounded-lg border">
        <Table
          columns={columns}
          dataSource={gateways}
          rowKey="id"
          pagination={{
            position: ['bottomRight'],
          }}
        />
      </div>
    </div>
  )
}
