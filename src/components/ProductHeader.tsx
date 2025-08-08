import React from "react";
import { Typography, Tag, Space } from "antd";
import { getStatusText, getStatusColor, getCategoryText, getCategoryColor } from "../lib/statusUtils";

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
}) => {
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
    </div>
  );
}; 