import type { ReactNode } from "react";
import { Navigation } from "./Navigation";

interface LayoutProps {
  children: ReactNode;
  className?: string;
}

export function Layout({ children, className = "" }: LayoutProps) {
  return (
    <div className={`min-h-screen bg-[#f4f4f6] ${className}`}>
      <Navigation />
      <main className="pt-4">
        <div className="w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {children}
        </div>
      </main>
      {/* <Footer /> */}
    </div>
  );
} 