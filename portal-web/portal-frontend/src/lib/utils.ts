// 迁移自 portal-web/portal-frontend/src/lib/utils.ts
export function fetcher(url: string) {
  return fetch(url).then((res) => res.json());
} 