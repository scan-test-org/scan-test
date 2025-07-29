import { Routes, Route } from 'react-router-dom'
import Layout from '@/components/Layout'
import Dashboard from '@/pages/Dashboard'
import Portals from '@/pages/Portals'
import ApiProducts from '@/pages/ApiProducts'
import Consoles from '@/pages/Consoles'
import PortalDetail from '@/pages/PortalDetail'
import ApiProductDetail from '@/pages/ApiProductDetail'
import Login from '@/pages/Login'
import Register from '@/pages/Register'

const AppRoutes = () => {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/" element={<Layout />}>
        <Route index element={<Dashboard />} />
        <Route path="portals" element={<Portals />} />
        <Route path="portals/detail" element={<PortalDetail />} />
        <Route path="api-products" element={<ApiProducts />} />
        <Route path="api-products/detail" element={<ApiProductDetail />} />
        <Route path="consoles" element={<Consoles />} />
      </Route>
    </Routes>
  )
}

export default AppRoutes 