"use client"

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Navigation } from "@/components/navigation";
import { Footer } from "@/components/footer";
import Link from "next/link";

const mcps = [
  {
    id: 1,
    name: "file-manager",
    description: "Powerful file management and operations for modern applications",
    category: "File Management",
    version: "v2.1.0",
    status: "active"
  },
  {
    id: 2,
    name: "database-connector",
    description: "Universal database connectivity and query management",
    category: "Database",
    version: "v1.5.2",
    status: "active"
  },
  {
    id: 3,
    name: "message-queue",
    description: "High-performance message queuing and event streaming",
    category: "Messaging",
    version: "v3.0.1",
    status: "active"
  },
  {
    id: 4,
    name: "cache-manager",
    description: "Distributed caching solution for scalable applications",
    category: "Caching",
    version: "v1.8.3",
    status: "active"
  },
  {
    id: 5,
    name: "auth-service",
    description: "Comprehensive authentication and authorization service",
    category: "Security",
    version: "v2.3.0",
    status: "beta"
  },
  {
    id: 6,
    name: "logging-system",
    description: "Centralized logging and monitoring platform",
    category: "Monitoring",
    version: "v1.9.1",
    status: "active"
  }
];

export default function MCPPage() {
  const [selectedCategory, setSelectedCategory] = useState<string>("All");
  
  // 获取所有分类
  const categories = ["All", ...Array.from(new Set(mcps.map(mcp => mcp.category)))];
  
  // 根据选择的分类过滤MCP
  const filteredMcps = selectedCategory === "All" 
    ? mcps 
    : mcps.filter(mcp => mcp.category === selectedCategory);

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "active":
        return <Badge className="bg-green-100 text-green-800 hover:bg-green-100">Active</Badge>
      case "beta":
        return <Badge className="bg-orange-100 text-orange-800 hover:bg-orange-100">Beta</Badge>
      case "deprecated":
        return <Badge className="bg-red-100 text-red-800 hover:bg-red-100">Deprecated</Badge>
      default:
        return <Badge variant="secondary">Unknown</Badge>
    }
  }

  return (
    <div className="min-h-screen bg-white">
      <Navigation />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            MCP Servers
          </h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Discover and integrate powerful MCP (Model Context Protocol) services to enhance your applications.
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

        {/* MCP统计 */}
        <div className="text-center mb-8">
          <p className="text-gray-600">
            Showing {filteredMcps.length} of {mcps.length} MCP Servers
            {selectedCategory !== "All" && ` in ${selectedCategory}`}
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredMcps.map((mcp) => (
            <Link href={`/mcp/${mcp.id}`} key={mcp.id}>
              <Card className="hover:shadow-lg transition-shadow cursor-pointer h-full">
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <CardTitle className="text-xl">{mcp.name}</CardTitle>
                    <div className="flex gap-2 ml-2">
                      <Badge variant="secondary">{mcp.category}</Badge>
                      {getStatusBadge(mcp.status)}
                    </div>
                  </div>
                  <CardDescription>{mcp.description}</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center justify-between mb-4">
                    <span className="text-sm text-gray-500 font-mono">{mcp.version}</span>
                  </div>
                  <Button variant="outline" className="w-full">
                    View Details
                  </Button>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>

        {filteredMcps.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">No MCP Servers found in this category.</p>
          </div>
        )}
      </main>
      
      <Footer />
    </div>
  );
} 