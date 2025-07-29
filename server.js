// server.js
import express from 'express';
import path from 'path';
import { fileURLToPath } from 'url';

// 模拟 __dirname（ESM 中不再可用）
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();

// 静态资源目录（你的构建输出目录，如 dist）
const DIST_DIR = path.join(__dirname, 'dist');

// 提供静态文件（JS、CSS、图片等）
app.use(express.static(DIST_DIR));

// SPA：所有未匹配的路由返回 index.html（支持前端路由）
// app.get('/', (req, res) => {
//   res.sendFile(path.resolve(DIST_DIR, 'index.html'));
// });

app.get('/*', (req, res) => {
  res.sendFile(path.resolve(DIST_DIR, 'index.html'));
});
// 设置端口
const PORT = 3000;

app.listen(PORT, () => {
  console.log(`🚀 Server is running on http://localhost:${PORT}`);
});
