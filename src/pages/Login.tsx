import React, { useEffect, useState } from "react";
import { useLocation, Link } from "react-router-dom";
import api from "../lib/api";
import aliyunIcon from "../assets/aliyun.png";
import githubIcon from "../assets/github.png";
import googleIcon from "../assets/google.png";

const oidcIcons: Record<string, React.ReactNode> = {
  google: <img src={googleIcon} alt="Google" className="w-5 h-5 mr-2" />,
  github: <img src={githubIcon} alt="GitHub" className="w-6 h-6 mr-2" />,
  aliyun: <img src={aliyunIcon} alt="Aliyun" className="w-6 h-6 mr-2" />,
};

const Login: React.FC = () => {
  const [providers, setProviders] = useState<
    { provider: string; displayName?: string }[]
  >([]);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const portalId = searchParams.get("portalId") || "";

  useEffect(() => {
    if (!portalId) return;
    api
      .post("/developers/providers", { portalId })
      .then((res: any) => setProviders(res.data || res))
      .catch(() => setProviders([]));
  }, [portalId]);

  // 账号密码登录
  const handlePasswordLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    try {
      const res = await api.post("/api/developer/login", {
        username,
        password,
      });
      // 登录成功后跳转到首页并携带token
      if (res && res.data && res.data.token) {
        window.location.href = `/?token=${res.data.token}`;
      } else {
        setError("登录失败，未获取到token");
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
        <form
          className="w-full flex flex-col gap-4"
          onSubmit={handlePasswordLogin}
        >
          <input
            className="px-4 py-2 rounded bg-gray-100 text-gray-900 placeholder-gray-400 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            type="text"
            placeholder="账号"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
          />
          <input
            className="px-4 py-2 rounded bg-gray-100 text-gray-900 placeholder-gray-400 border border-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            type="password"
            placeholder="密码"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
          {error && (
            <div className="text-red-500 text-sm text-center">{error}</div>
          )}
          <button
            type="submit"
            className="w-full py-2 rounded bg-gray-900 text-white font-semibold hover:bg-gray-800 transition-colors disabled:opacity-60"
            disabled={loading}
          >
            {loading ? "登录中..." : "登录"}
          </button>
        </form>
        {/* 分隔线 */}
        <div className="flex items-center w-full my-6">
          <hr className="flex-1 border-gray-200" />
          <span className="mx-4 text-gray-400">或</span>
          <hr className="flex-1 border-gray-200" />
        </div>
        {/* OIDC 登录按钮 */}
        <div className="w-full flex flex-col gap-3">
          {providers.length === 0 ? (
            <div className="text-gray-400 text-center">暂无可用第三方登录</div>
          ) : (
            providers.map((provider) => (
              <button
                key={provider.provider}
                onClick={() => handleOidcLogin(provider.provider)}
                className="w-full flex items-center justify-center gap-2 py-2 rounded bg-white border border-gray-200 text-gray-900 hover:bg-gray-50 transition-colors text-base font-medium shadow-sm"
              >
                {oidcIcons[provider.provider.toLowerCase()] || (
                  <span className="mr-2">🔑</span>
                )}
                使用{provider.displayName || provider.provider}登录
              </button>
            ))
          )}
        </div>
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
