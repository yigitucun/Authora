import { NextRequest, NextResponse } from "next/server"
import { getToken } from "next-auth/jwt"

export async function proxy(request: NextRequest) {
    const token = await getToken({ req: request })
    const pathname = request.nextUrl.pathname

    if (!token) {
        return NextResponse.redirect(new URL("/login", request.url))
    }

    if (!token.onboardingCompleted && pathname !== "/dashboard/onboarding") {
        return NextResponse.redirect(new URL("/dashboard/onboarding", request.url))
    }

    if (token.onboardingCompleted && pathname === "/dashboard/onboarding") {
        return NextResponse.redirect(new URL("/dashboard", request.url))
    }

    return NextResponse.next()
}

export const config = {
    matcher: ["/((?!api|_next/static|_next/image|favicon.ico|auth|login).*)"]
}