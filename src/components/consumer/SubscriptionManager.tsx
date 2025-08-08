import { useState } from "react";
import {
  Card,
  Button,
  message,
  Input,
  Drawer,
  Table,
  Badge,
  Popconfirm,
  Select,
} from "antd";
import {
  PlusOutlined,
} from "@ant-design/icons";
import api from "../../lib/api";
import type { Subscription } from "../../types/consumer";
import type { Product } from "../../types";

interface SubscriptionManagerProps {
  consumerId: string;
  subscriptions: Subscription[];
  onSubscriptionsChange: (searchParams?: { productName: string; status: string }) => void;
}

export function SubscriptionManager({ consumerId, subscriptions, onSubscriptionsChange }: SubscriptionManagerProps) {
  const [productDrawerVisible, setProductDrawerVisible] = useState(false);
  const [products, setProducts] = useState<Product[]>([]);
  const [productLoading, setProductLoading] = useState(false);
  const [selectedProducts, setSelectedProducts] = useState<string[]>([]);
  const [subscriptionSearch, setSubscriptionSearch] = useState({ productName: '', status: '' as 'PENDING' | 'APPROVED' | '' });

  const openProductDrawer = async () => {
    setProductDrawerVisible(true);
    setProductLoading(true);
    try {
      const response = await api.get("/products?page=0&size=100");
      if (response.data?.code === "SUCCESS" && response.data?.data) {
        setProducts(response.data.data.content || []);
      }
    } catch (error) {
      console.error('获取产品列表失败:', error);
      message.error('获取产品列表失败');
    } finally {
      setProductLoading(false);
    }
  };

  const handleSubscribeProducts = async () => {
    if (selectedProducts.length === 0) {
      message.warning('请选择要订阅的产品');
      return;
    }

    try {
      for (const productId of selectedProducts) {
        await api.post(`/consumers/${consumerId}/subscriptions`, { productId });
      }
      message.success('订阅成功');
      setProductDrawerVisible(false);
      setSelectedProducts([]);
      onSubscriptionsChange();
    } catch (error) {
      console.error('订阅失败:', error);
      message.error('订阅失败');
    }
  };

  const handleUnsubscribe = async (productId: string) => {
    try {
      await api.delete(`/consumers/${consumerId}/subscriptions/${productId}`);
      message.success('取消订阅成功');
      onSubscriptionsChange();
    } catch (error) {
      console.error('取消订阅失败:', error);
      message.error('取消订阅失败');
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'PENDING':
        return '待审批';
      case 'APPROVED':
        return '已通过';
      default:
        return status;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'orange';
      case 'APPROVED':
        return 'green';
      default:
        return 'default';
    }
  };

  const subscriptionColumns = [
    {
      title: '产品名称',
      dataIndex: 'product',
      key: 'productName',
      render: (product: Product) => product?.name || '-',
    },
    {
      title: '产品类型',
      dataIndex: 'product',
      key: 'productType',
      render: (product: Product) => {
        const typeMap = {
          'REST_API': 'REST API',
          'HTTP_API': 'HTTP API',
          'MCP_SERVER': 'MCP Server'
        };
        return typeMap[product?.type as keyof typeof typeMap] || product?.type || '-';
      }
    },
    {
      title: '订阅状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Badge status={getStatusColor(status) as 'success' | 'processing' | 'error' | 'default' | 'warning'} text={getStatusText(status)} />
      ),
    },
    {
      title: '订阅时间',
      dataIndex: 'createAt',
      key: 'createAt',
      render: (date: string) => date ? new Date(date).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      render: (record: Subscription) => (
        <Popconfirm
          title="确定要取消订阅吗？"
          onConfirm={() => handleUnsubscribe(record.productId)}
        >
          <Button type="link" danger size="small">
            取消订阅
          </Button>
        </Popconfirm>
      ),
    },
  ];

  const productColumns = [
    {
      title: '产品名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '产品类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => {
        const typeMap = {
          'REST_API': 'REST API',
          'HTTP_API': 'HTTP API',
          'MCP_SERVER': 'MCP Server'
        };
        return typeMap[type as keyof typeof typeMap] || type;
      }
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
  ];

  return (
    <>
      <Card>
        <div className="mb-4 flex justify-between items-center">
          <div className="flex space-x-4">
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={openProductDrawer}
            >
              授权
            </Button>
            <Input.Search
              placeholder="请输入API名称进行搜索"
              style={{ width: 300 }}
              onSearch={(value) => {
                const newSearch = { ...subscriptionSearch, productName: value };
                setSubscriptionSearch(newSearch);
                onSubscriptionsChange(newSearch);
              }}
            />
            <Select
              placeholder="订阅状态"
              style={{ width: 120 }}
              allowClear
              value={subscriptionSearch.status || undefined}
              onChange={(value) => {
                const newSearch = { ...subscriptionSearch, status: value as 'PENDING' | 'APPROVED' | '' };
                setSubscriptionSearch(newSearch);
                onSubscriptionsChange(newSearch);
              }}
            >
              <Select.Option value="PENDING">待审批</Select.Option>
              <Select.Option value="APPROVED">已通过</Select.Option>
            </Select>
          </div>
        </div>
        <Table
          columns={subscriptionColumns}
          dataSource={subscriptions}
          rowKey={(record) => record.productId}
          pagination={false}
          size="small"
          locale={{ emptyText: '暂无订阅记录，请点击上方按钮进行授权' }}
        />
      </Card>

      {/* 产品选择抽屉 */}
      <Drawer
        title="选择要授权的产品"
        placement="right"
        width={600}
        open={productDrawerVisible}
        onClose={() => {
          setProductDrawerVisible(false);
          setSelectedProducts([]);
        }}
        footer={
          <div className="flex justify-end space-x-2">
            <Button
              onClick={() => {
                setProductDrawerVisible(false);
                setSelectedProducts([]);
              }}
            >
              取消
            </Button>
            <Button
              type="primary"
              onClick={handleSubscribeProducts}
              disabled={selectedProducts.length === 0}
            >
              确定授权 ({selectedProducts.length})
            </Button>
          </div>
        }
      >
        <Table
          columns={productColumns}
          dataSource={products}
          rowKey={(record) => record.productId}
          loading={productLoading}
          rowSelection={{
            selectedRowKeys: selectedProducts,
            onChange: (selectedRowKeys) => setSelectedProducts(selectedRowKeys as string[]),
          }}
          pagination={false}
          size="small"
          locale={{ emptyText: '没有数据' }}
        />
      </Drawer>
    </>
  );
} 