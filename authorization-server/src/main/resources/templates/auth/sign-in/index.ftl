<#import '../layout.ftl' as layout >
<#import '../particials/form.ftl' as form>

<@layout.body title="Sign in">

    <#assign formTitle = isB2C?then("Sign in to " + appName, "Sign in to Authora")>

    <@form.form action="/sign-in" title=formTitle description="Enter your email and password to sign in">

        <#if error??>
            <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4 text-sm">
                ${error}
            </div>
        </#if>
        <#if successMessage??>
            <div class="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded mb-4 text-sm">
                ${successMessage}
            </div>
        </#if>

        <#if _csrf??>
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        </#if>
        <input type="hidden" name="clientId" value="${clientId!''}">

        <@form.input
        name="email"
        type="email"
        label="Email"
        placeholder="m@mail.com" />

        <@form.input
        name="password"
        type="password"
        label="Şifre"
        placeholder="••••••••" />

        <@form.button text="Sign In" />

        <#if connections?? && connections?size gt 0>
            <div class="flex items-center gap-3">
                <div class="h-px flex-1 bg-gray-200"></div>
                <div class="text-center text-xs uppercase tracking-[0.2em] text-gray-400">or continue with</div>
                <div class="h-px flex-1 bg-gray-200"></div>
            </div>
            <div class="grid gap-3">
                <#list connections as connection>
                    <@form.providerButton connection=connection clientId=clientId />
                </#list>
            </div>
        </#if>

        <div class="text-center text-sm text-gray-500">
            Don't have an account?
            <a href="/sign-up<#if clientId?? && clientId != ''>?client_id=${clientId}</#if>" class="underline">
                Sign up
            </a>
        </div>
    </@form.form>

</@layout.body>