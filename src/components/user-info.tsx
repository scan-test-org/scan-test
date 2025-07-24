import { useState, useEffect } from "react";
import { Button, Avatar, Spin } from "antd";
import { UserOutlined } from "@ant-design/icons";
import api from "../lib/api";

function getTokenFromUrl() {
  const params = new URLSearchParams(window.location.search);
  return params.get("token");
}

interface UserInfo {
  displayName: string;
  avatar?: string;
}

export function UserInfo() {
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null);
  const [loadingUser, setLoadingUser] = useState(false);

  useEffect(() => {
    const token = getTokenFromUrl();
    if (token) {
      setLoadingUser(true);
      api.get("/oauth2/list-identities", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
        .then((data: { displayName: string; rawInfoJson: string }[]) => {
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

  const portalId = "test_portal";

  if (loadingUser) {
    return <Spin size="small" />;
  }
  if (userInfo) {
    return (
      <div className="flex items-center space-x-2 cursor-pointer" onClick={() => {
        window.location.href = `/profile?portalId=${portalId}&token=${getTokenFromUrl()}`;
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
        window.location.href = `/login?portalId=${portalId}`;
      }}
      type="default"
    >
      登录
    </Button>
  );
} 