import React, { useState } from "react";
import { Typography, Tag, Space, Button, Modal, Form, Select, Input, message } from "antd";
import { getStatusText, getStatusColor, getCategoryText, getCategoryColor } from "../lib/statusUtils";
import { useParams } from "react-router-dom";
import { getConsumers, subscribeProduct } from "../lib/api";
import type { Consumer } from "../types/consumer";
import type { McpConfig } from "../types";

const { Title, Paragraph } = Typography;

interface ProductHeaderProps {
  name: string;
  description: string;
  status: string;
  category: string;
  icon?: string;
  defaultIcon?: string;
  enableConsumerAuth?: boolean;
  version?: string;
  enabled?: boolean;
  showConsumerAuth?: boolean;
  showVersion?: boolean;
  showEnabled?: boolean;
  mcpConfig?: McpConfig | null;
}

export const ProductHeader: React.FC<ProductHeaderProps> = ({
  name,
  description,
  status,
  category,
  icon,
  defaultIcon = "/default-icon.png",
  enableConsumerAuth,
  version,
  enabled,
  showConsumerAuth = false,
  showVersion = false,
  showEnabled = false,
  mcpConfig,
}) => {
  const { id, mcpName } = useParams();
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [consumers, setConsumers] = useState<Consumer[]>([]);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm();

  // 判断是否应该显示申请订阅按钮
  const shouldShowSubscribeButton = !mcpConfig || mcpConfig.meta.source !== 'NACOS';

  // 获取消费者列表
  const fetchConsumers = async () => {
    try {
      setLoading(true);
      const response = await getConsumers({}, { page: 1, size: 100 });
      if (response.data) {
        setConsumers(response.data.content || response.data);
      }
    } catch (error) {
      // message.error('获取消费者列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 显示弹窗
  const showModal = () => {
    setIsModalVisible(true);
    fetchConsumers();
  };

  // 隐藏弹窗
  const handleCancel = () => {
    setIsModalVisible(false);
    form.resetFields();
  };

  // 提交申请
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      console.log('申请订阅:', values);
      
      // 调用申请订阅API
      await subscribeProduct(values.consumerId, id || mcpName || '');
      message.success('申请提交成功');
      setIsModalVisible(false);
      form.resetFields();
    } catch (error) {
      console.error('申请订阅失败:', error);
      // message.error('申请提交失败，请重试');
    }
  };

  return (
    <div className="mb-8">
      <div className="flex items-start gap-4 mb-4">
        <img
          src={icon || defaultIcon}
          alt="icon"
          className="w-16 h-16 rounded-lg object-cover border"
          onError={(e) => {
            const target = e.target as HTMLImageElement;
            target.src = defaultIcon;
          }}
        />
        <div className="flex-1">
          <Title level={1} className="mb-2">
            {name}
          </Title>
          <Space className="mb-3">
            <Tag color={getStatusColor(status)}>
              {getStatusText(status)}
            </Tag>
            {category && (
              <Tag color={getCategoryColor(category)}>
                {getCategoryText(category)}
              </Tag>
            )}
            {showVersion && version && (
              <Tag color="blue">{version}</Tag>
            )}
            {showEnabled && typeof enabled !== "undefined" && (
              <Tag color={enabled ? "green" : "red"}>
                {enabled ? "已启用" : "未启用"}
              </Tag>
            )}
            {showConsumerAuth && typeof enableConsumerAuth !== "undefined" && (
              <Tag color={enableConsumerAuth ? "green" : "orange"}>
                消费者鉴权: {enableConsumerAuth ? "开启" : "关闭"}
              </Tag>
            )}
          </Space>
        </div>
      </div>
      <Paragraph className="text-gray-600 mb-3">{description}</Paragraph>
      {shouldShowSubscribeButton && (
        <Button type="primary" onClick={showModal}>
          申请订阅
        </Button>
      )}

      {/* 申请订阅弹窗 */}
      <Modal
        title="申请订阅"
        open={isModalVisible}
        onCancel={handleCancel}
        onOk={handleSubmit}
        okText="确认"
        cancelText="取消"
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          className="mt-4"
        >
          <Form.Item label="产品名称">
            <Input value={name} disabled />
          </Form.Item>
          
          <Form.Item label="产品ID">
            <Input value={id || mcpName} disabled />
          </Form.Item>

          <Form.Item
            label="消费者"
            name="consumerId"
            rules={[{ required: true, message: '请选择消费者' }]}
          >
            <Select
              placeholder="搜索或选择消费者"
              showSearch
              loading={loading}
              filterOption={(input, option) =>
                (option?.children as unknown as string)?.toLowerCase().includes(input.toLowerCase())
              }
              notFoundContent={loading ? '加载中...' : '暂无消费者数据'}
            >
              {consumers.map(consumer => (
                <Select.Option key={consumer.consumerId} value={consumer.consumerId}>
                  {consumer.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          {/* <Form.Item
            label="申请理由"
            name="reason"
            rules={[{ required: true, message: '请输入申请理由' }]}
          >
            <TextArea
              rows={4}
              placeholder="请描述申请订阅的原因和用途"
              maxLength={500}
              showCount
            />
          </Form.Item> */}
        </Form>
      </Modal>
    </div>
  );
}; 