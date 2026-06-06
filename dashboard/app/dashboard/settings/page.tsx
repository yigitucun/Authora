"use client"

import * as React from "react"
import { useEffect, useRef, useState } from "react"
import { fetchApi } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useSession } from "next-auth/react"

type TenantSettings = {
  id: string
  name: string
  companyName: string
  usageType: string
  companySize: string
  onboardingCompleted: boolean
}

type SettingsStore = {
  status: "idle" | "loading" | "success" | "error"
  data: TenantSettings | null
  error: string | null
}

let settingsStore: SettingsStore = { status: "idle", data: null, error: null }
const settingsListeners = new Set<() => void>()

function notifySettings() {
  settingsListeners.forEach((listener) => listener())
}

async function fetchSettings() {
  if (settingsStore.status === "loading") return
  settingsStore = { ...settingsStore, status: "loading", error: null }
  notifySettings()

  const [error, data] = await fetchApi<TenantSettings>("/v1/tenant")
  if (error) {
    settingsStore = { status: "error", data: null, error: error.message }
  } else {
    settingsStore = { status: "success", data: data ?? null, error: null }
  }
  notifySettings()
}

async function updateSettings(payload: Omit<TenantSettings, "id" | "onboardingCompleted">) {
  const [error] = await fetchApi("/v1/tenant", {
    method: "PUT",
    body: JSON.stringify(payload)
  })
  if (!error) {
    await fetchSettings()
  }
  return error
}

function subscribeSettings(listener: () => void) {
  settingsListeners.add(listener)
  return () => settingsListeners.delete(listener)
}

function getSettingsSnapshot() {
  return settingsStore
}

export default function TenantSettingsPage() {
  const settingsSnapshot = React.useSyncExternalStore(
    subscribeSettings,
    getSettingsSnapshot,
    getSettingsSnapshot
  )
  const { update } = useSession()

  const nameRef = useRef<HTMLInputElement | null>(null)
  const companyNameRef = useRef<HTMLInputElement | null>(null)
  const [usageType, setUsageType] = useState("")
  const [companySize, setCompanySize] = useState("")
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<string | null>(null)

  useEffect(() => {
    if (settingsSnapshot.status === "idle") {
      fetchSettings()
    }
  }, [settingsSnapshot.status])

  async function handleSave() {
    setSaving(true)
    setMessage(null)
    const usageTypeValue = usageType || settingsSnapshot.data?.usageType || ""
    const companySizeValue = usageTypeValue === "INDIVIDUAL"
      ? ""
      : (companySize || settingsSnapshot.data?.companySize || "")
    const payload = {
      name: nameRef.current?.value ?? "",
      companyName: companyNameRef.current?.value ?? "",
      usageType: usageTypeValue,
      companySize: companySizeValue
    }
    const error = await updateSettings(payload)
    if (error) {
      setMessage(error.message)
    } else {
      const displayName = payload.companyName || payload.name
      await update({ companyName: displayName })
      setMessage("Settings updated")
    }
    setSaving(false)
  }

  if (settingsSnapshot.status === "error") {
    return (
      <div className="p-6 text-sm text-destructive">{settingsSnapshot.error}</div>
    )
  }

  const formKey = settingsSnapshot.data?.id ?? "empty"
  const usageTypeValue = usageType || settingsSnapshot.data?.usageType || ""
  const companySizeValue = companySize || settingsSnapshot.data?.companySize || ""

  return (
    <div className="flex-1 space-y-6 p-6 md:p-8">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">Tenant Settings</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Manage workspace profile details.
        </p>
      </div>

      <div className="grid gap-6 max-w-2xl" key={formKey}>
        <div className="space-y-2">
          <Label htmlFor="workspace-name">Workspace Name</Label>
          <Input
            id="workspace-name"
            defaultValue={settingsSnapshot.data?.name ?? ""}
            ref={nameRef}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="company-name">Company Name</Label>
          <Input
            id="company-name"
            defaultValue={settingsSnapshot.data?.companyName ?? ""}
            ref={companyNameRef}
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="usage-type">Usage Type</Label>
          <select
            id="usage-type"
            value={usageTypeValue}
            onChange={(event) => {
              const nextValue = event.target.value
              setUsageType(nextValue)
              if (nextValue === "INDIVIDUAL") {
                setCompanySize("")
              }
            }}
            className="border border-input bg-background px-3 py-2 text-sm rounded-md"
          >
            <option value="">Select</option>
            <option value="COMPANY">Company</option>
            <option value="INDIVIDUAL">Personal</option>
          </select>
        </div>
        {usageTypeValue !== "INDIVIDUAL" && (
          <div className="space-y-2">
            <Label htmlFor="company-size">Company Size</Label>
            <select
              id="company-size"
              value={companySizeValue}
              onChange={(event) => setCompanySize(event.target.value)}
              className="border border-input bg-background px-3 py-2 text-sm rounded-md"
            >
              <option value="">Select</option>
              <option value="1-10">1-10</option>
              <option value="11-50">11-50</option>
              <option value="51-100">51-100</option>
              <option value="101-500">101-500</option>
              <option value="500+">500+</option>
            </select>
          </div>
        )}

        <div className="flex items-center gap-3">
          <Button onClick={handleSave} disabled={saving}>
            {saving ? "Saving..." : "Save changes"}
          </Button>
          {message && <span className="text-sm text-muted-foreground">{message}</span>}
        </div>
      </div>
    </div>
  )
}
