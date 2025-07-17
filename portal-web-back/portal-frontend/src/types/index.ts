// Common types used across the application

export interface Consumer {
  id: string;
  name: string;
  email: string;
  apiKey: string;
  status: "active" | "inactive" | "suspended";
  createdAt: string;
  updatedAt: string;
  apisAccess: number;
  permissions: string[];
}

export interface ApiSpec {
  id: string;
  name: string;
  description: string;
  version: string;
  endpoints: number;
  methods: HttpMethod[];
  category: string;
  status: "active" | "deprecated" | "beta";
  documentation?: string;
  baseUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Guide {
  id: string;
  title: string;
  description: string;
  category: string;
  readTime: string;
  difficulty: "Beginner" | "Intermediate" | "Advanced";
  content: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH" | "HEAD" | "OPTIONS";

export interface ApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
  error?: string;
  meta?: {
    total: number;
    page: number;
    limit: number;
  };
}

export interface PaginationParams {
  page?: number;
  limit?: number;
  search?: string;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
}

export interface NavigationItem {
  label: string;
  href: string;
  icon?: React.ComponentType<{ className?: string }>;
  children?: NavigationItem[];
}

export interface User {
  id: string;
  name: string;
  email: string;
  avatar?: string;
  role: "admin" | "developer" | "user";
  preferences: {
    theme: "light" | "dark" | "system";
    notifications: boolean;
  };
} 