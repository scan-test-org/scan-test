import { useState, useEffect } from "react";
import { Button, Avatar, Spin } from "antd";
import { UserOutlined } from "@ant-design/icons";
import api from "../lib/api";
import { getTokenFromCookie } from "../lib/utils";
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
    const token = getTokenFromCookie();
    if (token) {
      setLoadingUser(true);
      api.post("/developers/list-identities")
        .then((response) => {
          const data = response.data as { displayName: string; rawInfoJson: string }[];
          if (Array.isArray(data) && data.length > 0) {
            const info = data[0];
            let raw: { picture?: string } = {};
            try {
              raw = JSON.parse(info.rawInfoJson);
            } catch {
              // 忽略解析错误
            }
            setUserInfo({
              displayName: info.displayName,
              avatar: raw.picture,
            });
          }
        })
        .finally(() => setLoadingUser(false));
    }
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