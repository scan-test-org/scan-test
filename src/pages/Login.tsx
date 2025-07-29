import React, { useEffect, useState } from "react";
import { useLocation, Link, useNavigate } from "react-router-dom";
import api from "../lib/api";
import aliyunIcon from "../assets/aliyun.png";
import githubIcon from "../assets/github.png";
import googleIcon from "../assets/google.png";
import { Form, Input, Button, Alert, Divider } from "antd";

const oidcIcons: Record<string, React.ReactNode> = {
  google: <img src={googleIcon} alt="Google" className="w-5 h-5 mr-2" />,
  github: <img src={githubIcon} alt="GitHub" className="w-5 h-5 mr-2" />,
  aliyun: <img src={aliyunIcon} alt="Aliyun" className="w-5 h-5 mr-2" />,
};

const Login: React.FC = () => {
  const [providers, setProviders] = useState<
    { provider: string; displayName?: string }[]
  >([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const location = useLocation();
  const navigate = useNavigate();
  const searchParams = new URLSearchParams(location.search);
  const portalId = searchParams.get("portalId") || "";

  useEffect(() => {
    if (!portalId) return;
    api
      .get("/oauth2/api/oauth/providers", { params: { portalId } })
      .then((res: any) => setProviders(res.data || res))
      .catch(() => setProviders([]));
  }, [portalId]);

  // 账号密码登录
  const handlePasswordLogin = async (values: { username: string; password: string }) => {
    setLoading(true);
    setError("");
    try {
      const res = await api.post("/admins/login", {
        username: values.username,
        password: values.password,
      });
      // 登录成功后跳转到首页（token 由后端通过 Set-Cookie 设置）
      if (res && res.data) {
        // 跳转到首页
        navigate('/');
      } else {
        setError("登录失败");
      }
    } catch {
      setError("账号或密码错误");
    } finally {
      setLoading(false);
    }
  };

  // 跳转到 OIDC 授权
  const handleOidcLogin = (provider: string) => {
    const stateRaw = `LOGIN|${portalId}|${provider}`;
    const state = encodeURIComponent(stateRaw);
    window.location.href = `${api.defaults.baseURL}/oauth2/api/oauth/authorize?portalId=${portalId}&provider=${provider}&state=${state}&frontendRedirectUrl=http://${window.location.host}`;
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-white">
      <div className="bg-white p-8 rounded-xl shadow-2xl w-full max-w-md flex flex-col items-center border border-gray-100">
        {/* Logo */}
        <div className="mb-4">
          <img src="/logo.png" alt="Logo" className="w-16 h-16 mx-auto mb-4" />
        </div>
        <h2 className="text-2xl font-bold mb-6 text-gray-900 text-center">
          登录 API Portal
        </h2>
        {/* 账号密码登录表单 */}
        <Form
          className="w-full flex flex-col gap-4"
          layout="vertical"
          onFinish={handlePasswordLogin}
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: "请输入账号" }]}
          >
            <Input
              placeholder="账号"
              autoComplete="username"
              size="large"
            />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[{ required: true, message: "请输入密码" }]}
          >
            <Input.Password
              placeholder="密码"
              autoComplete="current-password"
              size="large"
            />
          </Form.Item>
          {error && (
            <Alert message={error} type="error" showIcon className="mb-2" />
          )}
          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              className="w-full"
              loading={loading}
              size="large"
            >
              登录
            </Button>
          </Form.Item>
        </Form>
        {/* 分隔线 */}
        {/* <Divider plain>或</Divider> */}
        {/* OIDC 登录按钮 */}
        {/* <div className="w-full flex flex-col gap-3">
          {providers.length === 0 ? (
            <div className="text-gray-400 text-center">暂无可用第三方登录</div>
          ) : (
            providers.map((provider) => (
              <Button
                key={provider.provider}
                onClick={() => handleOidcLogin(provider.provider)}
                className="w-full flex items-center justify-center gap-2"
                icon={oidcIcons[provider.provider.toLowerCase()] || <span className="mr-2">🔑</span>}
                size="large"
              >
                使用{provider.displayName || provider.provider}登录
              </Button>
            ))
          )}
        </div> */}
        {/* 底部提示 */}
        <div className="mt-6 text-gray-400 text-sm text-center w-full">
          没有账号？
          <Link to="/register" className="text-indigo-500 hover:underline ml-1">
            注册
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
