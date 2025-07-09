"use client"

import * as React from "react"
import {
  Database,
  Globe,
  LayoutDashboard,
  Settings,
} from "lucide-react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { useSidebar } from "@/components/ui/sidebar"
import { cn } from "@/lib/utils"

const items = [
  {
    title: "Portal",
    url: "/portals",
    icon: Globe,
  },
  {
    title: "API Products",
    url: "/api-products",
    icon: Database,
  },
  {
    title: "网关实例",
    url: "/consoles",
    icon: LayoutDashboard,
  },
]

export function AppSidebar() {
  const pathname = usePathname()
  const { open } = useSidebar()

  return (
    <div 
      className={cn(
        "bg-sidebar border-r border-sidebar-border transition-all duration-200 ease-linear",
        open ? "w-64" : "w-16"
      )}
    >
      <div className="flex flex-col h-full">
        <nav className="flex-1 p-4">
          <ul className="space-y-2">
            {items.map((item) => (
              <li key={item.title}>
                <Link
                  href={item.url}
                  className={cn(
                    "flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors text-nowrap text-ellipsis",
                    "hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                    pathname === item.url
                      ? "bg-sidebar-accent text-sidebar-accent-foreground font-medium"
                      : "text-sidebar-foreground",
                    !open && "justify-center"
                  )}
                >
                  <item.icon className="h-4 w-4 shrink-0" />
                  {open && <span>{item.title}</span>}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
      </div>
    </div>
  )
} 