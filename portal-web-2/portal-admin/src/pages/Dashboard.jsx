import React from 'react'

const Dashboard = () => {
  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">仪表板</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-card p-4 rounded-lg border">
          <h3 className="text-sm font-medium text-muted-foreground">总门户数</h3>
          <p className="text-2xl font-bold">12</p>
        </div>
        <div className="bg-card p-4 rounded-lg border">
          <h3 className="text-sm font-medium text-muted-foreground">API产品数</h3>
          <p className="text-2xl font-bold">8</p>
        </div>
        <div className="bg-card p-4 rounded-lg border">
          <h3 className="text-sm font-medium text-muted-foreground">控制台数</h3>
          <p className="text-2xl font-bold">5</p>
        </div>
        <div className="bg-card p-4 rounded-lg border">
          <h3 className="text-sm font-medium text-muted-foreground">活跃用户</h3>
          <p className="text-2xl font-bold">1,234</p>
        </div>
      </div>
    </div>
  )
}

export default Dashboard 