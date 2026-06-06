"use client"

import { AppSidebar } from "@/components/app-sidebar"
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar"
import { usePathname } from "next/navigation"

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
    const pathname = usePathname()
    const isOnboarding = pathname === "/dashboard/onboarding"

    if (isOnboarding) {
        return <>{children}</>
    }

    return (
        <SidebarProvider>
            <AppSidebar />

            <SidebarInset>
                {children}
            </SidebarInset>
        </SidebarProvider>
    )
}