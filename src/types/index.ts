export interface ApiProduct {
  productId: string;
  name: string;
  description: string;
  type: 'REST_API' | 'MCP_SERVER';
  category: string;
  status: 'PENDING' | 'READY' | 'PUBLISHED' | string;
  createdAt: string;
  enableConsumerAuth?: boolean;
  apiSpec: string;
} 