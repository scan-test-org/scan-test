import { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import { Layout } from "../components/Layout";
import { Spin, Alert, Tabs } from "antd";
import api from "../lib/api";
import { ConsumerBasicInfo, CredentialManager, SubscriptionManager } from "../components/consumer";
import type { Consumer, Credential, Subscription, ConsumerCredentialResult } from "../types/consumer";

function ConsumerDetailPage() {
  const { consumerId } = useParams();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [consumer, setConsumer] = useState<Consumer | null>(null);
  const [credentials, setCredentials] = useState<Credential[]>([]);
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [activeTab, setActiveTab] = useState('basic');

  const fetchConsumerDetail = useCallback(async () => {
    if (!consumerId) return;
    try {
      const response: any = await api.get(`/consumers/${consumerId}`);
      if (response?.code === "SUCCESS" && response?.data) {
        setConsumer(response.data);
      }
    } catch (error) {
      console.error('获取消费者详情失败:', error);
      setError('加载失败，请稍后重试');
    }
  }, [consumerId]);

  const fetchCredentials = useCallback(async () => {
    if (!consumerId) return;
    try {
      const response: any = await api.get(`/consumers/${consumerId}/credentials`);
      if (response?.code === "SUCCESS" && response?.data) {
        const credentialData: ConsumerCredentialResult = response.data;
        const credentialsList: Credential[] = [];
        
        // 处理API Key凭证
        if (credentialData.apiKeyConfig?.credentials) {
          credentialData.apiKeyConfig.credentials.forEach(cred => {
            credentialsList.push({
              id: cred.id,
              type: 'API_KEY',
              apiKey: cred.key,
              createAt: cred.createAt
            });
          });
        }
        
        // 处理HMAC凭证
        if (credentialData.hmacConfig?.credentials) {
          credentialData.hmacConfig.credentials.forEach(cred => {
            credentialsList.push({
              id: cred.id,
              type: 'HMAC',
              accessKey: cred.accessKey,
              secretKey: cred.secretKey,
              createAt: cred.createAt
            });
          });
        }
        
        setCredentials(credentialsList);
      } else {
        // 如果没有凭证数据，设置为空数组，确保页面正常渲染
        setCredentials([]);
      }
    } catch (error) {
      console.error('获取凭证失败:', error);
      // 即使获取失败，也设置为空数组，确保页面正常渲染
      setCredentials([]);
    }
  }, [consumerId]);

  const fetchSubscriptions = useCallback(async (searchParams?: { productName: string; status: string }) => {
    if (!consumerId) return;
    try {
      const params = {
        page: 0,
        size: 100,
        ...searchParams
      };
      const response: any = await api.get(`/consumers/${consumerId}/subscriptions`, { params });
      if (response?.code === "SUCCESS" && response.data) {
        setSubscriptions(response.data.content || []);
      } else {
        setSubscriptions([]);
      }
    } catch (error) {
      console.error('获取订阅列表失败:', error);
      setSubscriptions([]);
    }
  }, [consumerId]);

  useEffect(() => {
    if (!consumerId) return;
    setLoading(true);
    Promise.all([
      fetchConsumerDetail(),
      fetchCredentials(),
      fetchSubscriptions()
    ]).finally(() => {
      setLoading(false);
    });
  }, [consumerId, fetchConsumerDetail, fetchCredentials, fetchSubscriptions]);

  if (loading) {
    return (
      <Layout>
        <div className="flex justify-center items-center min-h-[300px]">
          <Spin size="large" />
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <Alert message={error} type="error" showIcon className="my-8" />
      </Layout>
    );
  }

  if (!consumer) {
    return (
      <Layout>
        <Alert message="未找到消费者信息" type="warning" showIcon className="my-8" />
      </Layout>
    );
  }

      return (
      <Layout>
        <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <Tabs.TabPane tab="基本信息" key="basic">
          <ConsumerBasicInfo consumer={consumer} />
          <div className="mt-6">
            <CredentialManager 
              consumerId={consumerId!}
              credentials={credentials}
              onCredentialsChange={fetchCredentials}
            />
          </div>
        </Tabs.TabPane>

        <Tabs.TabPane tab="消费者授权" key="authorization">
          <SubscriptionManager 
            consumerId={consumerId!}
            subscriptions={subscriptions}
            onSubscriptionsChange={fetchSubscriptions}
          />
        </Tabs.TabPane>
      </Tabs>
    </Layout>
  );
}

export default ConsumerDetailPage; 