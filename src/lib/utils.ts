import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"
 
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
} 


export const getStatusBadgeVariant = (status: string) => {
  return status === "PENDING" ? "orange" : status === "READY" ? "blue" : "green"
}