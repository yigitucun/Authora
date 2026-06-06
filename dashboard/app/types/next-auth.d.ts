import "next-auth"
import "next-auth/jwt"

declare module "next-auth" {
    interface Session {
        accessToken?: string
        onboardingCompleted?: boolean
        companyName?: string
        idToken?: string
        dashboardAccess?: boolean
    }

    interface Profile {
        onboarding_completed?: boolean
        company_name?: string
        dashboard_access?: boolean
    }
}

declare module "next-auth/jwt" {
    interface JWT {
        accessToken?: string
        onboardingCompleted?: boolean
        companyName?: string
        idToken?: string
        dashboardAccess?: boolean
    }
}