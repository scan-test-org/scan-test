"use client"

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Navigation } from "@/components/navigation";
import { Footer } from "@/components/footer";
import Link from "next/link";

const apis = [
  {
    id: 1,
    name: "test",
    description: "A test API for demonstration purposes",
    category: "Testing"
  },
  {
    id: 2,
    name: "payments",
    description: "Payment processing API for e-commerce applications",
    category: "Finance"
  },
  {
    id: 3,
    name: "users",
    description: "User management and authentication API",
    category: "Authentication"
  },
  {
    id: 4,
    name: "notifications",
    description: "Send and manage notifications across different channels",
    category: "Communication"
  },
  {
    id: 5,
    name: "analytics",
    description: "Track and analyze application metrics and user behavior",
    category: "Analytics"
  },
  {
    id: 6,
    name: "inventory",
    description: "Manage product inventory and stock levels",
    category: "E-commerce"
  }
];

export default function APIsPage() {
  const [selectedCategory, setSelectedCategory] = useState<string>("All");
  
  // 获取所有分类
  const categories = ["All", ...Array.from(new Set(apis.map(api => api.category)))];
  
  // 根据选择的分类过滤API
  const filteredApis = selectedCategory === "All" 
    ? apis 
    : apis.filter(api => api.category === selectedCategory);

  return (
    <div className="min-h-screen bg-white">
      <Navigation />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            Explore our APIs
          </h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Discover and integrate powerful APIs to accelerate your development.
          </p>
        </div>

        {/* 分类过滤器 */}
        <div className="flex flex-wrap justify-center gap-2 mb-8">
          {categories.map((category) => (
            <Button
              key={category}
              variant={selectedCategory === category ? "default" : "outline"}
              size="sm"
              onClick={() => setSelectedCategory(category)}
              className="mb-2"
            >
              {category}
            </Button>
          ))}
        </div>

        {/* API统计 */}
        <div className="text-center mb-8">
          <p className="text-gray-600">
            Showing {filteredApis.length} of {apis.length} APIs
            {selectedCategory !== "All" && ` in ${selectedCategory}`}
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredApis.map((api) => (
            <Link href={`/apis/${api.id}`} key={api.id}>
              <Card className="hover:shadow-lg transition-shadow cursor-pointer h-full">
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <CardTitle className="text-xl">{api.name}</CardTitle>
                    <Badge variant="secondary" className="ml-2">
                      {api.category}
                    </Badge>
                  </div>
                  <CardDescription>{api.description}</CardDescription>
                </CardHeader>
                <CardContent>
                  <Button variant="outline" className="w-full">
                    View Documentation
                  </Button>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>

        {filteredApis.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">No APIs found in this category.</p>
          </div>
        )}
      </main>
      
      <Footer />
    </div>
  );
} 