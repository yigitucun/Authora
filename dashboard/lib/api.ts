import { getSession } from "next-auth/react"

export type ApiResult<T> = [Error | null, T | null]

export async function fetchApi<T>(
    endpoint: string,
    options: RequestInit = {}
): Promise<ApiResult<T>> {
    try {
        const session = await getSession()
        const token = session?.accessToken

        const headers = new Headers(options.headers)
        headers.set("Content-Type", "application/json")

        if (token) {
            headers.set("Authorization", `Bearer ${token}`)
        }

        const response = await fetch(`http://localhost:8080/api${endpoint}`, {
            ...options,
            headers,
        })

        if (!response.ok) {
            return [new Error(`API Hatası: ${response.status} ${response.statusText}`), null]
        }

        if (response.status === 204) {
            return [null, null]
        }

        const contentLength = response.headers.get("content-length")
        if (contentLength === "0") {
            return [null, null]
        }

        const text = await response.text()
        if (!text) {
            return [null, null]
        }

        const data = JSON.parse(text)
        return [null, data as T]

    } catch (error) {
        return [error instanceof Error ? error : new Error("Bilinmeyen bir hata oluştu"), null]
    }
}