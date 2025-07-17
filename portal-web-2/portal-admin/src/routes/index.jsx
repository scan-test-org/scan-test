import { createBrowserRouter } from 'react-router-dom'
import Layout from '@/components/Layout'
import Dashboard from '@/pages/Dashboard'
import Portals from '@/pages/Portals'
import ApiProducts from '@/pages/ApiProducts'
import Consoles from '@/pages/Consoles'
import PortalDetail from '@/pages/PortalDetail'

export const router = createBrowserRouter([
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
        path: 'api-products',
        element: <ApiProducts />,
      },
      {
        path: 'consoles',
        element: <Consoles />,
      },
      {
        path: 'portals/:id',
        element: <PortalDetail />,
      },
    ],
  },
]) 