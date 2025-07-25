// 迁移自 portal-web/portal-frontend/src/lib/utils.ts
export function fetcher(url: string) {
  return fetch(url).then((res) => res.json());
}

export function getTokenFromCookie(): string | null {
  const match = document.cookie.match(/(?:^|;\s*)token=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : null;
} 