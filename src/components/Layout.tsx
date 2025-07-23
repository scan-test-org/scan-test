import type { ReactNode } from "react";
import { Navigation } from "./navigation";
import { Footer } from "./footer";

interface LayoutProps {
  children: ReactNode;
  className?: string;
}

export function Layout({ children, className = "" }: LayoutProps) {
  return (
    <div className={`min-h-screen bg-gray-50 ${className}`}>
      <Navigation />
      <main className="pt-4">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {children}
        </div>
      </main>
      <Footer />
    </div>
  );
} 