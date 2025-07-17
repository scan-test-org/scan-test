import { Link } from "react-router-dom";
import { Typography, Row, Col, Divider } from "antd";

const { Title, Paragraph } = Typography;

export function Footer() {
  return (
    <footer className="bg-white border-t mt-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <Row gutter={[32, 32]}>
          <Col xs={24} md={6}>
            <div className="flex items-center space-x-2 mb-4">
              <div className="w-8 h-8 bg-black rounded-full flex items-center justify-center">
                <span className="text-white font-bold text-sm">C</span>
              </div>
              <Title level={4} className="mb-0 text-gray-900">Company</Title>
            </div>
            <Paragraph className="text-gray-600 text-sm leading-relaxed">
              Build powerful applications with our comprehensive API platform.
            </Paragraph>
          </Col>
          
          <Col xs={24} md={6}>
            <Title level={5} className="mb-4 text-gray-900">Products</Title>
            <div className="space-y-3">
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm transition-colors">
                Pricing
              </Link>
              <Link to="/apis" className="block text-gray-600 hover:text-gray-900 text-sm transition-colors">
                APIs
              </Link>
              <Link to="/mcp" className="block text-gray-600 hover:text-gray-900 text-sm transition-colors">
                MCP Servers
              </Link>
            </div>
          </Col>
          
          <Col xs={24} md={6}>
            <Title level={5} className="mb-4 text-gray-900">Company</Title>
            <div className="space-y-3">
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm transition-colors">
                About
              </Link>
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm transition-colors">
                Careers
              </Link>
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm transition-colors">
                Press
              </Link>
            </div>
          </Col>
          
          <Col xs={24} md={6}>
            <Title level={5} className="mb-4 text-gray-900">Legal</Title>
            <div className="space-y-3">
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm transition-colors">
                Terms and conditions
              </Link>
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm transition-colors">
                Data privacy
              </Link>
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm transition-colors">
                Trust and compliance
              </Link>
            </div>
          </Col>
        </Row>
        
        <Divider className="my-8" />
        
        <div className="text-center">
          <Paragraph className="text-gray-500 text-sm mb-0">
            © 2024 Company. All rights reserved.
          </Paragraph>
        </div>
      </div>
    </footer>
  );
} 