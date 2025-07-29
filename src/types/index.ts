// 迁移自 portal-web/portal-frontend/src/types/index.ts
// 这里直接复制原文件内容 

// 产品类型枚举
export const ProductType = {
  REST_API: 'REST_API',
  MCP_SERVER: 'MCP_SERVER',
} as const;
export type ProductType = typeof ProductType[keyof typeof ProductType];

// 产品状态枚举
export const ProductStatus = {
  ENABLE: 'ENABLE',
  DISABLE: 'DISABLE',
} as const;
export type ProductStatus = typeof ProductStatus[keyof typeof ProductStatus];

// 产品分类
export const ProductCategory = {
  OFFICIAL: 'official',
  COMMUNITY: 'community',
  CUSTOM: 'custom',
} as const;
export type ProductCategory = typeof ProductCategory[keyof typeof ProductCategory];

// 基础产品接口
export interface BaseProduct {
  productId: string;
  name: string;
  description: string;
  status: ProductStatus;
  enableConsumerAuth: boolean | null;
  type: ProductType;
  document: string | null;
  icon: string | null;
  category: ProductCategory;
}

// REST API 产品
export interface RestApiProduct extends BaseProduct {
  type: typeof ProductType.REST_API;
  apiSpec: string | null;
  mcpSpec: null;
}

// MCP Server 产品
export interface McpServerProduct extends BaseProduct {
  type: typeof ProductType.MCP_SERVER;
  apiSpec: null;
  mcpSpec: string;
}

// 联合类型
export type Product = RestApiProduct | McpServerProduct;

// API 响应结构
export interface ApiResponse<T> {
  code: string;
  message: string | null;
  data: T;
}

// 分页响应结构
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// MCP 配置解析后的结构
export interface McpServerConfig {
  server: {
    name: string;
    config: Record<string, unknown>;
    allowTools: string[];
  };
  tools: Array<{
    name: string;
    description: string;
    args: Array<{
      name: string;
      description: string;
      type: string;
      required: boolean;
      position: string;
      default?: string;
      enum?: string[];
    }>;
    requestTemplate: {
      url: string;
      method: string;
      headers: Array<{
        key: string;
        value: string;
      }>;
    };
    responseTemplate: {
      body: string;
    };
  }>;
} 