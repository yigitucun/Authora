<#import '../layout.ftl' as layout >
<#import '../particials/form.ftl' as form>

<@layout.body title="Sign up">

    <#assign formTitle = (isB2C?? && isB2C && appName?? && appName?has_content)?then("Create Your " + appName + " Account", "Create Your Authora Account")>
    <#assign formDescription = (isB2C?? && isB2C && appName?? && appName?has_content)?then("Enter your email below to create your " + appName + " account", "Enter your email below to create your account")>

    <@form.form action="/sign-up" title=formTitle description=formDescription>
        <#if _csrf??>
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
        </#if>
        <input type="hidden" name="clientId" value="${clientId!''}">
        <#if error??>
            <div style="color: #b91c1c; background-color: #fee2e2; border: 1px solid #f87171; padding: 10px; border-radius: 5px; margin-bottom: 15px; font-size: 14px; text-align: center;">
                ${error}
            </div>
        </#if>
        <@form.input
        name="email"
        description="We'll use this to contact you. We will not share your email with anyone else."
        type="email"
        label="Email"
        value=email
        placeholder="m@mail.com"
        errors=(errors??)?then(errors.getFieldErrors('email'), [])/>


        <@form.input
        name="password"
        type="password"
        label="Şifre"
        description="Must be at least 8 characters long."
        placeholder="••••••••"
        errors=(errors??)?then(errors.getFieldErrors('password'), []) />



        <@form.button text="Create Account" />

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
            Already have an account?
            <a href="/sign-in<#if clientId?? && clientId != ''>?client_id=${clientId}</#if>" class="underline">Sign in</a>
        </div>
    </@form.form>

</@layout.body>