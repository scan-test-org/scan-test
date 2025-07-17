import express from 'express'
import path from 'path'
import { fileURLToPath } from 'url'
import compression from 'compression'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const app = express()
const PORT = 3000


// 压缩中间件
app.use(compression())

// 静态文件服务
app.use(express.static(path.join(__dirname, 'dist')))

// 所有其他请求都返回index.html（SPA路由）
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist', 'index.html'))
})

app.listen(PORT, () => {
  console.log(`🚀 Portal Admin Server is running on port ${PORT}`)
  console.log(`📱 Open http://localhost:${PORT} to view the application`)
}) 