"use client"
import { useState } from "react"
import { useRouter } from "next/navigation"
import { useSession } from "next-auth/react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

type UsageType = "COMPANY" | "INDIVIDUAL" | null
type CompanySize = "1-10" | "11-50" | "51-100" | "101-500" | "500+" | null

export default function OnboardingPage() {
    const router = useRouter()
    const { update } = useSession()

    const [usageType, setUsageType] = useState<UsageType>(null)
    const [companyName, setCompanyName] = useState("")
    const [companySize, setCompanySize] = useState<CompanySize>(null)
    const [loading, setLoading] = useState(false)

    const companySizes = ["1-10", "11-50", "51-100", "101-500", "500+"]

    const handleSubmit = async () => {
        if (!usageType) return
        if (usageType === "COMPANY" && (!companyName || !companySize)) return

        setLoading(true)

        try {
            const response = await fetch("/api/onboarding", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ usageType, companyName, companySize })
            })

            if (!response.ok) throw new Error("Onboarding başarısız")


            await update({
                onboardingCompleted: true,
                companyName: usageType === "COMPANY" ? companyName : "Individual Workspace"
            })
            router.push("/dashboard")

        } catch (e) {
            console.error(e)
            setLoading(false)
        }
    }

    return (
        <div className="min-h-screen flex">
            <div className="hidden lg:flex w-1/2 bg-black text-white flex-col justify-center px-16">
                <h1 className="text-4xl font-bold mb-4">Welcome to Authora</h1>
                <p className="text-gray-400 text-lg">
                    Help us set up your account and start authenticating your users.
                </p>
            </div>

            <div className="flex-1 flex items-center justify-center px-8">
                <div className="w-full max-w-md space-y-8">
                    <div>
                        <h2 className="text-2xl font-semibold">Create Your Account</h2>
                        <p className="text-gray-500 mt-1 text-sm">Tell us a bit about yourself</p>
                    </div>

                    <div className="space-y-3">
                        <Label>Account Type</Label>
                        <div className="grid grid-cols-2 gap-3">
                            <button
                                onClick={() => setUsageType("COMPANY")}
                                className={`border rounded-lg p-4 text-left transition-all ${
                                    usageType === "COMPANY"
                                        ? "border-black bg-black text-white"
                                        : "border-gray-200 hover:border-gray-400"
                                }`}
                            >
                                <div className="text-lg mb-1">🏢</div>
                                <div className="font-medium text-sm">Company</div>
                                <div className="text-xs text-gray-400 mt-0.5">For teams and businesses</div>
                            </button>
                            <button
                                onClick={() => setUsageType("INDIVIDUAL")}
                                className={`border rounded-lg p-4 text-left transition-all ${
                                    usageType === "INDIVIDUAL"
                                        ? "border-black bg-black text-white"
                                        : "border-gray-200 hover:border-gray-400"
                                }`}
                            >
                                <div className="text-lg mb-1">👤</div>
                                <div className="font-medium text-sm">Individual</div>
                                <div className="text-xs text-gray-400 mt-0.5">For personal projects</div>
                            </button>
                        </div>
                    </div>

                    {/* Şirket bilgileri */}
                    {usageType === "COMPANY" && (
                        <>
                            <div className="space-y-2">
                                <Label>Company Name</Label>
                                <Input
                                    placeholder="Acme Inc."
                                    value={companyName}
                                    onChange={(e) => setCompanyName(e.target.value)}
                                />
                            </div>

                            <div className="space-y-3">
                                <Label>Company Size</Label>
                                <div className="grid grid-cols-3 gap-2">
                                    {companySizes.map((size) => (
                                        <button
                                            key={size}
                                            onClick={() => setCompanySize(size as CompanySize)}
                                            className={`border rounded-lg py-2 px-3 text-sm font-medium transition-all ${
                                                companySize === size
                                                    ? "border-black bg-black text-white"
                                                    : "border-gray-200 hover:border-gray-400"
                                            }`}
                                        >
                                            {size}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        </>
                    )}

                    <Button
                        className="w-full"
                        onClick={handleSubmit}
                        disabled={
                            loading ||
                            !usageType ||
                            (usageType === "COMPANY" && (!companyName || !companySize))
                        }
                    >
                        {loading ? "Creating..." : "Create Account →"}
                    </Button>
                </div>
            </div>
        </div>
    )
}