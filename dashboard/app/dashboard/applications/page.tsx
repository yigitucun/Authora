"use client"

import * as React from "react"
import { useEffect, useState } from "react"
import { fetchApi } from "@/lib/api"
import { Button } from "@/components/ui/button"
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
    PlusIcon,
    BlocksIcon,
    AlertCircleIcon,
    Loader2Icon,
    MoreHorizontalIcon,
    CopyIcon,
    SettingsIcon,
    TrashIcon,
    CheckIcon,
    ShieldAlertIcon
} from "lucide-react"

type Application = {
    id: string
    clientName: string
    clientId: string
    createdAt: string
    redirectUris: string
}

type CreatedApp = {
    id: string
    clientId: string
    clientSecret: string
    clientName: string
    redirectUri: string
}

export default function ApplicationsPage() {
    const [applications, setApplications] = useState<Application[]>([])
    const [loading, setLoading] = useState(true)
    const [errorMsg, setErrorMsg] = useState<string | null>(null)
    const [createOpen, setCreateOpen] = useState(false)
    const [secretOpen, setSecretOpen] = useState(false)
    const [editOpen, setEditOpen] = useState(false)
    const [deleteOpen, setDeleteOpen] = useState(false)
    const [selectedApp, setSelectedApp] = useState<Application | null>(null)
    const [newAppName, setNewAppName] = useState("")
    const [newRedirectUri, setNewRedirectUri] = useState("")
    const [editName, setEditName] = useState("")
    const [editRedirectUri, setEditRedirectUri] = useState("")
    const [creating, setCreating] = useState(false)
    const [saving, setSaving] = useState(false)
    const [deleting, setDeleting] = useState(false)
    const [createdApp, setCreatedApp] = useState<CreatedApp | null>(null)
    const [copiedField, setCopiedField] = useState<string | null>(null)
    const [formError, setFormError] = useState<string | null>(null)

    useEffect(() => { loadApplications() }, [])

    async function loadApplications() {
        setLoading(true)
        const [error, data] = await fetchApi<Application[]>("/v1/applications")
        if (error) setErrorMsg(error.message)
        else if (data) setApplications(data)
        setLoading(false)
    }

    function isValidRedirectUri(value: string) {
        try {
            const url = new URL(value)
            return url.protocol === "http:" || url.protocol === "https:"
        } catch {
            return false
        }
    }

    async function handleCreate() {
        if (!newAppName || !newRedirectUri) return
        if (!isValidRedirectUri(newRedirectUri)) {
            setFormError("Redirect URI must be a valid http/https URL")
            return
        }
        setFormError(null)
        setCreating(true)
        const [error, data] = await fetchApi<CreatedApp>("/v1/applications", {
            method: "POST",
            body: JSON.stringify({ clientName: newAppName, redirectUri: newRedirectUri })
        })
        if (!error && data) {
            setCreatedApp(data)
            setCreateOpen(false)
            setSecretOpen(true)
            setNewAppName("")
            setNewRedirectUri("")
            await loadApplications()
        }
        setCreating(false)
    }

    async function handleUpdate() {
        if (!selectedApp || !editName || !editRedirectUri) return
        if (!isValidRedirectUri(editRedirectUri)) {
            setFormError("Redirect URI must be a valid http/https URL")
            return
        }
        setFormError(null)
        setSaving(true)
        const [error] = await fetchApi(`/v1/applications/${selectedApp.id}`, {
            method: "PUT",
            body: JSON.stringify({ clientName: editName, redirectUri: editRedirectUri })
        })
        if (error) {
            setErrorMsg(error.message)
        } else {
            setEditOpen(false)
            setSelectedApp(null)
            await loadApplications()
        }
        setSaving(false)
    }

    async function handleDelete() {
        if (!selectedApp) return
        setDeleting(true)
        const [error] = await fetchApi(`/v1/applications/${selectedApp.id}`, {
            method: "DELETE"
        })
        if (error) {
            setErrorMsg(error.message)
        } else {
            setDeleteOpen(false)
            setSelectedApp(null)
            await loadApplications()
        }
        setDeleting(false)
    }

    function copy(text: string, field: string) {
        navigator.clipboard.writeText(text)
        setCopiedField(field)
        setTimeout(() => setCopiedField(null), 2000)
    }

    function formatDate(dateStr: string) {
        return new Date(dateStr).toLocaleDateString("en-US", {
            year: "numeric", month: "short", day: "numeric"
        })
    }

    function openEdit(app: Application) {
        setSelectedApp(app)
        setEditName(app.clientName)
        setEditRedirectUri(app.redirectUris || "")
        setFormError(null)
        setEditOpen(true)
    }

    function openDelete(app: Application) {
        setSelectedApp(app)
        setDeleteOpen(true)
    }

    const authBaseUrl = process.env.NEXT_PUBLIC_AUTH_SERVER_URL || "http://localhost:8080"

    function firstRedirectUri(value: string) {
        if (!value) return ""
        return value.split(",")[0].trim()
    }

    function buildLoginUrl(clientId: string, redirectUris: string) {
        const redirectUri = encodeURIComponent(firstRedirectUri(redirectUris))
        return `${authBaseUrl}/oauth2/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&scope=openid%20profile`
    }

    function buildSignupUrl(clientId: string) {
        return `${authBaseUrl}/sign-up?client_id=${clientId}`
    }

    return (
        <div className="flex-1 space-y-6 p-6 md:p-8">
            <div className="flex items-start justify-between">
                <div>
                    <h1 className="text-2xl font-semibold tracking-tight">Applications</h1>
                    <p className="text-sm text-muted-foreground mt-1">
                        Manage your registered OIDC clients and APIs.
                    </p>
                </div>

                {/* Create Application Dialog */}
                <Dialog open={createOpen} onOpenChange={(open) => { setCreateOpen(open); if (!open) setFormError(null) }}>
                    <DialogTrigger asChild>
                        <Button size="sm">
                            <PlusIcon className="h-4 w-4 mr-2" />
                            Create Application
                        </Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-md">
                        <DialogHeader>
                            <DialogTitle>Create Application</DialogTitle>
                            <DialogDescription>
                                Register a new OIDC client to start authenticating users.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="space-y-4 py-2">
                            <div className="space-y-2">
                                <Label htmlFor="app-name">Application Name</Label>
                                <Input
                                    id="app-name"
                                    placeholder="My Web App"
                                    value={newAppName}
                                    onChange={(e) => setNewAppName(e.target.value)}
                                />
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="redirect-uri">Redirect URI</Label>
                                <Input
                                    id="redirect-uri"
                                    placeholder="https://myapp.com/callback"
                                    value={newRedirectUri}
                                    onChange={(e) => setNewRedirectUri(e.target.value)}
                                />
                                <p className="text-xs text-muted-foreground">
                                    The URI users will be redirected to after login.
                                </p>
                            </div>
                            {formError && (
                                <p className="text-xs text-destructive">{formError}</p>
                            )}
                        </div>
                        <DialogFooter>
                            <Button variant="outline" onClick={() => setCreateOpen(false)}>Cancel</Button>
                            <Button onClick={handleCreate} disabled={creating || !newAppName || !newRedirectUri}>
                                {creating && <Loader2Icon className="h-4 w-4 mr-2 animate-spin" />}
                                Create
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                {/* Secret Dialog */}
                <Dialog open={secretOpen} onOpenChange={(open) => {
                    if (!open) { setSecretOpen(false); setCreatedApp(null) }
                }}>
                    <DialogContent className="sm:max-w-lg">
                        <DialogHeader>
                            <DialogTitle>Application Created</DialogTitle>
                            <DialogDescription>
                                Save your credentials now. The client secret will never be shown again.
                            </DialogDescription>
                        </DialogHeader>

                        <Alert variant="destructive" className="border-yellow-200 bg-yellow-50 text-yellow-800 dark:border-yellow-800 dark:bg-yellow-950 dark:text-yellow-200">
                            <ShieldAlertIcon className="h-4 w-4" />
                            <AlertDescription>
                                Copy and store your client secret securely. You won&apos;t be able to view it again.
                            </AlertDescription>
                        </Alert>

                        <div className="space-y-4">
                            <div className="space-y-1.5">
                                <Label className="text-xs text-muted-foreground">Client ID</Label>
                                <div className="flex items-center gap-2">
                                    <code className="flex-1 text-xs bg-muted px-3 py-2 rounded-md font-mono break-all">
                                        {createdApp?.clientId}
                                    </code>
                                    <Button
                                        variant="outline"
                                        size="icon"
                                        className="h-8 w-8 shrink-0"
                                        onClick={() => copy(createdApp?.clientId || "", "clientId")}
                                    >
                                        {copiedField === "clientId"
                                            ? <CheckIcon className="h-3 w-3 text-green-600" />
                                            : <CopyIcon className="h-3 w-3" />
                                        }
                                    </Button>
                                </div>
                            </div>

                            <div className="space-y-1.5">
                                <Label className="text-xs text-muted-foreground">Client Secret</Label>
                                <div className="flex items-center gap-2">
                                    <code className="flex-1 text-xs bg-muted px-3 py-2 rounded-md font-mono break-all">
                                        {createdApp?.clientSecret}
                                    </code>
                                    <Button
                                        variant="outline"
                                        size="icon"
                                        className="h-8 w-8 shrink-0"
                                        onClick={() => copy(createdApp?.clientSecret || "", "clientSecret")}
                                    >
                                        {copiedField === "clientSecret"
                                            ? <CheckIcon className="h-3 w-3 text-green-600" />
                                            : <CopyIcon className="h-3 w-3" />
                                        }
                                    </Button>
                                </div>
                            </div>
                        </div>

                        {createdApp && (
                            <div className="space-y-3 pt-2">
                                <div className="space-y-1.5">
                                    <Label className="text-xs text-muted-foreground">Hosted Login URL</Label>
                                    <div className="flex items-center gap-2">
                                        <code className="flex-1 text-xs bg-muted px-3 py-2 rounded-md font-mono break-all">
                                            {buildLoginUrl(createdApp.clientId, createdApp.redirectUri)}
                                        </code>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            className="h-8 w-8 shrink-0"
                                            onClick={() => copy(buildLoginUrl(createdApp.clientId, createdApp.redirectUri), "loginUrl")}
                                        >
                                            {copiedField === "loginUrl"
                                                ? <CheckIcon className="h-3 w-3 text-green-600" />
                                                : <CopyIcon className="h-3 w-3" />
                                            }
                                        </Button>
                                    </div>
                                </div>

                                <div className="space-y-1.5">
                                    <Label className="text-xs text-muted-foreground">Hosted Signup URL</Label>
                                    <div className="flex items-center gap-2">
                                        <code className="flex-1 text-xs bg-muted px-3 py-2 rounded-md font-mono break-all">
                                            {buildSignupUrl(createdApp.clientId)}
                                        </code>
                                        <Button
                                            variant="outline"
                                            size="icon"
                                            className="h-8 w-8 shrink-0"
                                            onClick={() => copy(buildSignupUrl(createdApp.clientId), "signupUrl")}
                                        >
                                            {copiedField === "signupUrl"
                                                ? <CheckIcon className="h-3 w-3 text-green-600" />
                                                : <CopyIcon className="h-3 w-3" />
                                            }
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        )}

                        <DialogFooter>
                            <Button onClick={() => { setSecretOpen(false); setCreatedApp(null) }}>
                                I&apos;ve saved my credentials
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                {/* Edit Dialog */}
                <Dialog open={editOpen} onOpenChange={(open) => {
                    setEditOpen(open)
                    if (!open) { setSelectedApp(null); setFormError(null) }
                }}>
                    <DialogContent className="sm:max-w-md">
                        <DialogHeader>
                            <DialogTitle>Edit Application</DialogTitle>
                            <DialogDescription>
                                Update your client name or redirect URI.
                            </DialogDescription>
                        </DialogHeader>
                        <div className="space-y-4 py-2">
                            <div className="space-y-2">
                                <Label htmlFor="edit-app-name">Application Name</Label>
                                <Input
                                    id="edit-app-name"
                                    value={editName}
                                    onChange={(e) => setEditName(e.target.value)}
                                />
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="edit-redirect-uri">Redirect URI</Label>
                                <Input
                                    id="edit-redirect-uri"
                                    value={editRedirectUri}
                                    onChange={(e) => setEditRedirectUri(e.target.value)}
                                />
                            </div>
                            {formError && (
                                <p className="text-xs text-destructive">{formError}</p>
                            )}
                        </div>
                        <DialogFooter>
                            <Button variant="outline" onClick={() => setEditOpen(false)}>Cancel</Button>
                            <Button onClick={handleUpdate} disabled={saving || !editName || !editRedirectUri}>
                                {saving && <Loader2Icon className="h-4 w-4 mr-2 animate-spin" />}
                                Save changes
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>

                {/* Delete Dialog */}
                <Dialog open={deleteOpen} onOpenChange={(open) => {
                    setDeleteOpen(open)
                    if (!open) { setSelectedApp(null) }
                }}>
                    <DialogContent className="sm:max-w-md">
                        <DialogHeader>
                            <DialogTitle>Delete Application</DialogTitle>
                            <DialogDescription>
                                This action cannot be undone. Your client credentials will stop working.
                            </DialogDescription>
                        </DialogHeader>
                        <DialogFooter>
                            <Button variant="outline" onClick={() => setDeleteOpen(false)}>Cancel</Button>
                            <Button variant="destructive" onClick={handleDelete} disabled={deleting}>
                                {deleting && <Loader2Icon className="h-4 w-4 mr-2 animate-spin" />}
                                Delete
                            </Button>
                        </DialogFooter>
                    </DialogContent>
                </Dialog>
            </div>

            <div className="rounded-lg border bg-card">
                {loading ? (
                    <div className="flex flex-col items-center justify-center p-16 text-muted-foreground">
                        <Loader2Icon className="h-8 w-8 animate-spin mb-3" />
                        <p className="text-sm">Loading applications...</p>
                    </div>
                ) : errorMsg ? (
                    <div className="flex flex-col items-center justify-center p-16 text-destructive">
                        <AlertCircleIcon className="h-8 w-8 mb-3" />
                        <p className="text-sm">{errorMsg}</p>
                    </div>
                ) : applications.length === 0 ? (
                    <div className="flex flex-col items-center justify-center p-16 text-muted-foreground">
                        <div className="rounded-full bg-muted p-4 mb-4">
                            <BlocksIcon className="h-8 w-8 opacity-50" />
                        </div>
                        <p className="font-medium mb-1">No applications yet</p>
                        <p className="text-sm mb-4">Create your first application to get started.</p>
                        <Button variant="outline" size="sm" onClick={() => setCreateOpen(true)}>
                            <PlusIcon className="h-4 w-4 mr-2" />
                            Create Application
                        </Button>
                    </div>
                ) : (
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Name</TableHead>
                                <TableHead>Client ID</TableHead>
                                <TableHead>Redirect URI</TableHead>
                                <TableHead>Created</TableHead>
                                <TableHead className="w-[50px]"></TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {applications.map((app) => (
                                <TableRow key={app.id}>
                                    <TableCell>
                                        <div className="flex items-center gap-3">
                                            <div className="h-8 w-8 rounded-md bg-muted flex items-center justify-center">
                                                <BlocksIcon className="h-4 w-4 text-muted-foreground" />
                                            </div>
                                            <span className="font-medium text-sm">{app.clientName}</span>
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <code className="text-xs bg-muted px-2 py-1 rounded font-mono">
                                            {app.clientId}
                                        </code>
                                    </TableCell>
                                    <TableCell>
                                        <span className="text-xs text-muted-foreground truncate max-w-[200px] block">
                                            {app.redirectUris || "-"}
                                        </span>
                                    </TableCell>
                                    <TableCell>
                                        <span className="text-sm text-muted-foreground">
                                            {formatDate(app.createdAt)}
                                        </span>
                                    </TableCell>
                                    <TableCell>
                                        <DropdownMenu>
                                            <DropdownMenuTrigger asChild>
                                                <Button variant="ghost" size="icon" className="h-8 w-8">
                                                    <MoreHorizontalIcon className="h-4 w-4" />
                                                </Button>
                                            </DropdownMenuTrigger>
                                            <DropdownMenuContent align="end">
                                                <DropdownMenuItem onSelect={(event) => {
                                                    event.preventDefault()
                                                    copy(buildLoginUrl(app.clientId, app.redirectUris), `loginUrl-${app.id}`)
                                                }}>
                                                    <CopyIcon className="h-4 w-4 mr-2" />
                                                    Copy Login URL
                                                </DropdownMenuItem>
                                                <DropdownMenuItem onSelect={(event) => {
                                                    event.preventDefault()
                                                    copy(buildSignupUrl(app.clientId), `signupUrl-${app.id}`)
                                                }}>
                                                    <CopyIcon className="h-4 w-4 mr-2" />
                                                    Copy Signup URL
                                                </DropdownMenuItem>
                                                <DropdownMenuSeparator />
                                                <DropdownMenuItem onSelect={(event) => { event.preventDefault(); openEdit(app) }}>
                                                    <SettingsIcon className="h-4 w-4 mr-2" />
                                                    Edit
                                                </DropdownMenuItem>
                                                <DropdownMenuSeparator />
                                                <DropdownMenuItem className="text-destructive" onSelect={(event) => { event.preventDefault(); openDelete(app) }}>
                                                    <TrashIcon className="h-4 w-4 mr-2" />
                                                    Delete
                                                </DropdownMenuItem>
                                            </DropdownMenuContent>
                                        </DropdownMenu>
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