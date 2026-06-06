import NextAuth, { NextAuthOptions } from "next-auth";

export const authOptions: NextAuthOptions = {
    providers: [
        {
            id: "authora",
            name: "authora",
            type: "oauth",
            clientId: process.env.AUTH_CLIENT_ID,
            clientSecret: process.env.AUTH_CLIENT_SECRET,
            issuer: "http://localhost:8080",
            checks: ["state"],
            jwks_endpoint: "http://localhost:8080/oauth2/jwks",
            authorization: {
                url: 'http://localhost:8080/oauth2/authorize',
                params: {scope: "openid profile"}
            },
            token: "http://localhost:8080/oauth2/token",
            userinfo: "http://localhost:8080/userinfo",
            profile(profile) {
                console.log("PROFILE: ", profile)
                return {
                    id: profile.sub,
                    name: profile.name || profile.email,
                    email: profile.email
                }
            }
        }
    ],
    pages: {
        error: "/auth/error",
    },
    callbacks: {
        async signIn({ profile }) {
            if (profile && Object.prototype.hasOwnProperty.call(profile, "dashboard_access")) {
                return Boolean((profile as { dashboard_access?: boolean }).dashboard_access);
            }
            return false;
        },
        async jwt({token, profile, account, trigger, session}) {
            if (profile) {
                token.onboardingCompleted = profile.onboarding_completed;
                token.companyName = profile.company_name;
                token.dashboardAccess = (profile as { dashboard_access?: boolean }).dashboard_access;
            }
            if (account) {
                token.accessToken = account.access_token;
                token.idToken = account.id_token;
            }

            if (trigger === "update" && session) {
                if (session.onboardingCompleted !== undefined) {
                    token.onboardingCompleted = session.onboardingCompleted;
                }
                if (session.companyName !== undefined) {
                    token.companyName = session.companyName;
                }
            }
            return token;
        },
        async session({session, token}) {
            session.onboardingCompleted = token.onboardingCompleted;
            session.accessToken = token.accessToken;
            session.companyName = token.companyName;
            session.idToken = token.idToken;
            session.dashboardAccess = token.dashboardAccess;
            return session;
        }
    },
}

const handler = NextAuth(authOptions)
export { handler as GET, handler as POST }