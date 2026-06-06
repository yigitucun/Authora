"use client"

import { useCallback, useEffect, useMemo, useState } from "react"
import { fetchApi } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { CheckIcon, Loader2Icon } from "lucide-react"

type Application = {
  id: string
  clientId: string
  clientName: string
}

type ConnectionField = {
  key: string
  label: string
  type: string
  placeholder?: string
  required?: boolean
  secret?: boolean
}

type ConnectionType = {
  id: string
  name: string
  description: string
  enabled: boolean
  settings: string
  requiredFields?: string
  settingsSchema?: string
}

function toLabel(value: string) {
  return value
    .replace(/([a-z])([A-Z])/g, "$1 $2")
    .replace(/[_-]+/g, " ")
    .replace(/^./, (char) => char.toUpperCase())
}

function toStringArray(value: unknown) {
  if (!Array.isArray(value)) return []
  return value.map((item) => String(item))
}

function safeJsonParse(value: string | undefined) {
  if (!value) return null
  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

function safeJsonObject(value: string) {
  const parsed = safeJsonParse(value)
  return parsed && typeof parsed === "object" ? parsed : {}
}

function normalizeSchema(connection: ConnectionType): ConnectionField[] {
  const schemaValue = safeJsonParse(connection.settingsSchema)
  const required = new Set<string>(toStringArray(safeJsonParse(connection.requiredFields)))

  const schemaArray = Array.isArray(schemaValue)
    ? schemaValue
    : schemaValue && Array.isArray(schemaValue.fields)
      ? schemaValue.fields
      : []

  const fields = schemaArray
    .map((field) => {
      if (typeof field === "string") {
        return { key: field, label: toLabel(field), type: "text" }
      }
      if (!field || typeof field !== "object") {
        return null
      }
      const key = String(field.key || field.name || "")
      if (!key) return null
      return {
        key,
        label: String(field.label || toLabel(key)),
        type: String(field.type || (field.secret ? "password" : "text")),
        placeholder: field.placeholder ? String(field.placeholder) : undefined,
        required: Boolean(field.required),
        secret: Boolean(field.secret)
      }
    })
    .filter(Boolean) as ConnectionField[]

  if (fields.length > 0) {
    return fields.map((field) => ({
      ...field,
      required: field.required || required.has(field.key)
    }))
  }

  const requiredFields = toStringArray(safeJsonParse(connection.requiredFields))
  return requiredFields.map((key) => ({
    key,
    label: toLabel(key),
    type: "text",
    required: true
  }))
}

export default function SocialConnectionsPage() {
  const [applications, setApplications] = useState<Application[]>([])
  const [selectedClientId, setSelectedClientId] = useState("")
  const [connections, setConnections] = useState<ConnectionType[]>([])
  const [loading, setLoading] = useState(true)
  const [savingId, setSavingId] = useState<string | null>(null)
  const [savingSettingsId, setSavingSettingsId] = useState<string | null>(null)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [settingsById, setSettingsById] = useState<Record<string, Record<string, string>>>({})
  const [schemaById, setSchemaById] = useState<Record<string, ConnectionField[]>>({})

  const loadApplications = useCallback(async () => {
    const [error, data] = await fetchApi<Application[]>("/v1/applications")
    if (error) {
      setErrorMsg(error.message)
      return
    }
    const list = data || []
    setApplications(list)
    if (list.length > 0 && !selectedClientId) {
      setSelectedClientId(list[0].clientId)
    }
  }, [selectedClientId])

  const loadConnections = useCallback(async (clientId: string) => {
    if (!clientId) return
    setLoading(true)
    const [error, data] = await fetchApi<ConnectionType[]>(
      `/v1/connections/types?clientId=${clientId}&social=true`
    )
    if (error) {
      setErrorMsg(error.message)
    } else {
      const list = data || []
      setConnections(list)
      setErrorMsg(null)
      const nextSettings: Record<string, Record<string, string>> = {}
      const nextSchemas: Record<string, ConnectionField[]> = {}
      list.forEach((connection) => {
        nextSettings[connection.id] = safeJsonObject(connection.settings)
        nextSchemas[connection.id] = normalizeSchema(connection)
      })
      setSettingsById(nextSettings)
      setSchemaById(nextSchemas)
    }
    setLoading(false)
  }, [])

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadApplications()
  }, [loadApplications])

  useEffect(() => {
    if (selectedClientId) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      loadConnections(selectedClientId)
    }
  }, [loadConnections, selectedClientId])

  async function toggleConnection(connection: ConnectionType) {
    if (!selectedClientId) return
    setSavingId(connection.id)
    const settings = safeJsonParse(connection.settings)
    const [error] = await fetchApi(`/v1/connections/types/${connection.id}?clientId=${selectedClientId}`, {
      method: "PUT",
      body: JSON.stringify({ enabled: !connection.enabled, settings })
    })

    if (error) {
      setErrorMsg(error.message)
    } else {
      setConnections((prev) =>
        prev.map((item) =>
          item.id === connection.id ? { ...item, enabled: !item.enabled } : item
        )
      )
    }
    setSavingId(null)
  }

  async function saveSettings(connection: ConnectionType) {
    if (!selectedClientId) return
    setSavingSettingsId(connection.id)
    const settings = settingsById[connection.id] || {}
    const [error] = await fetchApi(`/v1/connections/types/${connection.id}?clientId=${selectedClientId}`, {
      method: "PUT",
      body: JSON.stringify({ enabled: connection.enabled, settings })
    })

    if (error) {
      setErrorMsg(error.message)
    } else {
      setConnections((prev) =>
        prev.map((item) =>
          item.id === connection.id ? { ...item, settings: JSON.stringify(settings) } : item
        )
      )
    }
    setSavingSettingsId(null)
  }

  const hasApplications = useMemo(() => applications.length > 0, [applications])

  return (
    <div className="flex-1 space-y-6 p-6 md:p-8">
      <div className="flex flex-col gap-4">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight">Social Connections</h1>
          <p className="text-sm text-muted-foreground mt-1">
            Enable social login providers for a specific application.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <label className="text-sm text-muted-foreground">Application</label>
          <select
            className="h-9 rounded-md border bg-background px-3 text-sm"
            value={selectedClientId}
            onChange={(event) => setSelectedClientId(event.target.value)}
            disabled={!hasApplications}
          >
            {applications.map((app) => (
              <option key={app.id} value={app.clientId}>
                {app.clientName}
              </option>
            ))}
          </select>
        </div>
      </div>

      {!hasApplications && !loading && (
        <div className="rounded-lg border bg-card p-6 text-sm text-muted-foreground">
          Create an application to manage social connections.
        </div>
      )}

      {errorMsg && (
        <div className="rounded-md border border-destructive/30 bg-destructive/10 p-3 text-sm text-destructive">
          {errorMsg}
        </div>
      )}

      <div className="space-y-4">
        {loading ? (
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Loader2Icon className="h-4 w-4 animate-spin" />
            Loading connections...
          </div>
        ) : connections.length === 0 ? (
          <div className="rounded-lg border bg-card p-6 text-sm text-muted-foreground">
            No social connections available yet.
          </div>
        ) : (
          connections.map((connection) => {
            const fields = schemaById[connection.id] || []
            const settings = settingsById[connection.id] || {}
            return (
              <div key={connection.id} className="flex flex-col gap-4 rounded-lg border bg-card p-4">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <p className="text-sm font-medium">{connection.name}</p>
                    <p className="text-xs text-muted-foreground">{connection.description}</p>
                  </div>
                  <Button
                    variant={connection.enabled ? "default" : "outline"}
                    size="sm"
                    onClick={() => toggleConnection(connection)}
                    disabled={savingId === connection.id}
                  >
                    {savingId === connection.id ? (
                      <Loader2Icon className="h-4 w-4 animate-spin" />
                    ) : connection.enabled ? (
                      <CheckIcon className="h-4 w-4 mr-1" />
                    ) : null}
                    {connection.enabled ? "Enabled" : "Enable"}
                  </Button>
                </div>

                {fields.length === 0 ? (
                  <div className="rounded-md border bg-muted/30 p-3 text-xs text-muted-foreground">
                    No settings schema provided yet.
                  </div>
                ) : (
                  <div className="space-y-4">
                    <div className="grid gap-4 md:grid-cols-2">
                      {fields.map((field) => (
                        <div key={field.key} className="space-y-1">
                          <Label htmlFor={`${connection.id}-${field.key}`} className="text-xs">
                            {field.label}
                            {field.required ? " *" : ""}
                          </Label>
                          <Input
                            id={`${connection.id}-${field.key}`}
                            type={field.secret ? "password" : field.type || "text"}
                            value={settings[field.key] ?? ""}
                            placeholder={field.placeholder}
                            onChange={(event) =>
                              setSettingsById((prev) => ({
                                ...prev,
                                [connection.id]: {
                                  ...prev[connection.id],
                                  [field.key]: event.target.value
                                }
                              }))
                            }
                          />
                        </div>
                      ))}
                    </div>
                    <div className="flex justify-end">
                      <Button
                        size="sm"
                        onClick={() => saveSettings(connection)}
                        disabled={savingSettingsId === connection.id}
                      >
                        {savingSettingsId === connection.id ? (
                          <Loader2Icon className="mr-2 h-4 w-4 animate-spin" />
                        ) : null}
                        Save settings
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}
