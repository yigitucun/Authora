"use client"

import { useState } from "react"
import { cn } from "@/lib/utils"

const sections = [
  { id: "introduction", label: "Introduction" },
  { id: "quickstart", label: "Quick Start" },
  { id: "applications", label: "Applications" },
  { id: "social-connections", label: "Social Connections" },
  { id: "enterprise-sso", label: "Enterprise SSO" },
  { id: "users", label: "User Management" },
  { id: "oidc-flow", label: "OIDC Flow" },
  { id: "tokens", label: "Tokens & Claims" },
  { id: "rate-limiting", label: "Rate Limiting" },
  { id: "api-reference", label: "API Reference" },
]

function Section({ id, title, children }: { id: string; title: string; children: React.ReactNode }) {
  return (
    <section id={id} className="mb-12 scroll-mt-8">
      <h2 className="text-xl font-semibold tracking-tight mb-4 border-b pb-2">{title}</h2>
      <div className="space-y-4 text-sm text-muted-foreground leading-relaxed">{children}</div>
    </section>
  )
}

function Code({ children }: { children: React.ReactNode }) {
  return (
    <code className="bg-muted px-1.5 py-0.5 rounded text-xs font-mono text-foreground">
      {children}
    </code>
  )
}

function CodeBlock({ children, lang = "" }: { children: string; lang?: string }) {
  const [copied, setCopied] = useState(false)
  function copy() {
    navigator.clipboard.writeText(children.trim())
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }
  return (
    <div className="relative group rounded-lg border bg-muted overflow-hidden my-3">
      <div className="flex items-center justify-between px-4 py-2 border-b bg-muted/60 text-xs text-muted-foreground">
        <span>{lang}</span>
        <button
          onClick={copy}
          className="opacity-0 group-hover:opacity-100 transition-opacity hover:text-foreground"
        >
          {copied ? "Copied!" : "Copy"}
        </button>
      </div>
      <pre className="p-4 overflow-x-auto text-xs leading-relaxed font-mono text-foreground">
        {children.trim()}
      </pre>
    </div>
  )
}

function Badge({ children, variant = "default" }: { children: React.ReactNode; variant?: "default" | "green" | "blue" | "yellow" }) {
  return (
    <span className={cn(
      "inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium",
      variant === "default" && "bg-muted text-muted-foreground",
      variant === "green" && "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400",
      variant === "blue" && "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400",
      variant === "yellow" && "bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400",
    )}>
      {children}
    </span>
  )
}

function ApiEndpoint({ method, path, description }: { method: string; path: string; description: string }) {
  const color = {
    GET: "bg-blue-100 text-blue-700",
    POST: "bg-green-100 text-green-700",
    PUT: "bg-yellow-100 text-yellow-700",
    DELETE: "bg-red-100 text-red-700",
  }[method] ?? "bg-muted text-muted-foreground"

  return (
    <div className="flex items-start gap-3 rounded-lg border p-3 my-2">
      <span className={cn("rounded px-2 py-0.5 text-xs font-mono font-bold shrink-0", color)}>{method}</span>
      <div>
        <code className="text-xs font-mono text-foreground">{path}</code>
        <p className="text-xs text-muted-foreground mt-0.5">{description}</p>
      </div>
    </div>
  )
}

export default function DocsPage() {
  const [active, setActive] = useState("introduction")

  return (
    <div className="flex min-h-screen w-full">
      {/* Sidebar nav */}
      <aside className="hidden lg:flex flex-col w-52 shrink-0 sticky top-0 h-screen border-r pt-8 pb-4 px-4 overflow-y-auto">
        <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">On this page</p>
        <nav className="space-y-1">
          {sections.map((s) => (
            <a
              key={s.id}
              href={`#${s.id}`}
              onClick={() => setActive(s.id)}
              className={cn(
                "block text-sm py-1 px-2 rounded transition-colors hover:bg-muted",
                active === s.id ? "text-foreground font-medium bg-muted" : "text-muted-foreground"
              )}
            >
              {s.label}
            </a>
          ))}
        </nav>
      </aside>

      {/* Content — centered with max width */}
      <main className="flex-1 flex justify-center">
        <div className="w-full max-w-3xl px-6 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold tracking-tight">Authora Documentation</h1>
          <p className="mt-2 text-muted-foreground">
            Multi-tenant OIDC Identity Provider for SaaS applications.
          </p>
        </div>

        {/* INTRODUCTION */}
        <Section id="introduction" title="Introduction">
          <p>
            Authora is a multi-tenant Identity Provider (IdP) built on{" "}
            <strong className="text-foreground">Spring Authorization Server</strong> and{" "}
            <strong className="text-foreground">OAuth 2.0 / OIDC</strong>. It lets you add authentication
            to your SaaS product without building auth from scratch.
          </p>
          <p>
            Each customer (tenant) gets their own isolated workspace. Tenant admins manage their users,
            register OIDC client applications, and configure social login providers — all from the
            dashboard.
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 mt-4">
            {[
              { title: "Multi-tenant", desc: "Full data isolation per tenant" },
              { title: "OIDC / OAuth 2.0", desc: "Standards-compliant token flows" },
              { title: "Social Login", desc: "Google, GitHub, Facebook" },
            ].map((f) => (
              <div key={f.title} className="rounded-lg border p-3">
                <p className="font-medium text-foreground text-xs">{f.title}</p>
                <p className="text-xs mt-1">{f.desc}</p>
              </div>
            ))}
          </div>
        </Section>

        {/* QUICK START */}
        <Section id="quickstart" title="Quick Start">
          <p>Get your application authenticating with Authora in 3 steps.</p>

          <div className="space-y-4">
            <div className="rounded-lg border p-4">
              <p className="font-medium text-foreground text-xs mb-2">Step 1 — Register your application</p>
              <p>Go to <strong className="text-foreground">Applications → Create Application</strong> in the dashboard. Save the <Code>client_id</Code> and <Code>client_secret</Code> — the secret is only shown once.</p>
            </div>

            <div className="rounded-lg border p-4">
              <p className="font-medium text-foreground text-xs mb-2">Step 2 — Redirect users to the login page</p>
              <CodeBlock lang="URL">
{`https://auth.yourdomain.com/oauth2/authorize
  ?response_type=code
  &client_id=YOUR_CLIENT_ID
  &redirect_uri=https://yourapp.com/callback
  &scope=openid profile`}
              </CodeBlock>
            </div>

            <div className="rounded-lg border p-4">
              <p className="font-medium text-foreground text-xs mb-2">Step 3 — Exchange the code for tokens</p>
              <CodeBlock lang="Shell">
{`curl -X POST https://auth.yourdomain.com/oauth2/token \\
  -u "YOUR_CLIENT_ID:YOUR_CLIENT_SECRET" \\
  -d "grant_type=authorization_code" \\
  -d "code=AUTHORIZATION_CODE" \\
  -d "redirect_uri=https://yourapp.com/callback"`}
              </CodeBlock>
              <p className="mt-2">The response contains <Code>access_token</Code>, <Code>id_token</Code>, and <Code>refresh_token</Code>.</p>
            </div>
          </div>
        </Section>

        {/* APPLICATIONS */}
        <Section id="applications" title="Applications">
          <p>
            An <strong className="text-foreground">Application</strong> is an OIDC client registered under your tenant.
            Each application gets a unique <Code>client_id</Code> and <Code>client_secret</Code>.
          </p>

          <div className="rounded-lg border divide-y mt-3">
            {[
              { field: "client_id", desc: "Unique identifier for your application. Public — safe to use in URLs." },
              { field: "client_secret", desc: "Secret key for server-to-server token exchange. Never expose in frontend code." },
              { field: "redirect_uri", desc: "The callback URL users are sent to after login. Must exactly match what was registered." },
              { field: "client_name", desc: "Display name shown on the login page when your users authenticate." },
            ].map((row) => (
              <div key={row.field} className="flex gap-3 px-3 py-2.5 text-xs">
                <Code>{row.field}</Code>
                <span className="text-muted-foreground">{row.desc}</span>
              </div>
            ))}
          </div>

          <p className="mt-3">
            After creating an application you can copy the pre-built <strong className="text-foreground">Hosted Login URL</strong> and
            <strong className="text-foreground"> Hosted Signup URL</strong> directly from the dashboard — no manual URL construction needed.
          </p>
        </Section>

        {/* SOCIAL CONNECTIONS */}
        <Section id="social-connections" title="Social Connections">
          <p>
            Social connections let your users sign in with their Google, GitHub, or Facebook accounts.
            Each connection is configured per application.
          </p>

          <div className="space-y-3 mt-2">
            {[
              { provider: "Google", fields: ["Client ID", "Client Secret", "Redirect URI"] },
              { provider: "GitHub", fields: ["Client ID", "Client Secret", "Redirect URI"] },
              { provider: "Facebook", fields: ["Client ID", "Client Secret", "Redirect URI"] },
            ].map((p) => (
              <div key={p.provider} className="rounded-lg border p-3">
                <div className="flex items-center gap-2 mb-2">
                  <span className="font-medium text-foreground text-xs">{p.provider}</span>
                  <Badge variant="green">OAuth 2.0</Badge>
                </div>
                <p className="text-xs mb-1">Required fields: {p.fields.map((f) => <Code key={f}>{f}</Code>).reduce((a, b) => <>{a}{" "}{b}</>)}</p>
              </div>
            ))}
          </div>

          <p className="mt-3">
            When a social connection is enabled, a <strong className="text-foreground">&quot;Continue with…&quot;</strong> button
            automatically appears on your application&apos;s login page. New users who sign in via social are
            auto-provisioned into your tenant.
          </p>

          <div className="rounded-lg border bg-yellow-50 dark:bg-yellow-900/20 border-yellow-200 dark:border-yellow-800 p-3 mt-3">
            <p className="text-xs text-yellow-800 dark:text-yellow-300">
              <strong>Note:</strong> You need to create a Google/GitHub/Facebook OAuth App and set{" "}
              <Code>{`https://auth.yourdomain.com/login/oauth2/code/{provider}`}</Code> as the authorized redirect URI in
              the provider&apos;s developer console.
            </p>
          </div>
        </Section>

        {/* ENTERPRISE SSO */}
        <Section id="enterprise-sso" title="Enterprise SSO">
          <p>
            Enterprise SSO allows your B2B customers to log in using their corporate identity provider via{" "}
            <strong className="text-foreground">SAML 2.0</strong>.
          </p>

          <div className="rounded-lg border p-3 mt-2">
            <div className="flex items-center gap-2 mb-2">
              <span className="font-medium text-foreground text-xs">SAML 2.0</span>
              <Badge variant="blue">Enterprise</Badge>
            </div>
            <div className="space-y-1 text-xs">
              {[
                ["Metadata URL", "Your IdP's SAML metadata endpoint"],
                ["Entity ID", "The unique identifier for your service provider"],
                ["ACS URL", "Assertion Consumer Service URL — where SAML responses are posted"],
                ["Certificate", "X.509 certificate from your IdP for signature verification"],
              ].map(([field, desc]) => (
                <div key={field} className="flex gap-2">
                  <Code>{field}</Code>
                  <span className="text-muted-foreground">{desc}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="rounded-lg border bg-muted/50 p-3 mt-3">
            <p className="text-xs text-muted-foreground">
              <strong className="text-foreground">Coming soon:</strong> SAML configuration UI is available but the authentication flow is under active development.
            </p>
          </div>
        </Section>

        {/* USER MANAGEMENT */}
        <Section id="users" title="User Management">
          <p>
            Users are scoped to a tenant. A user can only exist in one tenant and can only authenticate
            through that tenant&apos;s registered applications.
          </p>

          <div className="rounded-lg border divide-y mt-3">
            {[
              { field: "email", desc: "Unique within a tenant. Used as the primary identifier." },
              { field: "is_verified", desc: "Email must be verified before login is allowed. Verification link is sent on signup." },
              { field: "is_tenant_admin", desc: "Grants access to the Authora dashboard for this tenant." },
              { field: "tenant_id", desc: "The tenant this user belongs to. Set automatically based on which application they signed up through." },
            ].map((row) => (
              <div key={row.field} className="flex gap-3 px-3 py-2.5 text-xs">
                <Code>{row.field}</Code>
                <span className="text-muted-foreground">{row.desc}</span>
              </div>
            ))}
          </div>

          <p className="mt-3">
            <strong className="text-foreground">B2B signup:</strong> When a user registers through the Authora dashboard itself
            (no <Code>client_id</Code>), a new tenant workspace is automatically created for them.
          </p>
          <p>
            <strong className="text-foreground">B2C signup:</strong> When a user registers through your application
            (with a <Code>client_id</Code>), they are added to your tenant as a regular user.
          </p>
        </Section>

        {/* OIDC FLOW */}
        <Section id="oidc-flow" title="OIDC Flow">
          <p>
            Authora implements the <strong className="text-foreground">Authorization Code flow</strong> with optional Refresh Token support.
          </p>

          <div className="rounded-lg border p-4 mt-2 space-y-3">
            {[
              { step: "1", title: "Authorization Request", desc: "Your app redirects the user to /oauth2/authorize with client_id, redirect_uri, and scope." },
              { step: "2", title: "User Authentication", desc: "Authora shows the login page (optionally branded with your app name). The user enters credentials or uses social login." },
              { step: "3", title: "Authorization Code Issued", desc: "On success, Authora redirects back to your redirect_uri with a short-lived code." },
              { step: "4", title: "Token Exchange", desc: "Your server POSTs to /oauth2/token with the code and client credentials. Authora returns access_token, id_token, and refresh_token." },
              { step: "5", title: "Use the Token", desc: "Include the access_token as a Bearer token in API requests. Use the id_token to identify the user in your application." },
            ].map((item) => (
              <div key={item.step} className="flex gap-3">
                <div className="w-6 h-6 rounded-full bg-muted flex items-center justify-center text-xs font-medium shrink-0">{item.step}</div>
                <div>
                  <p className="font-medium text-foreground text-xs">{item.title}</p>
                  <p className="text-xs text-muted-foreground mt-0.5">{item.desc}</p>
                </div>
              </div>
            ))}
          </div>

          <p className="mt-3">Authora exposes standard OIDC discovery at:</p>
          <CodeBlock lang="URL">
{`https://auth.yourdomain.com/.well-known/openid-configuration`}
          </CodeBlock>
        </Section>

        {/* TOKENS */}
        <Section id="tokens" title="Tokens & Claims">
          <p>
            Authora issues <strong className="text-foreground">JWT</strong> access and ID tokens signed with an RSA key pair.
          </p>

          <p className="mt-2 font-medium text-foreground text-xs">Standard claims (all tokens)</p>
          <div className="rounded-lg border divide-y mt-1">
            {[
              { claim: "sub", desc: "User's email address" },
              { claim: "email", desc: "User's email address" },
              { claim: "iat / exp", desc: "Issued at / Expiry timestamps" },
            ].map((row) => (
              <div key={row.claim} className="flex gap-3 px-3 py-2 text-xs">
                <Code>{row.claim}</Code>
                <span className="text-muted-foreground">{row.desc}</span>
              </div>
            ))}
          </div>

          <p className="mt-3 font-medium text-foreground text-xs">Dashboard client claims (<Code>authora-dashboard</Code>)</p>
          <div className="rounded-lg border divide-y mt-1">
            {[
              { claim: "dashboard_access", desc: "Boolean — whether this user is a tenant admin with dashboard access" },
              { claim: "onboarding_completed", desc: "Boolean — whether the tenant has completed onboarding" },
              { claim: "company_name", desc: "The tenant's company name (set during onboarding)" },
            ].map((row) => (
              <div key={row.claim} className="flex gap-3 px-3 py-2 text-xs">
                <Code>{row.claim}</Code>
                <span className="text-muted-foreground">{row.desc}</span>
              </div>
            ))}
          </div>

          <p className="mt-3 font-medium text-foreground text-xs">B2C application claims</p>
          <div className="rounded-lg border divide-y mt-1">
            {[
              { claim: "tenant_id", desc: "UUID of the tenant the user belongs to" },
              { claim: "email", desc: "Verified email address" },
            ].map((row) => (
              <div key={row.claim} className="flex gap-3 px-3 py-2 text-xs">
                <Code>{row.claim}</Code>
                <span className="text-muted-foreground">{row.desc}</span>
              </div>
            ))}
          </div>

          <p className="mt-3">Token lifetimes:</p>
          <div className="rounded-lg border divide-y mt-1">
            {[
              ["Access Token", "1 hour"],
              ["Refresh Token", "7 days (rotated on use)"],
              ["Email Verification Link", "30 minutes"],
            ].map(([type, ttl]) => (
              <div key={type} className="flex gap-3 px-3 py-2 text-xs">
                <span className="text-foreground font-medium w-44 shrink-0">{type}</span>
                <span className="text-muted-foreground">{ttl}</span>
              </div>
            ))}
          </div>
        </Section>

        {/* RATE LIMITING */}
        <Section id="rate-limiting" title="Rate Limiting">
          <p>
            Authora applies rate limiting to the login and signup endpoints to prevent brute-force attacks.
            Limits are tracked per IP address in Redis.
          </p>

          <div className="rounded-lg border divide-y mt-3">
            {[
              ["Endpoint", "Limit", "Window", "Block Duration"],
              ["/sign-in (POST)", "10 requests", "1 minute", "15 minutes"],
              ["/sign-up (POST)", "10 requests", "1 minute", "15 minutes"],
            ].map((row, i) => (
              <div key={i} className={cn("grid grid-cols-4 gap-2 px-3 py-2 text-xs", i === 0 && "font-medium text-foreground")}>
                {row.map((cell, j) => (
                  <span key={j} className={i === 0 ? "" : "text-muted-foreground"}>{cell}</span>
                ))}
              </div>
            ))}
          </div>

          <p className="mt-3">
            When the limit is exceeded the request is redirected back to the form with <Code>?error=ratelimit</Code>.
            No special client configuration is required — rate limiting is fully server-side.
          </p>
        </Section>

        {/* API REFERENCE */}
        <Section id="api-reference" title="API Reference">
          <p>
            All dashboard API endpoints are under <Code>/api/v1/</Code> and require a valid Bearer token
            issued by Authora for the <Code>authora-dashboard</Code> client.
          </p>

          <p className="font-medium text-foreground mt-4 mb-1">Applications</p>
          <ApiEndpoint method="GET" path="/api/v1/applications" description="List all registered OIDC clients for the authenticated tenant." />
          <ApiEndpoint method="POST" path="/api/v1/applications" description="Register a new OIDC client. Returns client_id and client_secret (shown once)." />
          <ApiEndpoint method="PUT" path="/api/v1/applications/{id}" description="Update application name or redirect URI." />
          <ApiEndpoint method="DELETE" path="/api/v1/applications/{id}" description="Delete an application and revoke its credentials." />

          <p className="font-medium text-foreground mt-4 mb-1">Connections</p>
          <ApiEndpoint method="GET" path="/api/v1/connections/types?clientId=" description="List connection types for an application. Pass social=true for social providers, social=false for SSO." />
          <ApiEndpoint method="PUT" path="/api/v1/connections/types/{typeId}?clientId=" description="Enable/disable a connection and save its settings (client credentials)." />

          <p className="font-medium text-foreground mt-4 mb-1">Users</p>
          <ApiEndpoint method="GET" path="/api/v1/users" description="List all users in the authenticated tenant." />

          <p className="font-medium text-foreground mt-4 mb-1">Audit Logs</p>
          <ApiEndpoint method="GET" path="/api/v1/audit-logs" description="Retrieve audit log entries for the tenant. Sorted by most recent." />

          <p className="font-medium text-foreground mt-4 mb-1">Tenant</p>
          <ApiEndpoint method="POST" path="/tenant/onboarding" description="Complete onboarding — set company name, usage type, and company size." />

          <p className="font-medium text-foreground mt-4 mb-1">OIDC Endpoints (Authorization Server)</p>
          <ApiEndpoint method="GET" path="/oauth2/authorize" description="Start the Authorization Code flow. Redirects to the login page." />
          <ApiEndpoint method="POST" path="/oauth2/token" description="Exchange authorization code for tokens. Requires Basic auth with client credentials." />
          <ApiEndpoint method="GET" path="/userinfo" description="Return claims for the authenticated user. Requires Bearer access token." />
          <ApiEndpoint method="GET" path="/oauth2/jwks" description="JSON Web Key Set — public keys for verifying JWTs." />
          <ApiEndpoint method="GET" path="/.well-known/openid-configuration" description="OIDC discovery document with all endpoint URLs." />

          <div className="rounded-lg border bg-muted/50 p-3 mt-4">
            <p className="text-xs text-muted-foreground">
              <strong className="text-foreground">Authorization:</strong> All <Code>/api/</Code> endpoints validate the Bearer token
              as a JWT issued by this authorization server. The token must have been issued for the
              <Code>authora-dashboard</Code> client and the authenticated user must be a tenant admin.
            </p>
          </div>
        </Section>
        </div>
      </main>
    </div>

  )
}
