import { RouterProvider } from 'react-router-dom'
import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { router } from './routes'
import aliyunThemeToken from './aliyunThemeToken'
import './App.css'

function App() {
  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: aliyunThemeToken,
      }}
    >
      <RouterProvider router={router} />
    </ConfigProvider>
  )
}

export default App
