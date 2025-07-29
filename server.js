// server.js
import express from 'express';
import path from 'path';
import { fileURLToPath } from 'url';
import process from 'process';

// 模拟 __dirname（ESM 中不再可用）
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();

// 静态资源目录（你的构建输出目录，如 dist）
const DIST_DIR = path.join(__dirname, 'dist');

// 提供静态文件（JS、CSS、图片等）
app.use(express.static(DIST_DIR));

// 处理 API 路由（如果有的话）
// app.get('/api/*', (req, res) => {
//   res.status(404).json({ error: 'API endpoint not found' });
// });

// SPA：所有其他路由返回 index.html（支持前端路由）
app.get('*', (req, res) => {
  res.sendFile(path.join(DIST_DIR, 'index.html'));
});

// 设置端口
const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`🚀 Server is running on http://localhost:${PORT}`);
});
