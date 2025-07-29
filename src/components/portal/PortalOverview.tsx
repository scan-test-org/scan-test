import { Card, Row, Col, Statistic, Progress, Tag, Table, Tooltip } from 'antd'
import { 
  EyeOutlined, 
  UserOutlined, 
  ApiOutlined,
  ClockCircleOutlined,
  LinkOutlined
} from '@ant-design/icons'
import { Portal } from '@/types'

interface PortalOverviewProps {
  portal: Portal
}

const mockRecentActivity = [
  {
    key: '1',
    action: 'API访问',
    description: 'Payment API被访问了1250次',
    time: '2小时前',
    user: 'developer@example.com'
  },
  {
    key: '2',
    action: '新用户注册',
    description: '新开发者注册了账户',
    time: '4小时前',
    user: 'newuser@example.com'
  },
  {
    key: '3',
    action: 'API订阅',
    description: '用户订阅了User API',
    time: '6小时前',
    user: 'existing@example.com'
  }
]

export function PortalOverview({ portal }: PortalOverviewProps) {
  const activityColumns = [
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '用户',
      dataIndex: 'user',
      key: 'user',
    },
    {
      title: '时间',
      dataIndex: 'time',
      key: 'time',
    },
  ]

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold mb-2">概览</h1>
        <p className="text-gray-600">Portal的基本信息和统计数据</p>
      </div>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="访问量"
              value={1250}
              prefix={<EyeOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="注册用户"
              value={45}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="API调用"
              value={8765}
              prefix={<ApiOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="在线时长"
              value={168}
              suffix="小时"
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 详细信息 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title="基本信息" className="h-full">
            <div className="space-y-4">
              <div className="flex justify-between">
                <span className="text-gray-600">Portal名称</span>
                <span className="font-medium">{portal.title}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Portal ID</span>
                <span className="font-medium">{portal.name}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">管理员ID</span>
                <span className="font-medium">{portal.adminId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">域名</span>
                <div className="flex items-center gap-2">
                  <LinkOutlined />
                  <a href={`http://${portal.portalDomainConfig[0]?.domain}`} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline">
                    {portal.portalDomainConfig[0]?.domain}
                  </a>
                </div>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">内置认证</span>
                <Tag color={portal.portalSettingConfig.builtinAuthEnabled ? "green" : "default"}>
                  {portal.portalSettingConfig.builtinAuthEnabled ? "已启用" : "已禁用"}
                </Tag>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">OIDC认证</span>
                <Tag color={portal.portalSettingConfig.oidcAuthEnabled ? "green" : "default"}>
                  {portal.portalSettingConfig.oidcAuthEnabled ? "已启用" : "已禁用"}
                </Tag>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">开发者自动审批</span>
                <Tag color={portal.portalSettingConfig.autoApproveDevelopers ? "green" : "default"}>
                  {portal.portalSettingConfig.autoApproveDevelopers ? "已启用" : "已禁用"}
                </Tag>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">订阅自动审批</span>
                <Tag color={portal.portalSettingConfig.autoApproveSubscriptions ? "green" : "default"}>
                  {portal.portalSettingConfig.autoApproveSubscriptions ? "已启用" : "已禁用"}
                </Tag>
              </div>
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="使用统计" className="h-full">
            <div className="space-y-6">
              <div>
                <div className="flex justify-between mb-2">
                  <span>页面访问量</span>
                  <span className="text-blue-600">1250</span>
                </div>
                <Progress percent={75} strokeColor="#1890ff" />
              </div>
              <div>
                <div className="flex justify-between mb-2">
                  <span>API调用成功率</span>
                  <span className="text-green-600">99.8%</span>
                </div>
                <Progress percent={99.8} strokeColor="#52c41a" />
              </div>
              <div>
                <div className="flex justify-between mb-2">
                  <span>用户活跃度</span>
                  <span className="text-orange-600">85%</span>
                </div>
                <Progress percent={85} strokeColor="#fa8c16" />
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* OIDC配置 */}
      {portal.portalSettingConfig.oidcOptions.length > 0 && (
        <Card title="OIDC认证配置">
          <div className="space-y-4">
            {portal.portalSettingConfig.oidcOptions.map((config) => (
              <div key={config.id} className="border rounded-lg p-4">
                <div className="flex items-center justify-between mb-3">
                  <h4 className="font-medium">{config.name}</h4>
                  <Tag color={config.enabled ? "green" : "default"}>
                    {config.enabled ? "已启用" : "已禁用"}
                  </Tag>
                </div>
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="text-gray-600">提供商:</span>
                    <span className="ml-2 font-medium">{config.provider}</span>
                  </div>
                  <div>
                    <span className="text-gray-600">Client ID:</span>
                    <span className="ml-2 font-medium" style={{ maxWidth: 280, display: 'inline-block', overflow: 'hidden', textOverflow: 'ellipsis', verticalAlign: 'bottom', whiteSpace: 'nowrap' }}>
                      <Tooltip title={config.clientId} placement="top" color="#000">
                        {config.clientId}
                      </Tooltip>
                    </span>
                  </div>
                  <div>
                    <span className="text-gray-600">授权范围:</span>
                    <span className="ml-2 font-medium">{config.scopes}</span>
                  </div>
                  <div>
                    <span className="text-gray-600">重定向URI:</span>
                    <span className="ml-2 font-medium">{config.redirectUri}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* 最近活动 */}
      <Card title="最近活动">
        <Table 
          columns={activityColumns} 
          dataSource={mockRecentActivity}
          rowKey="key"
          pagination={false}
          size="small"
        />
      </Card>

      {/* 描述 */}
      <Card title="Portal描述">
        <p className="text-gray-700">{portal.description}</p>
      </Card>
    </div>
  )
} 