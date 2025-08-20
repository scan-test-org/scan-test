import { Card, Row, Col, Statistic, Progress, Tag, Table, Tooltip } from 'antd'
import { 
  UserOutlined, 
  ApiOutlined,
  LinkOutlined
} from '@ant-design/icons'
import { Portal } from '@/types'
import { useState, useEffect } from 'react'
import { portalApi, apiProductApi } from '@/lib/api'
import { useNavigate } from 'react-router-dom'

interface PortalOverviewProps {
  portal: Portal
}
export function PortalOverview({ portal }: PortalOverviewProps) {
  const navigate = useNavigate()
  const [apiCount, setApiCount] = useState(0)
  const [developerCount, setDeveloperCount] = useState(0)

  useEffect(() => {
    portalApi.getDeveloperList(portal.portalId, {
      page: 1,
      size: 10
    }).then((res: any) => {
      setDeveloperCount(res.data.totalElements || 0)
    })
    apiProductApi.getApiProducts({
      portalId: portal.portalId,
      page: 1,
      size: 10
    }).then((res: any) => {
      setApiCount(res.data.totalElements || 0)
    })

  }, [portal])

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold mb-2">概览</h1>
        <p className="text-gray-600">Portal的基本信息和统计数据</p>
      </div>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={12}>
          <Card onClick={() => {
            navigate(`/portals/detail?id=${portal.portalId}&tab=developers`)
          }}>
            <Statistic
              title="注册开发者"
              value={developerCount}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={12}>
          <Card onClick={() => {
            navigate(`/portals/detail?id=${portal.portalId}&tab=published-apis`)
          }}>
            <Statistic
              title="已发布的API"
              value={apiCount}
              prefix={<ApiOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        
      </Row>

      {/* 详细信息 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={24}>
          <Card title="基本信息" className="h-full">
            <div className="space-y-4">
              <div className="flex justify-between">
                <span className="text-gray-600">Portal名称</span>
                <span className="font-medium">{portal.name}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Portal ID</span>
                <span className="font-medium">{portal.portalId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">域名</span>
                <div className="flex items-center gap-2">
                  <LinkOutlined />
                  <a href={`http://${portal.portalDomainConfig?.[0]?.domain}`} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline">
                    {portal.portalDomainConfig?.[0]?.domain}
                  </a>
                </div>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">账号密码登录</span>
                <Tag color={portal.portalSettingConfig?.builtinAuthEnabled ? "green" : "default"}>
                  {portal.portalSettingConfig?.builtinAuthEnabled ? "已启用" : "已禁用"}
                </Tag>
              </div>
              {/* <div className="flex justify-between">
                <span className="text-gray-600">OIDC认证</span>
                <Tag color={portal.portalSettingConfig?.oidcAuthEnabled ? "green" : "default"}>
                  {portal.portalSettingConfig?.oidcAuthEnabled ? "已启用" : "已禁用"}
                </Tag>
              </div> */}
              <div className="flex justify-between">
                <span className="text-gray-600">开发者自动审批</span>
                <Tag color={portal.portalSettingConfig?.autoApproveDevelopers ? "green" : "default"}>
                  {portal.portalSettingConfig?.autoApproveDevelopers ? "已启用" : "已禁用"}
                </Tag>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">订阅自动审批</span>
                <Tag color={portal.portalSettingConfig?.autoApproveSubscriptions ? "green" : "default"}>
                  {portal.portalSettingConfig?.autoApproveSubscriptions ? "已启用" : "已禁用"}
                </Tag>
              </div>
            </div>
          </Card>
        </Col>
      </Row>

      {/* OIDC配置 */}
      {portal.portalSettingConfig?.oidcConfigs?.length > 0 && (
        <Card title="OIDC认证配置">
          <div className="space-y-4">
            {portal.portalSettingConfig?.oidcConfigs.map((config) => (
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

     
    </div>
  )
} 