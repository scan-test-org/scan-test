import { useState, useEffect } from "react";
import { Button, Avatar, Spin, Dropdown, Menu } from "antd";
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
  const [loadingUser, setLoadingUser] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
      setLoadingUser(true);
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
        })
        .finally(() => setLoadingUser(false));
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
      label: 'My applications',
      onClick: () => navigate('/consumers'),
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Log out',
      onClick: handleLogout,
    },
  ];

  if (loadingUser) {
    return <Spin size="small" />;
  }
  if (userInfo) {
    return (
      <Dropdown
        menu={{ items: menuItems }}
        placement="bottomRight"
        trigger={['hover']}
        overlayClassName="user-dropdown"
      >
        <div className="flex items-center space-x-2 cursor-pointer hover:bg-gray-50 px-2 py-1 rounded">
          <Avatar src={userInfo.avatar} icon={<UserOutlined />} size="small" />
          <span>{userInfo.displayName}</span>
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