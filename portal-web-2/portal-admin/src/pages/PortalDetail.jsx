import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Dropdown, Menu, Modal, Avatar, Badge } from 'antd';
import { LeftOutlined, MoreOutlined, LinkOutlined, DashboardOutlined, DatabaseOutlined, TeamOutlined, UserOutlined, SettingOutlined } from '@ant-design/icons';

const mockPortal = {
  id: '1',
  name: 'test',
  title: 'Company',
  description: '测试公司门户',
  url: 'https://3995a4355203.us.kongportals.com',
  userAuth: 'Konnect Built-in',
  rbac: 'Disabled',
  authStrategy: 'Key-Auth',
  apiVisibility: 'Private',
  pageVisibility: 'Private',
  logo: undefined
};

const menuItems = [
  { key: 'overview', label: 'Overview', icon: <DashboardOutlined /> },
  { key: 'published-apis', label: 'Published APIs', icon: <DatabaseOutlined /> },
  { key: 'developers', label: 'Developers', icon: <TeamOutlined /> },
  { key: 'consumers', label: 'Consumers', icon: <UserOutlined /> },
  { key: 'settings', label: 'Settings', icon: <SettingOutlined /> },
];

const tabContent = {
  overview: <div className="p-8">这里是 Overview 内容</div>,
  'published-apis': <div className="p-8">这里是 Published APIs 内容</div>,
  developers: <div className="p-8">这里是 Developers 内容</div>,
  consumers: <div className="p-8">这里是 Consumers 内容</div>,
  settings: <div className="p-8">这里是 Settings 内容</div>,
};

const PortalDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('overview');
  const [showDelete, setShowDelete] = useState(false);
  const portal = mockPortal; // 实际项目中可根据id获取

  const menu = (
    <Menu>
      <Menu.Item key="edit">编辑Portal</Menu.Item>
      <Menu.Item key="copy">复制Portal</Menu.Item>
      <Menu.Divider />
      <Menu.Item key="delete" danger onClick={() => setShowDelete(true)}>删除Portal</Menu.Item>
    </Menu>
  );

  return (
    <div className="flex h-screen bg-gray-50">
      {/* 侧边栏 */}
      <div className="w-64 border-r bg-white flex flex-col">
        {/* 返回按钮 */}
        <div className="p-4 border-b">
          <Button
            type="text"
            icon={<LeftOutlined />}
            className="w-full flex items-center justify-start"
            onClick={() => navigate('/portals')}
          >
            返回 Portal 列表
          </Button>
        </div>
        {/* Portal 信息 */}
        <div className="p-4 border-b">
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-lg font-semibold">{portal.title}</h2>
            <Dropdown overlay={menu} trigger={["click"]}>
              <Button type="text" icon={<MoreOutlined />} className="!p-0 !h-8 !w-8 flex items-center justify-center" />
            </Dropdown>
          </div>
          <p className="text-sm text-gray-500 mb-2">{portal.name}</p>
          <div className="flex items-center gap-2 text-sm text-gray-500">
            <LinkOutlined className="text-xs" />
            <a
              href={portal.url}
              target="_blank"
              rel="noopener noreferrer"
              className="hover:underline truncate"
            >
              {portal.url}
            </a>
          </div>
        </div>
        {/* 菜单 */}
        <nav className="flex-1 p-4 space-y-1">
          {menuItems.map((item) => (
            <button
              key={item.key}
              onClick={() => setActiveTab(item.key)}
              className={`w-full flex items-center gap-3 px-3 py-2 rounded-lg text-left transition-colors ${
                activeTab === item.key
                  ? 'bg-blue-500 text-white'
                  : 'hover:bg-gray-100 text-gray-700'
              }`}
            >
              {item.icon}
              <div className="font-medium">{item.label}</div>
            </button>
          ))}
        </nav>
      </div>
      {/* 主内容区 */}
      <div className="flex-1 overflow-auto bg-white">
        {tabContent[activeTab]}
      </div>
      <Modal
        open={showDelete}
        title={<span className="text-lg font-bold">确认删除该Portal？</span>}
        onCancel={() => setShowDelete(false)}
        onOk={() => setShowDelete(false)}
        okText="删除"
        okType="danger"
        cancelText="取消"
      >
        <div>删除后不可恢复，确定要删除吗？</div>
      </Modal>
    </div>
  );
};

export default PortalDetail; 