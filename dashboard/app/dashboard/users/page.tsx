"use client"

import * as React from "react"
import { useEffect, useState } from "react"
import { fetchApi } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
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
    Avatar,
    AvatarFallback,
} from "@/components/ui/avatar"
import {
    UsersIcon,
    AlertCircleIcon,
    Loader2Icon,
    MoreHorizontalIcon,
    SearchIcon,
    ShieldCheckIcon,
    ShieldOffIcon,
    TrashIcon,
    MailIcon,
} from "lucide-react"

type User = {
    id: string
    email: string
    isVerified: boolean
    createdAt: string
}

type UsersStore = {
    status: "idle" | "loading" | "success" | "error"
    data: User[]
    error: string | null
}

let usersStore: UsersStore = { status: "idle", data: [], error: null }
const usersListeners = new Set<() => void>()

function notifyUsers() {
    usersListeners.forEach((listener) => listener())
}

async function fetchUsersFromApi() {
    if (usersStore.status === "loading") return
    usersStore = { ...usersStore, status: "loading", error: null }
    notifyUsers()

    const [error, data] = await fetchApi<User[]>("/v1/users")
    if (error) {
        usersStore = { status: "error", data: [], error: error.message }
    } else {
        usersStore = { status: "success", data: data ?? [], error: null }
    }
    notifyUsers()
}

async function updateUserVerification(id: string, verified: boolean) {
    const [error] = await fetchApi(`/v1/users/${id}/verify`, {
        method: "PATCH",
        body: JSON.stringify({ verified })
    })
    if (!error) {
        await fetchUsersFromApi()
    }
    return error
}

async function deleteUserById(id: string) {
    const [error] = await fetchApi(`/v1/users/${id}`, { method: "DELETE" })
    if (!error) {
        await fetchUsersFromApi()
    }
    return error
}

function subscribeUsers(listener: () => void) {
    usersListeners.add(listener)
    return () => usersListeners.delete(listener)
}

function getUsersSnapshot() {
    return usersStore
}

export default function UsersPage() {
    const [search, setSearch] = useState("")
    const usersSnapshot = React.useSyncExternalStore(subscribeUsers, getUsersSnapshot, getUsersSnapshot)
    const users = usersSnapshot.data
    const loading = usersSnapshot.status === "idle" || usersSnapshot.status === "loading"
    const errorMsg = usersSnapshot.status === "error" ? usersSnapshot.error : null

    useEffect(() => {
        if (usersSnapshot.status === "idle") {
            fetchUsersFromApi()
        }
    }, [usersSnapshot.status])

    function formatDate(dateStr: string) {
        return new Date(dateStr).toLocaleDateString("en-US", {
            year: "numeric", month: "short", day: "numeric"
        })
    }

    function getInitials(email: string) {
        return email.substring(0, 2).toUpperCase()
    }

    const filtered = users.filter(u =>
        u.email.toLowerCase().includes(search.toLowerCase())
    )

    async function handleToggleVerify(user: User) {
        await updateUserVerification(user.id, !user.isVerified)
    }

    async function handleDelete(user: User) {
        const confirmed = window.confirm(`Delete user ${user.email}?`)
        if (!confirmed) return
        await deleteUserById(user.id)
    }

    return (
        <div className="flex-1 space-y-6 p-6 md:p-8">
            <div className="flex items-start justify-between">
                <div>
                    <h1 className="text-2xl font-semibold tracking-tight">Users</h1>
                    <p className="text-sm text-muted-foreground mt-1">
                        Manage users registered through your applications.
                    </p>
                </div>
                <div className="flex items-center gap-2 text-sm text-muted-foreground bg-muted px-3 py-1.5 rounded-md">
                    <UsersIcon className="h-4 w-4" />
                    <span>{users.length} total</span>
                </div>
            </div>

            <div className="relative">
                <SearchIcon className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                    placeholder="Search by email..."
                    className="pl-9 max-w-sm"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
            </div>

            <div className="rounded-lg border bg-card">
                {loading ? (
                    <div className="flex flex-col items-center justify-center p-16 text-muted-foreground">
                        <Loader2Icon className="h-8 w-8 animate-spin mb-3" />
                        <p className="text-sm">Loading users...</p>
                    </div>
                ) : errorMsg ? (
                    <div className="flex flex-col items-center justify-center p-16 text-destructive">
                        <AlertCircleIcon className="h-8 w-8 mb-3" />
                        <p className="text-sm">{errorMsg}</p>
                    </div>
                ) : filtered.length === 0 ? (
                    <div className="flex flex-col items-center justify-center p-16 text-muted-foreground">
                        <div className="rounded-full bg-muted p-4 mb-4">
                            <UsersIcon className="h-8 w-8 opacity-50" />
                        </div>
                        <p className="font-medium mb-1">
                            {search ? "No users found" : "No users yet"}
                        </p>
                        <p className="text-sm">
                            {search ? "Try a different search term." : "Users will appear here once they register through your applications."}
                        </p>
                    </div>
                ) : (
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>User</TableHead>
                                <TableHead>Status</TableHead>
                                <TableHead>Joined</TableHead>
                                <TableHead className="w-[50px]"></TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {filtered.map((user) => (
                                <TableRow key={user.id}>
                                    <TableCell>
                                        <div className="flex items-center gap-3">
                                            <Avatar className="h-8 w-8">
                                                <AvatarFallback className="text-xs bg-muted">
                                                    {getInitials(user.email)}
                                                </AvatarFallback>
                                            </Avatar>
                                            <div>
                                                <p className="text-sm font-medium">{user.email}</p>
                                                <p className="text-xs text-muted-foreground font-mono">
                                                    {user.id.substring(0, 8)}...
                                                </p>
                                            </div>
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        {user.isVerified ? (
                                            <Badge variant="outline" className="text-green-700 border-green-200 bg-green-50 dark:bg-green-950 dark:border-green-800 dark:text-green-300">
                                                <ShieldCheckIcon className="h-3 w-3 mr-1" />
                                                Verified
                                            </Badge>
                                        ) : (
                                            <Badge variant="outline" className="text-yellow-700 border-yellow-200 bg-yellow-50 dark:bg-yellow-950 dark:border-yellow-800 dark:text-yellow-300">
                                                <ShieldOffIcon className="h-3 w-3 mr-1" />
                                                Unverified
                                            </Badge>
                                        )}
                                    </TableCell>
                                    <TableCell>
                                        <span className="text-sm text-muted-foreground">
                                            {formatDate(user.createdAt)}
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
                                                <DropdownMenuItem onClick={() => handleToggleVerify(user)}>
                                                    <MailIcon className="h-4 w-4 mr-2" />
                                                    {user.isVerified ? "Mark unverified" : "Mark verified"}
                                                </DropdownMenuItem>
                                                <DropdownMenuSeparator />
                                                <DropdownMenuItem className="text-destructive" onClick={() => handleDelete(user)}>
                                                    <TrashIcon className="h-4 w-4 mr-2" />
                                                    Delete user
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