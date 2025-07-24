import { createBrowserRouter } from 'react-router-dom'
import Layout from '@/components/Layout'
import Dashboard from '@/pages/Dashboard'
import Portals from '@/pages/Portals'
import ApiProducts from '@/pages/ApiProducts'
import Consoles from '@/pages/Consoles'
import PortalDetail from '@/pages/PortalDetail'
import ApiProductDetail from '@/pages/ApiProductDetail'
import Login from '@/pages/Login'
import Register from '@/pages/Register'

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/register',
    element: <Register />,
  },
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Dashboard />,
      },
      {
        path: 'portals',
        element: <Portals />,
      },
      {
        path: 'portals/detail',
        element: <PortalDetail />,
      },
      {
        path: 'api-products',
        element: <ApiProducts />,
      },
      {
        path: 'api-products/detail',
        element: <ApiProductDetail />,
      },
      {
        path: 'consoles',
        element: <Consoles />,
      },
    ],
  },
]) 