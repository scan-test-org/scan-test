import { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { MenuProps } from 'antd';
import { Badge, Button, Card, Col, Dropdown, Modal, Row, Select, Statistic, Form, Input, message, Pagination } from 'antd';
import type { ApiProduct } from '@/types/api-product';
import { ApiOutlined, ClockCircleOutlined, MoreOutlined, PlusOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { apiProductApi } from '@/lib/api';
import { getStatusBadgeVariant } from '@/lib/utils';
import ApiProductFormModal from '@/components/api-product/ApiProductFormModal';
import { AdvancedSearch, SearchParam } from '@/components/common/AdvancedSearch';

// 优化的产品卡片组件
const ProductCard = memo(({ product, onNavigate, handleRefresh, onEdit }: {
  product: ApiProduct;
  onNavigate: (productId: string) => void;
  handleRefresh: () => void;
  onEdit: (product: ApiProduct) => void;
}) => {
  const getTypeIcon = (type: string) => {
    return type === "REST_API" ? <ApiOutlined className="h-4 w-4" /> : <ClockCircleOutlined className="h-4 w-4" />
  }

  const getTypeBadgeVariant = (type: string) => {
    return type === "REST_API" ? "blue" : "purple"
  }

  const handleClick = useCallback((e: React.MouseEvent<HTMLDivElement>) => {
    onNavigate(product.productId)
  }, [product.productId, onNavigate]);

  const handleDelete = useCallback((productId: string, productName: string, e?: React.MouseEvent | any) => {
    if (e && e.stopPropagation) e.stopPropagation();
    Modal.confirm({
      title: '确认删除',
      icon: <ExclamationCircleOutlined />,
      content: `确定要删除API产品 "${productName}" 吗？此操作不可恢复。`,
      okText: '确认删除',
      okType: 'danger',
      cancelText: '取消',
      onOk() {
        apiProductApi.deleteApiProduct(productId).then((res: any) => {
          message.success('API Product 删除成功');
          handleRefresh();
        });
      },
    });
  }, [handleRefresh]);

  const handleEdit = useCallback((e?: React.MouseEvent | any) => {
    if (e && e?.domEvent?.stopPropagation) e.domEvent.stopPropagation();
    onEdit(product);
  }, [product, onEdit]);

  const dropdownItems: MenuProps['items'] = [
    {
      key: 'edit',
      label: '编辑',
      onClick: handleEdit,
    },
    {
      type: 'divider',
    },
    {
      key: 'delete',
      label: '删除',
      danger: true,
      onClick: (info: any) => handleDelete(product.productId, product.name, info?.domEvent),
    },
  ]

  return (
    <Card
      className="hover:shadow-lg transition-shadow cursor-pointer"
      onClick={handleClick}
      bodyStyle={{ padding: '16px' }}
    >
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-blue-100">
            {getTypeIcon(product.type)}
          </div>
          <div>
            <h3 className="text-lg font-semibold">{product.name}</h3>
            <div className="flex items-center gap-2 mt-1 flex-wrap">
              {product.category && <Badge color="green" text={product.category} />}
              <Badge color={getTypeBadgeVariant(product.type)} text={product.type === "REST_API" ? "REST API" : "MCP Server"} />
              <Badge color={getStatusBadgeVariant(product.status)} text={product.status === "PENDING" ? "待配置" : product.status === "READY" ? "待发布" : "已发布"} />
            </div>
          </div>
        </div>
        <Dropdown menu={{ items: dropdownItems }} trigger={['click']}>
          <Button
            type="text"
            icon={<MoreOutlined />}
            onClick={(e) => e.stopPropagation()}
          />
        </Dropdown>
      </div>

      <div className="space-y-4">
        {product.description && (
          <p className="text-sm text-gray-600">{product.description}</p>
        )}

        <div className="space-y-2 text-sm">

          <div className="flex justify-between">
            <span className="text-gray-500">Product ID</span>
            <span>{product.productId}</span>
          </div>
        </div>
      </div>
    </Card>
  )
})

ProductCard.displayName = 'ProductCard'

export default function ApiProducts() {
  const navigate = useNavigate();
  const [apiProducts, setApiProducts] = useState<ApiProduct[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>('All');
  const [selectedType, setSelectedType] = useState<string>('All');
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 12,
    total: 0,
  });

  const [modalVisible, setModalVisible] = useState(false);
  const [editingProduct, setEditingProduct] = useState<ApiProduct | null>(null);

  const fetchApiProducts = useCallback((page = 0, size = 12) => {
    setLoading(true);
    apiProductApi.getApiProducts({ page, size }).then((res: any) => {
      setApiProducts(res.data.content);
      setPagination({
        current: page + 1,
        pageSize: size,
        total: res.data.totalElements || 0,
      });
    }).finally(() => {
      setLoading(false);
    });
  }, []);

  useEffect(() => {
    fetchApiProducts(0, 12);
  }, [fetchApiProducts]);

  // 使用useMemo优化数据计算
  const categories = useMemo(() =>
    ["All", ...Array.from(new Set(apiProducts?.map(product => product.category)))],
    [apiProducts]
  )

  const types = useMemo(() =>
    ["All", "REST API", "MCP Server"],
    []
  )

  // 高级搜索配置
  const searchParamsList: SearchParam[] = useMemo(() => [
    {
      label: '产品名称',
      name: 'name',
      placeholder: '请输入产品名称',
      type: 'input'
    },
    {
      label: '产品分类',
      name: 'category',
      placeholder: '选择分类',
      type: 'select',
      optionList: categories.filter(cat => cat !== 'All').map(cat => ({
        label: cat,
        value: cat
      }))
    },
    {
      label: '产品类型',
      name: 'type',
      placeholder: '选择类型',
      type: 'select',
      optionList: types.filter(type => type !== 'All').map(type => ({
        label: type === 'REST API' ? 'REST API' : 'MCP Server',
        value: type === 'REST API' ? 'REST_API' : 'MCP_SERVER'
      }))
    }
  ], [categories, types]);

  // 搜索处理函数
  const handleSearch = (searchName: string, searchValue: string) => {
    // 根据搜索条件过滤产品
    if (searchName === 'name') {
      // 名称搜索逻辑
      const filtered = apiProducts.filter(product => 
        product.name.toLowerCase().includes(searchValue.toLowerCase())
      );
      // 这里可以更新显示的产品列表或调用API
    } else if (searchName === 'category') {
      setSelectedCategory(searchValue);
    } else if (searchName === 'type') {
      setSelectedType(searchValue);
    }
  };

  const handleClearSearch = () => {
    setSelectedCategory('All');
    setSelectedType('All');
    // 重置搜索状态
  };

  // 处理分页变化
  const handlePaginationChange = (page: number, pageSize: number) => {
    fetchApiProducts(page - 1, pageSize);
  };

  // 优化的过滤器处理函数
  const handleCategoryChange = useCallback((category: string) => {
    setSelectedCategory(category);
  }, [])

  const handleTypeChange = useCallback((type: string) => {
    setSelectedType(type)
  }, [])

  // 过滤API Products
  const filteredProducts = useMemo(() =>
    apiProducts?.filter(product => {
      const categoryMatch = selectedCategory === "All" || product.category === selectedCategory
      const typeMatch = selectedType === "All" ||
        (selectedType === "REST API" && product.type === "REST_API") ||
        (selectedType === 'MCP Server' && product.type === 'MCP_SERVER')
      return categoryMatch && typeMatch;
    }),
    [apiProducts, selectedCategory, selectedType],
  )

  // 优化的导航处理函数
  const handleNavigateToProduct = useCallback((productId: string) => {
    navigate(`/api-products/detail?productId=${productId}`);
  }, [navigate]);

  // 处理创建
  const handleCreate = () => {
    setEditingProduct(null);
    setModalVisible(true);
  };

  // 处理编辑
  const handleEdit = (product: ApiProduct) => {
    setEditingProduct(product);
    setModalVisible(true);
  };

  // 处理模态框成功
  const handleModalSuccess = () => {
    setModalVisible(false);
    setEditingProduct(null);
    fetchApiProducts(pagination.current - 1, pagination.pageSize);
  };

  // 处理模态框取消
  const handleModalCancel = () => {
    setModalVisible(false);
    setEditingProduct(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">API Products</h1>
          <p className="text-gray-500 mt-2">
            管理和配置您的API产品
          </p>
        </div>
        <Button onClick={handleCreate} type="primary" icon={<PlusOutlined/>}>
          创建 API Product
        </Button>
      </div>

      {/* 高级搜索 */}
      <AdvancedSearch
        searchParamsList={searchParamsList}
        onSearch={handleSearch}
        onClear={handleClearSearch}
        className="mb-4"
      />

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {filteredProducts.map((product) => (
          <ProductCard
            key={product.productId}
            product={product}
            onNavigate={handleNavigateToProduct}
            handleRefresh={() => fetchApiProducts(pagination.current - 1, pagination.pageSize)}
            onEdit={handleEdit}
          />
        ))}
      </div>

      {pagination.total > 0 && (
        <div className="flex justify-center mt-6">
          <Pagination
            current={pagination.current}
            pageSize={pagination.pageSize}
            total={pagination.total}
            onChange={handlePaginationChange}
            showSizeChanger
            showQuickJumper
            showTotal={(total, range) => `第 ${range[0]}-${range[1]} 条/共 ${total} 条`}
            pageSizeOptions={['6', '12', '24', '48']}
          />
        </div>
      )}

      <ApiProductFormModal
        visible={modalVisible}
        onCancel={handleModalCancel}
        onSuccess={handleModalSuccess}
        productId={editingProduct?.productId}
        initialData={editingProduct || undefined}
      />
    </div>
  )
}
