import { useState, useEffect } from 'react'
import { Modal, Form, Input, Select, message } from 'antd'
import { apiProductApi } from '@/lib/api'
import type { ApiProduct } from '@/types/api-product'

interface ApiProductFormModalProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
  productId?: string // 如果有productId，则为编辑模式
  initialData?: Partial<ApiProduct> // 编辑时的初始数据
}

export default function ApiProductFormModal({
  visible,
  onCancel,
  onSuccess,
  productId,
  initialData
}: ApiProductFormModalProps) {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const isEditMode = !!productId

  // 当弹窗打开时，如果是编辑模式则加载数据
  useEffect(() => {
    if (visible && isEditMode && initialData) {
      form.setFieldsValue(initialData)
    }
  }, [visible, isEditMode, initialData, form])

  // 重置表单
  const resetForm = () => {
    form.resetFields()
  }

  // 处理取消
  const handleCancel = () => {
    resetForm()
    onCancel()
  }

  // 处理提交
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      setLoading(true)

      if (isEditMode) {
        // 编辑模式
        await apiProductApi.updateApiProduct(productId!, values)
        message.success('API Product 更新成功')
      } else {
        // 创建模式
        await apiProductApi.createApiProduct(values)
        message.success('API Product 创建成功')
      }

      resetForm()
      onSuccess()
    } catch (error: any) {
      if (error?.errorFields) {
        // 表单验证错误，不需要显示错误消息
        return
      }
      message.error(error?.message || (isEditMode ? '更新失败' : '创建失败'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal
      title={isEditMode ? '编辑 API Product' : '创建 API Product'}
      open={visible}
      onOk={handleSubmit}
      onCancel={handleCancel}
      confirmLoading={loading}
      destroyOnClose
      width={600}
    >
      <Form
        form={form}
        layout="vertical"
        preserve={false}
      >
        <Form.Item
          label="名称"
          name="name"
          rules={[{ required: true, message: '请输入API Product名称' }]}
        >
          <Input placeholder="请输入API Product名称" />
        </Form.Item>
        
        <Form.Item
          label="描述"
          name="description"
          rules={[{ required: true, message: '请输入描述' }]}
        >
          <Input.TextArea placeholder="请输入描述" rows={3} />
        </Form.Item>
        
        <Form.Item
          label="类型"
          name="type"
          rules={[{ required: true, message: '请选择类型' }]}
        >
          <Select placeholder="请选择类型">
            <Select.Option value="REST_API">REST API</Select.Option>
            <Select.Option value="MCP_SERVER">MCP Server</Select.Option>
          </Select>
        </Form.Item>
        
        {/* <Form.Item
          label="分类"
          name="category"
        >
          <Input placeholder="请输入分类" />
        </Form.Item> */}
      </Form>
    </Modal>
  )
} 