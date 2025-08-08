import { Card, Descriptions } from "antd";
import type { Consumer } from "../../types/consumer";

interface ConsumerBasicInfoProps {
  consumer: Consumer;
}

export function ConsumerBasicInfo({ consumer }: ConsumerBasicInfoProps) {
  return (
    <Card>
      <Descriptions column={2} bordered>
        <Descriptions.Item label="消费者ID">{consumer.consumerId}</Descriptions.Item>
        <Descriptions.Item label="名称">{consumer.name}</Descriptions.Item>
        <Descriptions.Item label="描述">{consumer.description || '-'}</Descriptions.Item>
        <Descriptions.Item label="状态">
          {consumer.enabled ? '已启用' : '未启用'}
        </Descriptions.Item>
        <Descriptions.Item label="创建时间">
          {consumer.createAt ? new Date(consumer.createAt).toLocaleString() : '-'}
        </Descriptions.Item>
      </Descriptions>
    </Card>
  );
} 