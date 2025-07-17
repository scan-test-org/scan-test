import { Link } from "react-router-dom";

export function Footer() {
  return (
    <footer className="bg-gray-50 border-t">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <div className="flex items-center space-x-2 mb-4">
              <div className="w-8 h-8 bg-black rounded-full flex items-center justify-center">
                <span className="text-white font-bold">C</span>
              </div>
              <span className="text-xl font-bold">Company</span>
            </div>
            <p className="text-gray-600 text-sm">
              Build powerful applications with our comprehensive API platform.
            </p>
          </div>
          <div>
            <h3 className="font-semibold mb-4">Products</h3>
            <div className="space-y-2">
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm">
                Pricing
              </Link>
              <Link to="/apis" className="block text-gray-600 hover:text-gray-900 text-sm">
                APIs
              </Link>
              <Link to="/mcp" className="block text-gray-600 hover:text-gray-900 text-sm">
                MCP Servers
              </Link>
            </div>
          </div>
          <div>
            <h3 className="font-semibold mb-4">Company</h3>
            <div className="space-y-2">
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm">
                About
              </Link>
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm">
                Careers
              </Link>
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm">
                Press
              </Link>
            </div>
          </div>
          <div>
            <h3 className="font-semibold mb-4">Legal</h3>
            <div className="space-y-2">
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm">
                Terms and conditions
              </Link>
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm">
                Data privacy
              </Link>
              <Link to="#" className="block text-gray-600 hover:text-gray-900 text-sm">
                Trust and compliance
              </Link>
            </div>
          </div>
        </div>
        <div className="border-t border-gray-200 mt-8 pt-8 text-center">
          <p className="text-gray-600 text-sm">
            © 2024 Company. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
} 