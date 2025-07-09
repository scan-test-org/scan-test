"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { LoginDialog } from "@/components/login-dialog";
import { UserMenu } from "@/components/user-menu";

export function Navigation() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    const loggedIn = localStorage.getItem("isLoggedIn") === "true";
    setIsLoggedIn(loggedIn);
  }, []);
  return (
    <nav className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link href="/" className="flex items-center space-x-2">
              <div className="w-8 h-8 bg-black rounded-full flex items-center justify-center">
                <span className="text-white font-bold">C</span>
              </div>
              <span className="text-xl font-bold">Company</span>
            </Link>
          </div>
          
          <div className="flex items-center space-x-6">
            <Link href="/getting-started" className="text-gray-700 hover:text-gray-900">
              Getting Started
            </Link>
            <Link href="/apis" className="text-gray-700 hover:text-gray-900">
              APIs
            </Link>
            <Link href="/consumers" className="text-gray-700 hover:text-gray-900">
              Consumers
            </Link>
          </div>
          
          <div className="flex items-center space-x-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <Input
                placeholder="Search"
                className="pl-10 pr-4 py-2 w-64"
              />
            </div>
            {isLoggedIn ? (
              <UserMenu />
            ) : (
              <LoginDialog />
            )}
          </div>
        </div>
      </div>
    </nav>
  );
} 