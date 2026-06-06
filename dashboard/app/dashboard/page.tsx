"use client"

import { useEffect, useState } from "react"
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb"
import { Separator } from "@/components/ui/separator"
import { SidebarTrigger } from "@/components/ui/sidebar"
import { fetchApi } from "@/lib/api"
import { ActivityIcon, UsersIcon, BlocksIcon, CheckCircle2Icon } from "lucide-react"

type Application = {
  id: string
  clientId: string
}

type User = {
  id: string
  email: string
  isVerified: boolean
}

type AuditLog = {
  id: string
  action: string
  actorEmail: string
  targetType: string
  targetId: string
  createdAt: string
}

export default function DashboardOverviewPage() {
  const [applications, setApplications] = useState<Application[]>([])
  const [users, setUsers] = useState<User[]>([])
  const [logs, setLogs] = useState<AuditLog[]>([])
  const [loading, setLoading] = useState(true)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  async function loadOverview() {
    setLoading(true)
    const [appsResult, usersResult, logsResult] = await Promise.all([
      fetchApi<Application[]>("/v1/applications"),
      fetchApi<User[]>("/v1/users"),
      fetchApi<AuditLog[]>("/v1/audit-logs?limit=5"),
    ])

    const [appsError, appsData] = appsResult
    const [usersError, usersData] = usersResult
    const [logsError, logsData] = logsResult

    const firstError = appsError || usersError || logsError
    if (firstError) {
      setErrorMsg(firstError.message)
    } else {
      setApplications(appsData || [])
      setUsers(usersData || [])
      setLogs(logsData || [])
      setErrorMsg(null)
    }

    setLoading(false)
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadOverview()
  }, [])

  function formatDate(value: string) {
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) {
      return value
    }
    return date.toLocaleString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    })
  }

  const verifiedUsers = users.filter((user) => user.isVerified).length

  return (
    <>
      {/* HEADER VE HAMBURGER MENU */}
      <header className="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12">
        <div className="flex items-center gap-2 px-4">
          <SidebarTrigger className="-ml-1" />
          <Separator orientation="vertical" className="mr-2 data-vertical:h-4 data-vertical:self-auto" />
          <Breadcrumb>
            <BreadcrumbList>
              <BreadcrumbItem className="hidden md:block">
                <BreadcrumbLink href="/dashboard">Platform</BreadcrumbLink>
              </BreadcrumbItem>
              <BreadcrumbSeparator className="hidden md:block" />
              <BreadcrumbItem>
                <BreadcrumbPage>Overview</BreadcrumbPage>
              </BreadcrumbItem>
            </BreadcrumbList>
          </Breadcrumb>
        </div>
      </header>

      {/* ANA ICERIK - METRIKLER VE AKTIVITE */}
      <div className="flex flex-1 flex-col gap-4 p-4 pt-0">
        {errorMsg && (
          <div className="rounded-xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
            {errorMsg}
          </div>
        )}

        <div className="grid auto-rows-min gap-4 md:grid-cols-3">
          <div className="flex flex-col justify-center rounded-xl border bg-card p-6 shadow-sm">
            <div className="flex items-center justify-between space-y-0 pb-2">
              <h3 className="tracking-tight text-sm font-medium text-muted-foreground">Total Applications</h3>
              <BlocksIcon className="h-4 w-4 text-muted-foreground" />
            </div>
            <p className="text-3xl font-bold">{loading ? "-" : applications.length}</p>
            <p className="text-xs text-muted-foreground mt-1">Registered clients in this tenant</p>
          </div>

          <div className="flex flex-col justify-center rounded-xl border bg-card p-6 shadow-sm">
            <div className="flex items-center justify-between space-y-0 pb-2">
              <h3 className="tracking-tight text-sm font-medium text-muted-foreground">Total Users</h3>
              <UsersIcon className="h-4 w-4 text-muted-foreground" />
            </div>
            <p className="text-3xl font-bold">{loading ? "-" : users.length}</p>
            <p className="text-xs text-muted-foreground mt-1">All users in this tenant</p>
          </div>

          <div className="flex flex-col justify-center rounded-xl border bg-card p-6 shadow-sm">
            <div className="flex items-center justify-between space-y-0 pb-2">
              <h3 className="tracking-tight text-sm font-medium text-muted-foreground">Verified Users</h3>
              <CheckCircle2Icon className="h-4 w-4 text-muted-foreground" />
            </div>
            <p className="text-3xl font-bold">{loading ? "-" : verifiedUsers}</p>
            <p className="text-xs text-muted-foreground mt-1">Email verified accounts</p>
          </div>
        </div>

        <div className="rounded-xl border bg-card p-6 shadow-sm">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-lg font-semibold tracking-tight">Recent Activity</h3>
              <p className="text-sm text-muted-foreground">Latest audit events for this tenant.</p>
            </div>
            <ActivityIcon className="h-5 w-5 text-muted-foreground" />
          </div>

          {loading ? (
            <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
              <ActivityIcon className="h-12 w-12 text-muted-foreground opacity-20 mb-4" />
              <p>Loading recent activity...</p>
            </div>
          ) : logs.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center text-muted-foreground">
              <ActivityIcon className="h-12 w-12 text-muted-foreground opacity-20 mb-4" />
              <p>No recent activity found.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {logs.map((log) => (
                <div key={log.id} className="flex items-start justify-between gap-4 rounded-lg border bg-background px-4 py-3">
                  <div>
                    <p className="text-sm font-medium">{log.action}</p>
                    <p className="text-xs text-muted-foreground">
                      {log.actorEmail || "System"} · {log.targetType ? `${log.targetType} ${log.targetId}` : log.targetId}
                    </p>
                  </div>
                  <span className="text-xs text-muted-foreground">{formatDate(log.createdAt)}</span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  )
}