import { useState, useEffect } from "react";
import { Button, Avatar, Spin } from "antd";
import { UserOutlined } from "@ant-design/icons";
import api from "../lib/api";
import { useNavigate } from "react-router-dom";

interface UserInfo {
  displayName: string;
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
              avatar: data.avatarUrl || undefined,
            });
          }
        })
        .finally(() => setLoadingUser(false));
  }, []);

  if (loadingUser) {
    return <Spin size="small" />;
  }
  if (userInfo) {
    return (
      <div className="flex items-center space-x-2 cursor-pointer" onClick={() => {
        navigate('/profile');
      }}>
        <Avatar src={userInfo.avatar} icon={<UserOutlined />} size="small" />
        <span>{userInfo.displayName}</span>
      </div>
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