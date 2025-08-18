import React, { useEffect } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import Layout from './Layout';
import { useLoading } from '@/contexts/LoadingContext';

const LayoutWrapper: React.FC = () => {
  const { loading, setLoading } = useLoading();
  const location = useLocation();

  // 监听路由变化，设置 loading 状态
  useEffect(() => {
    setLoading(true);
    // 模拟加载延迟
    const timer = setTimeout(() => {
      setLoading(false);
    }, 500);

    return () => clearTimeout(timer);
  }, [location.pathname, setLoading]);

  return (
    <Layout loading={loading}>
      <Outlet />
    </Layout>
  );
};

export default LayoutWrapper;
