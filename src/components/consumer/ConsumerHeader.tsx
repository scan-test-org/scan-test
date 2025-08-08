import { Button, Typography, Badge, Popconfirm, message } from "antd";
import { ArrowLeftOutlined, DeleteOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import api from "../../lib/api";
import type { Consumer } from "../../types/consumer";

const { Title } = Typography;

interface ConsumerHeaderProps {
  consumer: Consumer;
}

export function ConsumerHeader({ consumer }: ConsumerHeaderProps) {
  const navigate = useNavigate();

  const handleDelete = async () => {
    try {
      await api.delete(`/consumers/${consumer.consumerId}`);
      message.success('删除成功');
      navigate('/consumers');
    } catch {
      message.error('删除失败');
    }
  };

  return (
    <div className="mb-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <Button
            type="text"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/consumers')}
          >
            返回
          </Button>
          <Title level={2} className="mb-0">
            {consumer.name}
          </Title>
          {consumer.enabled && (
            <Badge status="success" text="已启用" />
          )}
        </div>
        <Popconfirm
          title="确定要删除这个消费者吗？"
          onConfirm={handleDelete}
        >
          <Button danger icon={<DeleteOutlined />}>
            删除
          </Button>
        </Popconfirm>
      </div>
    </div>
  );
} 