import { useState, useEffect } from "react";
import { Button, Avatar, Dropdown } from "antd";
import { UserOutlined, LogoutOutlined, AppstoreOutlined } from "@ant-design/icons";
import api from "../lib/api";
import { useNavigate } from "react-router-dom";

interface UserInfo {
  displayName: string;
  email?: string;
  avatar?: string;
}

export function UserInfo() {
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
      api.get("/developers/profile")
        .then((response) => {
          const data = response.data;
          if (data) {
            setUserInfo({
              displayName: data.username || data.email || "未命名用户",
              email: data.email,
              avatar: data.avatarUrl || undefined,
            });
          }
        });
  }, []);

  const handleLogout = () => {
    // 清除用户信息并跳转到登录页
    setUserInfo(null);
    navigate('/login');
  };

  const menuItems = [
    {
      key: 'user-info',
      label: (
        <div className="px-3 py-2">
          <div className="font-semibold text-gray-900">{userInfo?.displayName}</div>
          {userInfo?.email && (
            <div className="text-sm text-gray-500">{userInfo.email}</div>
          )}
        </div>
      ),
      disabled: true,
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'my-applications',
      icon: <AppstoreOutlined />,
      label: '消费者管理',
      onClick: () => navigate('/consumers'),
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ];

  if (userInfo) {
    return (
      <Dropdown
        menu={{ items: menuItems }}
        placement="bottomRight"
        trigger={['hover']}
        overlayClassName="user-dropdown" 
      >
        <div className="flex items-center space-x-2 cursor-pointer hover:bg-gray-50 px-2 py-1 rounded">
          <Avatar src={userInfo.avatar} icon={<UserOutlined />} size="default" />
          {/* <span>{userInfo.displayName}</span> */}
        </div>
      </Dropdown>
    );
  }

  return (
    <Button
      icon={<UserOutlined />}
      onClick={() => {
        navigate(`/login`);
      }}
      type="default"
    >
      登录
    </Button>
  );
} 