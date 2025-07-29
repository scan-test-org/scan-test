import { Link, useLocation } from "react-router-dom";
import { Input } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { UserInfo } from "./user-info";

export function Navigation() {
  const location = useLocation();
  
  const isActive = (path: string) => {
    if (path === '/') {
      return location.pathname === '/';
    }
    return location.pathname.startsWith(path);
  };

  const getNavLinkClass = (path: string) => {
    const baseClass = "font-medium transition-colors";
    return isActive(path) 
      ? `${baseClass} text-blue-600 border-b-2 border-blue-600 pb-1` 
      : `${baseClass} text-gray-700 hover:text-gray-900`;
  };

  return (
    <nav className="bg-white shadow-sm border-b sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link to="/" className="flex items-center space-x-2 hover:opacity-80 transition-opacity">
              <div className="w-8 h-8 bg-black rounded-full flex items-center justify-center">
                <span className="text-white font-bold text-sm">A</span>
              </div>
              <span className="text-xl font-bold text-gray-900">API Portal</span>
            </Link>
          </div>
          
          <div className="hidden md:flex items-center space-x-8">
            <Link 
              to="/getting-started" 
              className={getNavLinkClass('/getting-started')}
            >
              Getting Started
            </Link>
            <Link 
              to="/apis" 
              className={getNavLinkClass('/apis')}
            >
              APIs
            </Link>
            <Link 
              to="/mcp" 
              className={getNavLinkClass('/mcp')}
            >
              MCP
            </Link>
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="hidden sm:block">
              <Input
                placeholder="Search"
                prefix={<SearchOutlined className="text-gray-400" />}
                className="w-48 lg:w-64"
                size="middle"
              />
            </div>
            <UserInfo />
          </div>
        </div>
      </div>
    </nav>
  );
} 