import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../lib/api";
import { Form, Input, Button, Alert } from "antd";


const Login: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  

  // 账号密码登录
  const handlePasswordLogin = async (values: { username: string; password: string }) => {
    setLoading(true);
    setError("");
    try {
      const response = await api.post("/admins/login", {
        username: values.username,
        password: values.password,
      });
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('userInfo', response.data);
      navigate('/');
    } catch {
      setError("账号或密码错误");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-white">
      <div className="bg-white p-8 rounded-xl shadow-2xl w-full max-w-md flex flex-col items-center border border-gray-100">
        {/* Logo */}
        <div className="mb-4">
          <img src="/logo.png" alt="Logo" className="w-16 h-16 mx-auto mb-4" />
        </div>
        <h2 className="text-2xl font-bold mb-6 text-gray-900 text-center">
          登录 AI开放平台
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

        {/* <div className="mt-6 text-gray-400 text-sm text-center w-full">
          没有账号？
          <Link to="/register" className="text-indigo-500 hover:underline ml-1">
            注册
          </Link>
        </div> */}
      </div>
    </div>
  );
};

export default Login;
