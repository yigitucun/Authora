"use client"

import { useEffect, useState } from "react"
import { fetchApi } from "@/lib/api"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Button } from "@/components/ui/button"
import { Loader2Icon, RefreshCcwIcon } from "lucide-react"

type AuditLog = {
  id: string
  action: string
  targetType: string
  targetId: string
  actorEmail: string
  ip: string
  userAgent: string
  createdAt: string
}

export default function AuditLogsPage() {
  const [logs, setLogs] = useState<AuditLog[]>([])
  const [loading, setLoading] = useState(true)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  useEffect(() => {
    loadLogs()
  }, [])

  async function loadLogs() {
    setLoading(true)
    const [error, data] = await fetchApi<AuditLog[]>("/v1/audit-logs")
    if (error) {
      setErrorMsg(error.message)
    } else if (data) {
      setLogs(data)
      setErrorMsg(null)
    }
    setLoading(false)
  }

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

  return (
    <div className="flex-1 space-y-6 p-6 md:p-8">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Audit Logs</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Recent security and configuration events for your tenant.
          </p>
        </div>
        <Button variant="outline" size="sm" onClick={loadLogs} disabled={loading}>
          {loading ? (
            <Loader2Icon className="h-4 w-4 mr-2 animate-spin" />
          ) : (
            <RefreshCcwIcon className="h-4 w-4 mr-2" />
          )}
          Refresh
        </Button>
      </div>

      <div className="rounded-lg border bg-card">
        {loading ? (
          <div className="flex flex-col items-center justify-center p-16 text-muted-foreground">
            <Loader2Icon className="h-8 w-8 animate-spin mb-3" />
            <p className="text-sm">Loading audit logs...</p>
          </div>
        ) : errorMsg ? (
          <div className="flex flex-col items-center justify-center p-16 text-destructive">
            <p className="text-sm">{errorMsg}</p>
          </div>
        ) : logs.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-16 text-muted-foreground">
            <p className="text-sm">No audit events yet.</p>
          </div>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Time</TableHead>
                <TableHead>Action</TableHead>
                <TableHead>Actor</TableHead>
                <TableHead>Target</TableHead>
                <TableHead>IP</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {logs.map((log) => (
                <TableRow key={log.id}>
                  <TableCell className="text-sm text-muted-foreground">
                    {formatDate(log.createdAt)}
                  </TableCell>
                  <TableCell className="font-medium text-sm">
                    {log.action}
                  </TableCell>
                  <TableCell className="text-sm">
                    {log.actorEmail || "-"}
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {log.targetType ? `${log.targetType} ${log.targetId}` : log.targetId || "-"}
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground">
                    {log.ip || "-"}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </div>
    </div>
  )
}

