import React, { useEffect, useState } from "react";
import { useLocation, Link } from "react-router-dom";
import { Form, Input, Button, Card, Divider, message } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import api from "../lib/api";
import aliyunIcon from "../assets/aliyun.png";
import githubIcon from "../assets/github.png";
import googleIcon from "../assets/google.png";

interface Provider {
  provider: string;
  displayName?: string;
}

const oidcIcons: Record<string, React.ReactNode> = {
  google: <img src={googleIcon} alt="Google" className="w-5 h-5 mr-2" />,
  github: <img src={githubIcon} alt="GitHub" className="w-6 h-6 mr-2" />,
  aliyun: <img src={aliyunIcon} alt="Aliyun" className="w-6 h-6 mr-2" />,
};

const Login: React.FC = () => {
  const [providers, setProviders] = useState<Provider[]>([]);
  const [loading, setLoading] = useState(false);
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const portalId = searchParams.get("portalId") || "";

  useEffect(() => {
    if (!portalId) return;
    api
      .post("/developers/providers", { portalId })
      .then((res: { data?: Provider[] }) => setProviders(res.data || []))
      .catch(() => setProviders([]));
  }, [portalId]);

  // 账号密码登录
  const handlePasswordLogin = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      const res = await api.post("/api/developer/login", {
        username: values.username,
        password: values.password,
      });
      // 登录成功后跳转到首页并携带token
      if (res && res.data && res.data.token) {
        message.success('登录成功！');
        window.location.href = `/?token=${res.data.token}`;
      } else {
        message.error("登录失败，未获取到token");
      }
    } catch {
      message.error("账号或密码错误");
    } finally {
      setLoading(false);
    }
  };

  // 跳转到 OIDC 授权
  const handleOidcLogin = (provider: string) => {
    const stateRaw = `LOGIN|${portalId}|${provider}`;
    const state = encodeURIComponent(stateRaw);
    window.location.href = `${api.defaults.baseURL}/developers/authorize?portalId=${portalId}&provider=${provider}&state=${state}`;
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-50">
      <Card className="w-full max-w-md shadow-lg">
        {/* Logo */}
        <div className="text-center mb-6">
          <img src="/logo.png" alt="Logo" className="w-16 h-16 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-gray-900">登录 API Portal</h2>
        </div>

        {/* 账号密码登录表单 */}
        <Form
          name="login"
          onFinish={handlePasswordLogin}
          autoComplete="off"
          layout="vertical"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入账号' }
            ]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="账号"
              autoComplete="username"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码' }
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
              autoComplete="current-password"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              className="w-full"
              size="large"
            >
              {loading ? "登录中..." : "登录"}
            </Button>
          </Form.Item>
        </Form>

        {/* 分隔线 */}
        <Divider plain>或</Divider>

        {/* OIDC 登录按钮 */}
        <div className="flex flex-col gap-3">
          {providers.length === 0 ? (
            <div className="text-gray-400 text-center">暂无可用第三方登录</div>
          ) : (
            providers.map((provider) => (
              <Button
                key={provider.provider}
                onClick={() => handleOidcLogin(provider.provider)}
                className="w-full flex items-center justify-center"
                size="large"
                icon={oidcIcons[provider.provider.toLowerCase()] || <span>🔑</span>}
              >
                使用{provider.displayName || provider.provider}登录
              </Button>
            ))
          )}
        </div>

        {/* 底部提示 */}
        <div className="mt-6 text-center text-gray-500">
          没有账号？
          <Link to="/register" className="text-blue-500 hover:underline ml-1">
            注册
          </Link>
        </div>
      </Card>
    </div>
  );
};

export default Login;
