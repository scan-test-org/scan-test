import { Card, Table, Button, Space, Typography, Input, Avatar } from "antd";
import { SearchOutlined, DeleteOutlined } from "@ant-design/icons";
import { Layout } from "../components/Layout";
import { useEffect, useState, useCallback } from "react";
import { getConsumers, deleteConsumer, createConsumer } from "../lib/api";
import { message, Modal } from "antd";

const { Title, Paragraph } = Typography;
const { Search } = Input;

interface Consumer {
  consumerId: string;
  name: string;
  description?: string;
  status?: string;
  plan?: string;
  createAt?: string;
  // 你可以根据接口补充更多字段
}

function ConsumersPage() {
  const [consumers, setConsumers] = useState<Consumer[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [searchName, setSearchName] = useState("");
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [addLoading, setAddLoading] = useState(false);
  const [addForm, setAddForm] = useState({ name: '', description: '' });

  const fetchConsumers = useCallback(async () => {
    setLoading(true);
    try {
      const res = await getConsumers(
        { name: searchName },
        { page: page - 1, size: pageSize }
      );
      setConsumers(res.data?.content || []);
      setTotal(res.data?.totalElements || 0);
    } catch (e) {
      message.error("获取消费者列表失败");
    } finally {
      setLoading(false);
    }
  }, [page, pageSize, searchName]);

  useEffect(() => {
    fetchConsumers();
  }, [fetchConsumers]);

  const handleDelete = (record: Consumer) => {
    Modal.confirm({
      title: `确定要删除消费者「${record.name}」吗？`,
      onOk: async () => {
        try {
          await deleteConsumer(record.consumerId);
          message.success("删除成功");
          fetchConsumers();
        } catch {
          message.error("删除失败");
        }
      },
    });
  };

  const handleAdd = async () => {
    if (!addForm.name.trim()) {
      message.warning('请输入消费者名称');
      return;
    }
    setAddLoading(true);
    try {
      await createConsumer({ name: addForm.name, description: addForm.description });
      message.success('新增成功');
      setAddModalOpen(false);
      setAddForm({ name: '', description: '' });
      fetchConsumers();
    } catch {
      message.error('新增失败');
    } finally {
      setAddLoading(false);
    }
  };

  const columns = [
    {
      title: '消费者',
      dataIndex: 'name',
      key: 'name',
      render: (name: string, record: Consumer) => (
        <div className="flex items-center space-x-3">
          <Avatar className="bg-blue-500">
            {name?.charAt(0).toUpperCase()}
          </Avatar>
          <div>
            <div className="font-medium">{name}</div>
            <div className="text-xs text-gray-400">{record.description}</div>
          </div>
        </div>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createAt',
      key: 'createAt',
      render: (date: string) => date ? new Date(date).toLocaleDateString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Consumer) => (
        <Space>
          <Button type="link" danger icon={<DeleteOutlined />} onClick={() => handleDelete(record)}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <Layout>
      <div className="mb-8">
        <Title level={1} className="mb-2">
          消费者管理
        </Title>
        <Paragraph className="text-gray-600">
          管理API的消费者用户和订阅信息
        </Paragraph>
      </div>

      <Card>
        <div className="mb-4 flex gap-4">
          <Button type="primary" onClick={() => setAddModalOpen(true)}>
            新增消费者
          </Button>
          <Search
            placeholder="搜索消费者..."
            prefix={<SearchOutlined />}
            style={{ width: 300 }}
            value={searchName}
            onChange={e => setSearchName(e.target.value)}
            onSearch={() => { setPage(1); fetchConsumers(); }}
            enterButton
          />
        </div>
        <Table
          columns={columns}
          dataSource={consumers}
          rowKey="consumerId"
          loading={loading}
          pagination={{
            total,
            current: page,
            pageSize,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
            onChange: (p, ps) => {
              setPage(p);
              setPageSize(ps);
            },
          }}
        />
        <Modal
          title="新增消费者"
          open={addModalOpen}
          onCancel={() => { setAddModalOpen(false); setAddForm({ name: '', description: '' }); }}
          onOk={handleAdd}
          confirmLoading={addLoading}
          okText="提交"
          cancelText="取消"
        >
          <div style={{ marginBottom: 16 }}>
            <Input
              placeholder="消费者名称"
              value={addForm.name}
              maxLength={50}
              onChange={e => setAddForm(f => ({ ...f, name: e.target.value }))}
              disabled={addLoading}
            />
          </div>
          <div>
            <Input.TextArea
              placeholder="描述（可选）"
              value={addForm.description}
              maxLength={256}
              onChange={e => setAddForm(f => ({ ...f, description: e.target.value }))}
              disabled={addLoading}
              rows={3}
            />
          </div>
        </Modal>
      </Card>

      <Card title="消费者统计" className="mt-8">
        <div className="grid grid-cols-4 gap-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">{total}</div>
            <div className="text-sm text-gray-500">总消费者</div>
          </div>
          {/* 其他统计项可根据接口返回字段补充 */}
        </div>
      </Card>
    </Layout>
  );
}

export default ConsumersPage; 