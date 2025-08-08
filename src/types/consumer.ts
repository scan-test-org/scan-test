import type { Product } from "./index";

export interface Consumer {
  consumerId: string;
  name: string;
  description?: string;
  status?: string;
  createAt?: string;
  enabled?: boolean;
}

export interface Credential {
  id?: string;
  type: 'API_KEY' | 'HMAC' | 'JWT';
  apiKey?: string;
  accessKey?: string;
  secretKey?: string;
  createAt?: string;
}

export interface ConsumerCredentialResult {
  apiKeyConfig?: {
    credentials?: Array<{
      id?: string;
      key?: string;
      createAt?: string;
    }>;
    source?: string;
    key?: string;
  };
  hmacConfig?: {
    credentials?: Array<{
      id?: string;
      accessKey?: string;
      secretKey?: string;
      createAt?: string;
    }>;
  };
  jwtConfig?: Record<string, unknown>;
}

export interface Subscription {
  productId: string;
  consumerId: string;
  status: 'PENDING' | 'APPROVED';
  createAt: string;
  updatedAt: string;
  product?: Product;
}

export interface CreateCredentialParam {
  apiKeyConfig?: {
    credentials?: Array<{
      id?: string;
      key?: string;
      createAt?: string;
    }>;
    source?: string;
    key?: string;
    generationMethod?: 'SYSTEM' | 'CUSTOM';
    customApiKey?: string;
  };
  hmacConfig?: {
    credentials?: Array<{
      id?: string;
      accessKey?: string;
      secretKey?: string;
      createAt?: string;
    }>;
    generationMethod?: 'SYSTEM' | 'CUSTOM';
    customAccessKey?: string;
    customSecretKey?: string;
  };
  jwtConfig?: Record<string, unknown>;
} 