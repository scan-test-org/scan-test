import { Card, Row, Col, Statistic, Progress, Tag } from 'antd'
import { 
  ApiOutlined, 
  UserOutlined, 
  SettingOutlined,
  EyeOutlined,
  ClockCircleOutlined
} from '@ant-design/icons'
import type { ApiProduct } from '@/types/api-product'
import { getStatusBadgeVariant } from '@/lib/utils'

interface ApiProductOverviewProps {
  apiProduct: ApiProduct
}

export function ApiProductOverview({ apiProduct }: ApiProductOverviewProps) {
  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold mb-2">概览</h1>
        <p className="text-gray-600">API产品的基本信息和统计数据</p>
      </div>

      {/* 统计卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="关联门户"
              value={apiProduct.portals}
              prefix={<EyeOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="关联服务"
              value={apiProduct.linkedServices}
              prefix={<ApiOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        
        <Col xs={24} sm={12} lg={8}>
          <Card>
            <Statistic
              title="用户数量"
              value={0}
              prefix={<UserOutlined />}
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
                <span className="text-gray-600">产品名称</span>
                <span className="font-medium">{apiProduct.name}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">状态</span>
                <Tag color={getStatusBadgeVariant(apiProduct.status)}>
                  {apiProduct.status === "PENDING" ? "待关联" : apiProduct.status === "READY" ? "已关联" : "已发布"}
                </Tag>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">创建时间</span>
                <span>{apiProduct.createdAt}</span>
              </div>
              
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="使用统计" className="h-full">
            <div className="space-y-6">
              <div>
                <div className="flex justify-between mb-2">
                  <span>API调用次数</span>
                  <span className="text-blue-600">0</span>
                </div>
                <Progress percent={0} strokeColor="#1890ff" />
              </div>
              <div>
                <div className="flex justify-between mb-2">
                  <span>错误率</span>
                  <span className="text-red-600">0%</span>
                </div>
                <Progress percent={0} strokeColor="#ff4d4f" />
              </div>
              <div>
                <div className="flex justify-between mb-2">
                  <span>平均响应时间</span>
                  <span className="text-green-600">0ms</span>
                </div>
                <Progress percent={0} strokeColor="#52c41a" />
              </div>
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  )
} 