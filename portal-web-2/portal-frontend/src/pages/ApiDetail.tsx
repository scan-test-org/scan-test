import { useParams } from "react-router-dom";
import { Button } from "../components/ui/button";
import { Card } from "../components/ui/card";
import { Badge } from "../components/ui/badge";
import { Table } from "../components/ui/table";
import { Footer } from "../components/footer";
import { Navigation } from "../components/navigation";

function ApiDetailPage() {
  const { id } = useParams();
  // 这里可根据 id 获取 API 详情数据
  // 这里只做结构迁移，具体内容可后续细化
  return (
    <div className="min-h-screen bg-white">
      <Navigation />
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <h1 className="text-3xl font-bold mb-4">API 详情 - {id}</h1>
        {/* 这里展示 API 详情、文档、使用指南等内容 */}
        <Card className="mb-8">
          <div className="p-4">API 详细信息展示区域</div>
        </Card>
        <Card>
          <div className="p-4">API 绑定关系、消费者等信息展示区域</div>
        </Card>
      </main>
      <Footer />
    </div>
  );
}

export default ApiDetailPage; 