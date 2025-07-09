import { Navigation } from "@/components/navigation";
import { Footer } from "@/components/footer";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { CheckCircle, Code, Key, Book } from "lucide-react";
import Link from "next/link";

const steps = [
  {
    id: 1,
    title: "Register as a Consumer",
    description: "Create your developer account to access our APIs",
    icon: <CheckCircle className="h-6 w-6" />,
    action: "Sign Up",
    href: "/consumers"
  },
  {
    id: 2,
    title: "Get API Keys",
    description: "Generate API keys for authentication",
    icon: <Key className="h-6 w-6" />,
    action: "Get Keys",
    href: "/consumers"
  },
  {
    id: 3,
    title: "Explore APIs",
    description: "Browse our API catalog and find what you need",
    icon: <Book className="h-6 w-6" />,
    action: "Browse APIs",
    href: "/apis"
  },
  {
    id: 4,
    title: "Start Coding",
    description: "Integrate our APIs into your application",
    icon: <Code className="h-6 w-6" />,
    action: "View Docs",
    href: "/apis"
  }
];

export default function GettingStartedPage() {
  return (
    <div className="min-h-screen bg-white">
      <Navigation />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center mb-16">
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            Getting Started
          </h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Follow these simple steps to start using our APIs and integrate them into your applications
          </p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-16">
          {steps.map((step) => (
            <Card key={step.id} className="text-center hover:shadow-lg transition-shadow">
              <CardHeader>
                <div className="mx-auto w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center text-purple-600 mb-4">
                  {step.icon}
                </div>
                <CardTitle className="text-lg">{step.title}</CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-gray-600 mb-4">{step.description}</p>
                <Link href={step.href}>
                  <Button variant="outline" className="w-full">
                    {step.action}
                  </Button>
                </Link>
              </CardContent>
            </Card>
          ))}
        </div>
        
        <div className="bg-gray-50 rounded-lg p-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Quick Start Example</h2>
          <div className="bg-gray-900 rounded-lg p-6 text-green-400 font-mono text-sm">
            <div className="mb-2"># Install our SDK</div>
            <div className="mb-4">npm install kong-api-sdk</div>
            <div className="mb-2"># Initialize the client</div>
            <div className="mb-2">const client = new KongAPI(&#123;</div>
            <div className="mb-2 ml-4">apiKey: 'your-api-key',</div>
            <div className="mb-2 ml-4">baseURL: 'https://api.example.com'</div>
            <div className="mb-4">&#125;);</div>
            <div className="mb-2"># Make your first API call</div>
            <div>const response = await client.users.list();</div>
          </div>
        </div>
        
        <div className="text-center mt-16">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">
            Need Help?
          </h2>
          <p className="text-gray-600 mb-6">
            Check out our comprehensive documentation and guides
          </p>
          <div className="space-x-4">
            <Link href="/apis">
              <Button>
                Explore APIs
              </Button>
            </Link>
            <Link href="/consumers">
              <Button variant="outline">
                Manage Consumers
              </Button>
            </Link>
          </div>
        </div>
      </main>
      
      <Footer />
    </div>
  );
} 