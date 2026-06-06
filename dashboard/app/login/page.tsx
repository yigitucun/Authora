'use client';

import { signIn } from "next-auth/react";
import { useEffect } from "react";

export default function LoginPage() {
    useEffect(() => {
        signIn("authora", { callbackUrl: "http://localhost:3000/dashboard" })
    }, [])
    return (
        <div className="flex h-screen items-center justify-center bg-gray-50">
            <p className="text-lg font-medium text-gray-600">
                Giriş sistemine yönlendiriliyorsunuz...
            </p>
        </div>
    );
}