import React, { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { message, Spin } from "antd";
import api from "../lib/api";

const Callback: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const code = searchParams.get("code");
    const state = searchParams.get("state");

    if (!code || !state) {
      message.error("缺少 code 或 state 参数");
      return;
    }

    // window.location.href = `${api.defaults.baseURL}/developers/callback?code=${code}&state=${state}`;

    // 调用后端 callback
    api
      .post("/developers/token", { code, state })
      .then((res: any) => {
        if (res && res.data && res.data.token) {
          message.success("登录成功！");
          // 跳转首页并带上 token
          window.location.href = `/?token=${res.data.token}`;
        } else {
          message.error("登录失败，未获取到 token");
        }
      })
      .catch(() => {
        message.error("登录失败，请重试");
      });
  }, [location.search]);

  return (
    <div className="flex items-center justify-center min-h-screen">
      <Spin tip="登录中，请稍候..." />
    </div>
  );
};

export default Callback;