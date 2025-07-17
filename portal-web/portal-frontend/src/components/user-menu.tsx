import { useState, useEffect } from "react";
import { Avatar, Button, Dropdown, Menu } from "antd";
import { UserOutlined, LogoutOutlined, TeamOutlined } from "@ant-design/icons";

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
    <Menu className="min-w-48">
      <Menu.Item key="profile" disabled>
        <div className="flex flex-col space-y-1 p-2">
          <span className="text-sm font-medium leading-none text-gray-900">{user.name}</span>
          <span className="text-xs leading-none text-gray-500">@{user.username}</span>
        </div>
      </Menu.Item>
      <Menu.Divider />
      <Menu.Item key="consumers" icon={<TeamOutlined />}>
        <a href="/consumers" className="text-gray-700">Consumer Management</a>
      </Menu.Item>
      <Menu.Item key="logout" icon={<LogoutOutlined />} onClick={handleLogout}>
        <span className="text-gray-700">Logout</span>
      </Menu.Item>
    </Menu>
  );

  return (
    <Dropdown overlay={menu} placement="bottomRight" trigger={["click"]}>
      <Button 
        type="text" 
        className="relative h-8 w-8 p-0 flex items-center justify-center rounded-full hover:bg-gray-100 transition-colors"
      >
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