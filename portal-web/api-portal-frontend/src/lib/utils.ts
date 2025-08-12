// 迁移自 portal-web/portal-frontend/src/lib/utils.ts
export function fetcher(url: string) {
  return fetch(url).then((res) => res.json());
}

export function getTokenFromCookie(): string | null {
  const match = document.cookie.match(/(?:^|;\s*)auth_token=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : null;
}

/**
 * 处理字符串中的换行符转义
 * 将 \\n 转换为 \n
 */
export function unescapeNewlines(str: string): string {
  return str.replace(/\\n/g, '\n');
}

/**
 * 处理产品数据中的 mcpSpec 和 apiSpec 换行符转义
 */
export function processProductSpecs<T extends { type: string; mcpSpec?: string | null; apiSpec?: string | null }>(
  product: T
): T {
  if (product.type === 'MCP_SERVER' && product.mcpSpec) {
    return {
      ...product,
      mcpSpec: unescapeNewlines(product.mcpSpec)
    };
  } else if (product.type === 'REST_API' && product.apiSpec) {
    return {
      ...product,
      apiSpec: unescapeNewlines(product.apiSpec)
    };
  }
  return product;
} 