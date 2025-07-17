import { Button } from "@/components/ui/button";
import { Navigation } from "@/components/navigation";
import { Footer } from "@/components/footer";
import Link from "next/link";

export default function HomePage() {
  return (
    <div className="min-h-screen bg-white">
      <Navigation />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center">
          <h1 className="text-6xl font-bold text-gray-900 mb-6">
            API Dev Portal
          </h1>
          
          <p className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
            Here you will have some good context in the subheading for your developer portal so users can know
            more about your product
          </p>
          
          <Link href="/apis">
            <Button 
              size="lg" 
              className="bg-purple-600 hover:bg-purple-700 text-white px-8 py-3 text-lg"
            >
              Get started
            </Button>
          </Link>
        </div>
        
        <div className="mt-16 relative">
          <div className="bg-gradient-to-r from-purple-600 via-blue-600 to-purple-800 rounded-lg p-8 relative overflow-hidden">
            <div className="absolute inset-0 bg-gradient-to-r from-purple-600 via-blue-600 to-purple-800 opacity-90"></div>
            <div className="absolute inset-0 grid grid-cols-8 gap-4">
              {Array.from({ length: 32 }, (_, i) => (
                <div key={i} className="bg-white/10 rounded-full aspect-square opacity-30"></div>
              ))}
            </div>
            <div className="relative z-10 h-64 flex items-center justify-center">
              <div className="text-white text-center">
                <h2 className="text-3xl font-bold mb-4">
                  Explore our APIs
                </h2>
                <p className="text-purple-100 text-lg">
                  Discover powerful APIs to enhance your applications
                </p>
              </div>
            </div>
          </div>
        </div>
      </main>
      
      <Footer />
    </div>
  );
} 