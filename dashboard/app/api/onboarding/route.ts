import { NextResponse } from "next/server"
import { authOptions } from "../auth/[...nextauth]/route"
import { getServerSession } from "next-auth"

export async function POST(request: Request) {
    const session = await getServerSession(authOptions)
    console.log("ACCESS TOKEN:", session?.accessToken)

    const body = await request.json()
    console.log("BODY:", body)

    try {
        const response = await fetch("http://localhost:8080/api/tenant/onboarding", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${session?.accessToken}`
            },
            body: JSON.stringify(body)
        })


        if (!response.ok) {
            return NextResponse.json({ error: "Something went wrong" }, { status: 500 })
        }

        return NextResponse.json({ success: true })
    } catch (e) {
        console.error("ERROR:", e)
        return NextResponse.json({ error: "Something went wrong" }, { status: 500 })
    }
}