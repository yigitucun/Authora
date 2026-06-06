"use client"

import * as React from "react"
import { NavMain } from "@/components/nav-main"
import { NavProjects } from "@/components/nav-projects"
import { NavUser } from "@/components/nav-user"
import { WorkspaceHeader } from "@/components/team-switcher"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarRail,
} from "@/components/ui/sidebar"

// IDP mimarisine uygun yepyeni ikonlarımız:
import {
  GalleryVerticalEndIcon,
  LayoutDashboardIcon,
  BlocksIcon,
  UsersIcon,
  ShieldCheckIcon,
  PaletteIcon,
  ActivityIcon,
  SettingsIcon,
  BookOpenIcon
} from "lucide-react"
import {useSession} from "next-auth/react";


const data = {
  user: {
    name: "Yiğit Ali",
    email: "yigit_ali_ucun@outlook.com",
    avatar: "",
  },
  teams: [
    {
      name: "My Workspace",
      logo: <GalleryVerticalEndIcon/>,
      plan: "Enterprise",
    },
  ],
  navMain: [
    {
      title: "Dashboard",
      url: "/dashboard",
      icon: <LayoutDashboardIcon/>,
      isActive: true,
      items: [
        {
          title: "Overview",
          url: "/dashboard",
        },
        {
          title: "Analytics",
          url: "/dashboard/analytics",
        }
      ],
    },
    {
      title: "Applications",
      url: "/dashboard/applications",
      icon: <BlocksIcon/>,
      items: [
        {
          title: "Registered Clients",
          url: "/dashboard/applications",
        },
        {
          title: "Create New API",
          url: "/dashboard/applications/new",
        },
      ],
    },
    {
      title: "User Management",
      url: "/dashboard/users",
      icon: <UsersIcon/>,
      items: [
        {
          title: "All Users",
          url: "/dashboard/users",
        },
        {
          title: "Roles & Permissions",
          url: "/dashboard/roles",
        },
      ],
    },
    {
      title: "Authentication",
      url: "/dashboard/auth",
      icon: <ShieldCheckIcon/>,
      items: [
        {
          title: "Social Connections",
          url: "/dashboard/auth/social",
        },
        {
          title: "Enterprise SSO",
          url: "/dashboard/auth/sso",
        },
        {
          title: "Security & MFA",
          url: "/dashboard/auth/security",
        },
      ],
    },
    {
      title: "Customization",
      url: "/dashboard/branding",
      icon: <PaletteIcon/>,
      items: [
        {
          title: "Login Page Branding",
          url: "/dashboard/branding",
        },
        {
          title: "Email Templates",
          url: "/dashboard/emails",
        },
      ],
    },
  ],
  projects: [
    {
      name: "Audit Logs",
      url: "/dashboard/logs",
      icon: <ActivityIcon/>,
    },
    {
      name: "Tenant Settings",
      url: "/dashboard/settings",
      icon: <SettingsIcon/>,
    },
    {
      name: "Documentation",
      url: "/docs",
      icon: <BookOpenIcon/>,
    },
  ],
}

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  const { data: session } = useSession()

  const user = {
    name: session?.user?.name || "Loading...",
    email: session?.user?.email || "",
    avatar: "",
  }

  const workspace = {
    name: session?.companyName || "",
    logo: <GalleryVerticalEndIcon />,
    plan: "Enterprise",
  }

  return (
      <Sidebar collapsible="icon" {...props}>
        <SidebarHeader>
          <WorkspaceHeader workspace={workspace} />
        </SidebarHeader>
        <SidebarContent>
          <NavMain items={data.navMain} />
          <NavProjects projects={data.projects} />
        </SidebarContent>
        <SidebarFooter>
          <NavUser user={user} />
        </SidebarFooter>
        <SidebarRail />
      </Sidebar>
  )
}