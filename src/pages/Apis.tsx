import { useState, useEffect } from "react";
import { Card, Tag, Button, Typography, Input, Select, Row, Col } from "antd";
import { SearchOutlined, EyeOutlined, ApiOutlined } from "@ant-design/icons";
import { Link } from "react-router-dom";
import { Layout } from "../components/Layout";
import api from "../lib/api";
import type { ApiProduct } from '../types';

const { Title, Paragraph } = Typography;
const { Search } = Input;

function APIsPage() {

  const [products, setProducts] = useState<ApiProduct[]>([]);

  useEffect(() => {
    api.get('/products').then((res) => {
      setProducts(res.data?.content || []);
    });
  }, []);


  return (
    <Layout>
      <div className="mb-8">
        <Title level={1} className="mb-2">
          API 列表
        </Title>
        <Paragraph className="text-gray-600">
          探索我们提供的各种API服务
        </Paragraph>
      </div>

      <Card>
        <div className="mb-6 flex gap-4 flex-wrap">
          <Search
            placeholder="搜索API..."
            prefix={<SearchOutlined />}
            style={{ width: 300 }}
          />
          <Select
            placeholder="选择分类"
            style={{ width: 150 }}
            allowClear
          >
            <Select.Option value="Finance">Finance</Select.Option>
            <Select.Option value="Authentication">Authentication</Select.Option>
            <Select.Option value="Communication">Communication</Select.Option>
            <Select.Option value="Analytics">Analytics</Select.Option>
            <Select.Option value="Storage">Storage</Select.Option>
            <Select.Option value="AI">AI</Select.Option>
          </Select>
          
        </div>
        
        <Row gutter={[16, 16]}>
          {products.map((api) => (
            <Col xs={24} sm={12} lg={8} xl={6} key={api.productId}>
              <Card
                hoverable
                className="h-full"
                actions={[
                  <Link to={`/apis/${api.productId}`} key="view">
                    <Button type="link" icon={<EyeOutlined />} size="small">
                      查看详情
                    </Button>
                  </Link>
                ]}
              >
                <div className="mb-3">
                  <div className="flex items-center mb-2">
                    <ApiOutlined className="text-blue-500 mr-2" />
                    <Title level={5} className="mb-0 flex-1 truncate">
                      {api.name}
                    </Title>
                  </div>
                  <Paragraph 
                    className="text-gray-600 mb-3 text-sm min-h-[36px]" 
                    ellipsis={{ rows: 2 }}
                  >
                    {api.description}
                  </Paragraph>
                </div>
                
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Tag color="default">
                      {api.category}
                    </Tag>
                    
                  </div>
                  
                </div>
              </Card>
            </Col>
          ))}
        </Row>
      </Card>
    </Layout>
  );
}

export default APIsPage; 