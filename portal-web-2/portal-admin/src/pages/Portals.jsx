import React, { useState, useCallback, memo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Card, Badge, Avatar, Dropdown, Menu, Modal, Form, Input } from 'antd';
import { PlusOutlined, LinkOutlined, MoreOutlined } from '@ant-design/icons';

const mockPortals = [
  {
    id: "1",
    name: "test",
    description: "测试公司门户",
    title: "Company",
    url: "https://3995a4355203.us.kongportals.com",
    userAuth: "Konnect Built-in",
    rbac: "Disabled",
    authStrategy: "key-auth",
    apiVisibility: "Private",
    pageVisibility: "Private",
    logo: undefined
  }
];

const PortalCard = memo(({ portal, onNavigate, onEdit, onCopy, onDelete }) => {
  const menu = (
    <Menu>
      <Menu.Item key="edit" onClick={e => { e.domEvent.stopPropagation(); onEdit(portal); }}>编辑</Menu.Item>
      <Menu.Item key="copy" onClick={e => { e.domEvent.stopPropagation(); onCopy(portal); }}>复制</Menu.Item>
      <Menu.Divider />
      <Menu.Item key="delete" danger onClick={e => { e.domEvent.stopPropagation(); onDelete(portal); }}>删除</Menu.Item>
    </Menu>
  );
  return (
    <Card
      hoverable
      className="mb-4 cursor-pointer transition-all hover:shadow-lg hover:scale-[1.02] border-2 hover:border-blue-300 p-4"
      onClick={() => onNavigate(portal.id)}
    >
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-3">
          <Avatar className="bg-blue-500 mr-2" src={portal.logo} style={{ backgroundColor: undefined }}>
            {portal.title.charAt(0).toUpperCase()}
          </Avatar>
          <span className="font-semibold text-lg">{portal.title}</span>
        </div>
        <Dropdown overlay={menu} trigger={["click"]} onClick={e => e.stopPropagation()}>
          <Button type="text" icon={<MoreOutlined />} className="!p-0 !h-8 !w-8 flex items-center justify-center" />
        </Dropdown>
      </div>
      <div className="mb-2">
        <div className="font-medium">{portal.name}</div>
        <div className="text-gray-500 text-sm flex items-center gap-1">
          <LinkOutlined className="text-xs" />
          <a href={portal.url} target="_blank" rel="noopener noreferrer" onClick={e => e.stopPropagation()} className="hover:underline text-blue-600">{portal.url}</a>
        </div>
      </div>
      <div className="mb-2 space-y-1">
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">User authentication</span>
          <span className="text-blue-600">{portal.userAuth}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">RBAC</span>
          <Badge color="#d9d9d9" text={portal.rbac} className="bg-gray-200" />
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">Authentication strategy</span>
          <span className="text-blue-600">{portal.authStrategy}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">Default API visibility</span>
          <Badge color="#e6f7ff" text={portal.apiVisibility} className="bg-blue-50" />
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-gray-500">Default page visibility</span>
          <Badge color="#e6f7ff" text={portal.pageVisibility} className="bg-blue-50" />
        </div>
      </div>
      <div className="text-xs text-gray-400 text-center pt-2 border-t">点击查看详情</div>
    </Card>
  );
});
PortalCard.displayName = 'PortalCard';

const Portals = () => {
  const navigate = useNavigate();
  const [portals, setPortals] = useState(mockPortals);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [form] = Form.useForm();

  // 编辑、复制、删除功能可后续补充
  const handleEdit = (portal) => {};
  const handleCopy = (portal) => {};
  const handleDelete = (portal) => {
    Modal.confirm({
      title: '确认删除该Portal？',
      content: portal.title,
      okText: '删除',
      okType: 'danger',
      cancelText: '取消',
      onOk: () => setPortals(portals.filter(p => p.id !== portal.id)),
    });
  };

  const handleCreatePortal = (values) => {
    const portal = { ...values, id: Date.now().toString() };
    setPortals([...portals, portal]);
    setShowCreateDialog(false);
    form.resetFields();
  };

  const handlePortalClick = useCallback((portalId) => {
    navigate(`/portals/detail?id=${portalId}`);
  }, [navigate]);

  return (
    <div className="max-w-7xl mx-auto p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Portal</h1>
          <div className="text-gray-500 mt-2">管理和配置您的开发者门户</div>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setShowCreateDialog(true)} className="flex items-center">
          创建 Portal
        </Button>
      </div>
      <div className="grid grid-cols-3 gap-6">
        {portals.map((portal) => (
          <PortalCard
            key={portal.id}
            portal={portal}
            onNavigate={handlePortalClick}
            onEdit={handleEdit}
            onCopy={handleCopy}
            onDelete={handleDelete}
          />
        ))}
      </div>
      <Modal
        title={<span className="text-lg font-bold">创建 Portal</span>}
        open={showCreateDialog}
        onCancel={() => setShowCreateDialog(false)}
        onOk={() => form.submit()}
        okText="提交"
        cancelText="取消"
        className="!top-32"
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreatePortal}
          initialValues={{
            name: '',
            description: '',
            title: '',
            url: '',
            userAuth: 'Konnect Built-in',
            rbac: 'Disabled',
            authStrategy: 'key-auth',
            apiVisibility: 'Private',
            pageVisibility: 'Private',
            logo: undefined
          }}
        >
          <Form.Item label={<span className="font-medium">名称</span>} name="name" rules={[{ required: true, message: '请输入名称' }]}
            className="mb-4">
            <Input className="rounded" />
          </Form.Item>
          <Form.Item label={<span className="font-medium">标题</span>} name="title" rules={[{ required: true, message: '请输入标题' }]}
            className="mb-4">
            <Input className="rounded" />
          </Form.Item>
          <Form.Item label={<span className="font-medium">描述</span>} name="description" className="mb-4">
            <Input.TextArea rows={2} className="rounded" />
          </Form.Item>
          <Form.Item label={<span className="font-medium">URL</span>} name="url" rules={[{ required: true, message: '请输入URL' }]}
            className="mb-4">
            <Input className="rounded" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default Portals; 
// export default Portals;  