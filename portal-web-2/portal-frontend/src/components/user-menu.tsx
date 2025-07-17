import React, { useState, useEffect } from "react";
import { Avatar, Button, Dropdown, Menu } from "antd";
import { UserOutlined, LogoutOutlined, SettingOutlined, TeamOutlined } from "@ant-design/icons";

interface UserInfo {
  username: string;
  name: string;
}

export function UserMenu() {
  const [user, setUser] = useState<UserInfo | null>(null);

  useEffect(() => {
    const isLoggedIn = localStorage.getItem("isLoggedIn");
    const userInfo = localStorage.getItem("user");
    if (isLoggedIn === "true" && userInfo) {
      setUser(JSON.parse(userInfo));
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("isLoggedIn");
    localStorage.removeItem("user");
    setUser(null);
    window.location.reload();
  };

  if (!user) return null;

  const menu = (
    <Menu>
      <Menu.Item key="profile" disabled>
        <div className="flex flex-col space-y-1 p-2">
          <span className="text-sm font-medium leading-none">{user.name}</span>
          <span className="text-xs leading-none text-gray-400">@{user.username}</span>
        </div>
      </Menu.Item>
      <Menu.Divider />
      <Menu.Item key="consumers" icon={<TeamOutlined />}>
        <a href="/consumers">Consumer Management</a>
      </Menu.Item>
      <Menu.Item key="logout" icon={<LogoutOutlined />} onClick={handleLogout}>
        Logout
      </Menu.Item>
    </Menu>
  );

  return (
    <Dropdown overlay={menu} placement="bottomRight" trigger={["click"]}>
      <Button type="text" className="relative h-8 w-8 p-0 flex items-center justify-center rounded-full">
        <Avatar
          className="h-8 w-8"
          icon={<UserOutlined />}
          style={{ backgroundColor: "#722ed1", color: "#fff" }}
        >
          {user.name.charAt(0).toUpperCase()}
        </Avatar>
      </Button>
    </Dropdown>
  );
} 