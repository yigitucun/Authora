import Link from "next/link"
import { Button } from "@/components/ui/button"

export default function AuthErrorPage({
    searchParams,
}: {
    searchParams?: { error?: string }
}) {
    const error = searchParams?.error
    const isAccessDenied = error === "AccessDenied"
    const title = isAccessDenied ? "Dashboard erişimi yok" : "Giris basarisiz"
    const description = isAccessDenied
        ? "Bu hesap dashboard yetkisine sahip degil. Tenant admin hesabi ile giris yapin."
        : "Giris sirasinda beklenmeyen bir hata olustu."

    return (
        <div className="min-h-screen flex items-center justify-center bg-background">
            <div className="max-w-md w-full rounded-xl border bg-card p-8 text-center space-y-4">
                <h1 className="text-xl font-semibold">{title}</h1>
                <p className="text-sm text-muted-foreground">{description}</p>
                <div className="flex items-center justify-center gap-2">
                    <Button asChild variant="secondary">
                        <Link href="/">Dashboard</Link>
                    </Button>
                    <Button asChild>
                        <Link href="/api/auth/signin">Giris yap</Link>
                    </Button>
                </div>
            </div>
        </div>
    )
}

