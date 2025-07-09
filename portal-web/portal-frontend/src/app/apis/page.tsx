import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Navigation } from "@/components/navigation";
import { Footer } from "@/components/footer";
import Link from "next/link";

const apis = [
  {
    id: 1,
    name: "test",
    description: "A test API for demonstration purposes",
    endpoints: 5,
    methods: ["GET", "POST", "PUT", "DELETE"],
    category: "Testing"
  },
  {
    id: 2,
    name: "payments",
    description: "Payment processing API for e-commerce applications",
    endpoints: 12,
    methods: ["GET", "POST", "PUT"],
    category: "Finance"
  },
  {
    id: 3,
    name: "users",
    description: "User management and authentication API",
    endpoints: 8,
    methods: ["GET", "POST", "PUT", "DELETE"],
    category: "Authentication"
  },
  {
    id: 4,
    name: "notifications",
    description: "Send and manage notifications across different channels",
    endpoints: 6,
    methods: ["GET", "POST", "PUT"],
    category: "Communication"
  },
  {
    id: 5,
    name: "analytics",
    description: "Track and analyze application metrics and user behavior",
    endpoints: 15,
    methods: ["GET", "POST"],
    category: "Analytics"
  },
  {
    id: 6,
    name: "inventory",
    description: "Manage product inventory and stock levels",
    endpoints: 10,
    methods: ["GET", "POST", "PUT", "DELETE"],
    category: "E-commerce"
  }
];

export default function APIsPage() {
  return (
    <div className="min-h-screen bg-white">
      <Navigation />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            Explore our APIs
          </h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus finibus orci sit amet arcu feugiat.
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {apis.map((api) => (
            <Card key={api.id} className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="text-xl">{api.name}</CardTitle>
                <CardDescription>{api.description}</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between mb-4">
                  <span className="text-sm text-gray-500">{api.endpoints} endpoints</span>
                  <span className="text-sm text-gray-500">{api.category}</span>
                </div>
                <div className="flex flex-wrap gap-2 mb-4">
                  {api.methods.map((method) => (
                    <span
                      key={method}
                      className="px-2 py-1 bg-gray-100 text-gray-700 rounded text-xs"
                    >
                      {method}
                    </span>
                  ))}
                </div>
                <Button variant="outline" className="w-full">
                  View APIs
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      </main>
      
      <Footer />
    </div>
  );
} 