import { Button, Card, Typography } from "antd";
import { Link } from "react-router-dom";
import { Layout } from "../components/Layout";
import { useEffect } from "react";

const { Title, Paragraph } = Typography;

function HomePage() {
  useEffect(() => {
    // const params = new URLSearchParams(window.location.search);
    // const token = params.get("token");
    // if (!token) {
    //   window.location.href = `/login?portalId=test_portal`;
    // }
  }, []);

  return (
    <Layout>
      <div className="text-center">
        <Title level={1} className="text-6xl font-bold text-gray-900 mb-6">
          API Dev Portal
        </Title>
        <Paragraph className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
          Here you will have some good context in the subheading for your developer portal so users can know
          more about your product
        </Paragraph>
        <Link to="/apis">
          <Button 
            type="primary" 
            size="large" 
            className="bg-purple-600 hover:bg-purple-700 text-white px-8 py-3 text-lg"
          >
            Get started
          </Button>
        </Link>
      </div>
      
      <div className="mt-16">
        <Card className="bg-gradient-to-r from-purple-600 via-blue-600 to-purple-800 border-0">
          <div className="relative overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-r from-purple-600 via-blue-600 to-purple-800 opacity-90"></div>
            <div className="absolute inset-0 grid grid-cols-8 gap-4">
              {Array.from({ length: 32 }, (_, i) => (
                <div key={i} className="bg-white/10 rounded-full aspect-square opacity-30"></div>
              ))}
            </div>
            <div className="relative z-10 h-64 flex items-center justify-center">
              <div className="text-white text-center">
                <Title level={2} className="text-3xl font-bold mb-4 text-white">
                  Explore our APIs
                </Title>
                <Paragraph className="text-purple-100 text-lg">
                  Discover powerful APIs to enhance your applications
                </Paragraph>
              </div>
            </div>
          </div>
        </Card>
      </div>
    </Layout>
  );
}

export default HomePage; 