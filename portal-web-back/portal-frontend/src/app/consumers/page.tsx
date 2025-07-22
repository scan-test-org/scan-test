"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Navigation } from "@/components/navigation";
import { Footer } from "@/components/footer";
import { Plus, Eye, Edit, Trash2 } from "lucide-react";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";

interface Consumer {
  id: string;
  name: string;
  email: string;
  apiKey: string;
  status: "active" | "inactive";
  createdAt: string;
  apisAccess: number;
}

const initialConsumers: Consumer[] = [
  {
    id: "1",
    name: "John Smith",
    email: "john@example.com",
    apiKey: "ak_1234567890abcdef",
    status: "active",
    createdAt: "2024-01-15",
    apisAccess: 5
  },
  {
    id: "2",
    name: "Jane Doe",
    email: "jane@example.com",
    apiKey: "ak_abcdef1234567890",
    status: "active",
    createdAt: "2024-01-10",
    apisAccess: 3
  },
  {
    id: "3",
    name: "Bob Johnson",
    email: "bob@example.com",
    apiKey: "ak_567890abcdef1234",
    status: "inactive",
    createdAt: "2024-01-05",
    apisAccess: 2
  }
];

export default function ConsumersPage() {
  const [consumers, setConsumers] = useState<Consumer[]>(initialConsumers);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [newConsumer, setNewConsumer] = useState({
    name: "",
    email: "",
  });

  const handleAddConsumer = () => {
    if (newConsumer.name && newConsumer.email) {
      const consumer: Consumer = {
        id: Date.now().toString(),
        name: newConsumer.name,
        email: newConsumer.email,
        apiKey: `ak_${Math.random().toString(36).substr(2, 16)}`,
        status: "active",
        createdAt: new Date().toISOString().split('T')[0],
        apisAccess: 0
      };
      setConsumers([...consumers, consumer]);
      setNewConsumer({ name: "", email: "" });
      setIsDialogOpen(false);
    }
  };

  const handleDeleteConsumer = (id: string) => {
    setConsumers(consumers.filter(consumer => consumer.id !== id));
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navigation />
      
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Consumers</h1>
              <p className="text-gray-600 mt-2">Manage API consumers and their access permissions</p>
            </div>
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button className="bg-purple-600 hover:bg-purple-700">
                  <Plus className="h-4 w-4 mr-2" />
                  Add Consumer
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Add New Consumer</DialogTitle>
                </DialogHeader>
                <div className="space-y-4">
                  <div>
                    <Label htmlFor="name">Name</Label>
                    <Input
                      id="name"
                      placeholder="Enter consumer name"
                      value={newConsumer.name}
                      onChange={(e) => setNewConsumer({ ...newConsumer, name: e.target.value })}
                    />
                  </div>
                  <div>
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      placeholder="Enter consumer email"
                      value={newConsumer.email}
                      onChange={(e) => setNewConsumer({ ...newConsumer, email: e.target.value })}
                    />
                  </div>
                  <div className="flex justify-end space-x-2 pt-4">
                    <Button
                      variant="outline"
                      onClick={() => setIsDialogOpen(false)}
                    >
                      Cancel
                    </Button>
                    <Button
                      onClick={handleAddConsumer}
                      className="bg-purple-600 hover:bg-purple-700"
                    >
                      Add Consumer
                    </Button>
                  </div>
                </div>
              </DialogContent>
            </Dialog>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Consumer List</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>API Key</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>APIs Access</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {consumers.map((consumer) => (
                  <TableRow key={consumer.id}>
                    <TableCell className="font-medium">{consumer.name}</TableCell>
                    <TableCell>{consumer.email}</TableCell>
                    <TableCell>
                      <code className="bg-gray-100 px-2 py-1 rounded text-xs">
                        {consumer.apiKey}
                      </code>
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={consumer.status === "active" ? "default" : "secondary"}
                      >
                        {consumer.status}
                      </Badge>
                    </TableCell>
                    <TableCell>{consumer.apisAccess}</TableCell>
                    <TableCell>{consumer.createdAt}</TableCell>
                    <TableCell>
                      <div className="flex space-x-2">
                        <Button variant="outline" size="sm">
                          <Eye className="h-4 w-4" />
                        </Button>
                        <Button variant="outline" size="sm">
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => handleDeleteConsumer(consumer.id)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
                  </Card>
        </main>
        
        <Footer />
      </div>
    );
  } 