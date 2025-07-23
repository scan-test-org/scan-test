import { useState, useEffect } from "react";
import { Modal, Button, Form, Input, Avatar, Spin } from "antd";
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

export function LoginDialog() {
  const [isOpen, setIsOpen] = useState(false);
  const [form] = Form.useForm();
  const [isLoading, setIsLoading] = useState(false);
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
        .then((data: any) => {
          if (Array.isArray(data) && data.length > 0) {
            const info = data[0];
            let raw: { picture?: string } = {};
            try {
              raw = JSON.parse(info.rawInfoJson);
            } catch (e) {
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

  const handleLogin = async (values: { username: string; password: string }) => {
    setIsLoading(true);
    // 模拟登录API调用
    await new Promise(resolve => setTimeout(resolve, 1000));
    // 这里可以添加实际的登录逻辑
    localStorage.setItem("isLoggedIn", "true");
    localStorage.setItem("user", JSON.stringify({
      username: values.username,
      name: values.username
    }));
    setIsLoading(false);
    setIsOpen(false);
    form.resetFields();
    window.location.reload();
  };
  const portalId = "test_portal";

  // 展示用户信息
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
    <>
      <Button
        icon={<UserOutlined />}
        onClick={() => {
          window.location.href = `/login?portalId=${portalId}`;
        }}
        type="default"
        size="small"
      >
        登录
      </Button>
      <Modal
        open={isOpen}
        onCancel={() => setIsOpen(false)}
        footer={null}
        title="登录到您的账户"
        className="sm:max-w-md"
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleLogin}
          className="space-y-4"
        >
          <Form.Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: "请输入用户名" }]}
          >
            <Input placeholder="请输入用户名" />
          </Form.Item>
          <Form.Item
            label="密码"
            name="password"
            rules={[{ required: true, message: "请输入密码" }]}
          >
            <Input.Password placeholder="请输入密码" />
          </Form.Item>
          <div className="flex justify-end space-x-2 pt-4">
            <Button onClick={() => setIsOpen(false)} disabled={isLoading} style={{ marginRight: 8 }}>
              取消
            </Button>
            <Button type="primary" htmlType="submit" loading={isLoading}>
              登录
            </Button>
          </div>
        </Form>
        <div className="text-center text-sm text-gray-500 mt-4">
          <p>演示账号：admin / password</p>
        </div>
      </Modal>
    </>
  );
} 