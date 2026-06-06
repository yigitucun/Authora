<#macro form action="" title="" description="" method="POST" id="" class="">
    <div class="bg-white shadow-sm max-w-md rounded-lg px-10 py-8 flex flex-col gap-5">
        <div class="flex flex-col gap-4 items-center">
            <div><h1 class="text-3xl font-semibold">Authora</h1></div>
            <div><h2 class="text-2xl">${title}</h2></div>
            <div><h3 class="text-gray-500 text-center">${description}</h3></div>
        </div>

        <form class="flex flex-col gap-5<#if class?has_content> ${class}</#if>" action="${action}" method="${method}"<#if id?has_content> id="${id}"</#if>>
            <#nested>
        </form>
    </div>
</#macro>

<#macro input name type="text" id=name label="" value="" errors=[] description="" class="form-control" attrs...>
        <div class="flex flex-col gap-2">
            <#if label?has_content>
                <label for="${id}">${label}</label>
            </#if>
            <input type="${type}"
                   name="${name}"
                   id="${id}"
                   value="${value}"
                   class="border border-gray-300 px-3 py-1 rounded-md"
            <#list attrs as key, val>${key}="${val}" </#list> >
            <#if errors?has_content>
                <#list errors as err>
                    <p class="text-sm text-red-500">• ${err.defaultMessage}</p>
                </#list>
            <#elseif description?has_content>
                <p class="text-sm text-gray-500">${description}</p>
            </#if>

        </div>
</#macro>

<#macro button type="submit" class="bg-black py-2 rounded-md cursor-pointer text-white w-full" text="Gönder">
    <button type="${type}" class="${class}">${text}</button>
</#macro>

<#macro providerButton connection clientId="">
    <#assign providerKey = connection.name?lower_case>
    <#assign iconStyle = "background: #ffffff; border: 1px solid #e5e7eb; color: #111827;">
    <#assign iconLabelStyle = "font-weight: 800; font-size: 12px; letter-spacing: -0.02em;">
    <#assign fallbackLabel = "?">
    <#assign registrationId = "">

    <#if connection.name?? && connection.name?has_content>
        <#assign fallbackLabel = connection.name?substring(0, 1)?upper_case>
    </#if>

    <#if providerKey?contains("google")>
        <#assign registrationId = "google">
        <#assign iconStyle = "background: #ffffff; border: 1px solid #e5e7eb;">
        <#assign iconLabelStyle = "font-weight: 800; font-size: 13px; letter-spacing: -0.04em;">
    <#elseif providerKey?contains("facebook")>
        <#assign registrationId = "facebook">
        <#assign iconStyle = "background: #1877f2; border: 1px solid #1877f2; color: #ffffff;">
        <#assign iconLabelStyle = "font-weight: 800; font-size: 18px; line-height: 1; margin-top: -1px; color: #ffffff;">
    <#elseif providerKey?contains("github")>
        <#assign registrationId = "github">
        <#assign iconStyle = "background: #111827; border: 1px solid #111827; color: #ffffff;">
        <#assign iconLabelStyle = "font-weight: 800; font-size: 11px; letter-spacing: 0.02em; color: #ffffff;">
    <#elseif providerKey?contains("saml") || providerKey?contains("okta") || providerKey?contains("azure") || providerKey?contains("microsoft")>
        <#assign iconStyle = "background: #ffffff; border: 1px solid #e5e7eb; color: #111827;">
        <#assign iconLabelStyle = "font-weight: 800; font-size: 11px; letter-spacing: 0.02em; color: #111827;">
    </#if>

    <#assign href = "">
    <#if registrationId?has_content>
        <#assign finalRegistrationId = registrationId>
        <#if clientId?? && clientId?has_content>
            <#assign finalRegistrationId = registrationId + "__" + clientId>
        </#if>
        <#assign href = "/oauth2/authorization/" + finalRegistrationId>
    </#if>

    <#assign buttonClass = "group flex w-full items-center gap-3 rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-left text-sm font-medium text-gray-900 shadow-sm transition duration-200 hover:border-gray-300 hover:bg-gray-50">

    <#if href?has_content>
        <a href="${href}" class="${buttonClass}" role="button">
            <span class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-full" style="${iconStyle}">
                <#if providerKey?contains("google")>
                    <svg viewBox="0 0 24 24" aria-hidden="true" class="h-4 w-4">
                        <path fill="#EA4335" d="M12 10.2v3.95h5.62c-.24 1.28-1.47 3.74-5.62 3.74-3.38 0-6.14-2.8-6.14-6.26S8.62 5.37 12 5.37c1.93 0 3.23.82 3.97 1.52l2.7-2.6C16.95 2.7 14.7 1.75 12 1.75 6.98 1.75 2.9 5.83 2.9 10.86S6.98 20 12 20c5.3 0 8.81-3.72 8.81-8.96 0-.6-.06-1.05-.13-1.5H12Z"/>
                        <path fill="#FBBC05" d="M4.04 7.29 7.17 9.57c.84-2.64 3.28-4.2 4.83-4.2 1.82 0 3 .83 3.9 1.52l2.8-2.69C16.95 2.7 14.74 1.75 12 1.75 8.05 1.75 4.66 4.11 4.04 7.29Z"/>
                        <path fill="#34A853" d="M12 20c2.68 0 4.93-.88 6.58-2.39l-3.04-2.5c-.82.56-1.94 1.04-3.54 1.04-2.72 0-5.03-1.8-5.86-4.28l-3.08 2.38C4.61 17.79 8.03 20 12 20Z"/>
                        <path fill="#4285F4" d="M7.15 12.1c-.2-.58-.32-1.2-.32-1.84 0-.64.12-1.26.31-1.84l-3.1-2.39A8.2 8.2 0 0 0 2.9 10.86c0 1.34.32 2.61.87 3.75l3.38-2.51Z"/>
                    </svg>
                <#elseif providerKey?contains("facebook")>
                    <svg viewBox="0 0 24 24" aria-hidden="true" class="h-4 w-4 fill-white">
                        <path d="M13.5 22v-8.3h2.8l.4-3.2h-3.2V8.4c0-.9.2-1.5 1.6-1.5h1.7V4c-.3 0-1.4-.1-2.6-.1-2.5 0-4.2 1.5-4.2 4.3v2.3H7v3.2h2.9V22h3.6Z"/>
                    </svg>
                <#elseif providerKey?contains("github")>
                    <svg viewBox="0 0 24 24" aria-hidden="true" class="h-4 w-4 fill-white">
                        <path d="M12 2.25a9.75 9.75 0 0 0-3.08 19c.49.1.67-.2.67-.47v-1.66c-2.75.6-3.33-1.17-3.33-1.17-.45-1.15-1.1-1.46-1.1-1.46-.9-.62.07-.61.07-.61 1 .07 1.53 1.04 1.53 1.04.88 1.51 2.3 1.07 2.86.82.09-.64.35-1.07.63-1.31-2.2-.25-4.52-1.1-4.52-4.88 0-1.08.38-1.96 1.02-2.65-.1-.25-.44-1.25.1-2.6 0 0 .84-.27 2.75 1.01a9.59 9.59 0 0 1 5 0c1.9-1.28 2.74-1.01 2.74-1.01.55 1.35.2 2.35.1 2.6.64.69 1.02 1.57 1.02 2.65 0 3.79-2.33 4.63-4.55 4.88.36.31.68.92.68 1.86v2.76c0 .28.18.58.68.47A9.75 9.75 0 0 0 12 2.25Z"/>
                    </svg>
                <#elseif providerKey?contains("saml") || providerKey?contains("okta") || providerKey?contains("azure") || providerKey?contains("microsoft")>
                    <svg viewBox="0 0 24 24" aria-hidden="true" class="h-4 w-4 fill-current">
                        <path d="M11.1 2.5 3 6.4v11.2l8.1 3.9 9-4.3V5.9l-9-3.4Zm-.1 2.1 6.6 2.5-6.6 3.2-6.1-2.7 6.1-3Zm-6 5.4 5.4 2.4v6.7l-5.4-2.6V10Zm7.4 9.1v-6.7l6.6-3.2v8.3l-6.6 1.6Z"/>
                    </svg>
                <#else>
                    <span style="${iconLabelStyle}">${fallbackLabel}</span>
                </#if>
            </span>
            <span class="min-w-0 uppercase tracking-wide">Continue with ${connection.name}</span>
        </a>
    <#else>
        <button type="button"
                class="${buttonClass} disabled:cursor-not-allowed disabled:opacity-100"
                title="Yakında desteklenecek"
                aria-disabled="true"
                disabled>
            <span class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-full" style="${iconStyle}">
                <#if providerKey?contains("google")>
                    <svg viewBox="0 0 24 24" aria-hidden="true" class="h-4 w-4">
                        <path fill="#EA4335" d="M12 10.2v3.95h5.62c-.24 1.28-1.47 3.74-5.62 3.74-3.38 0-6.14-2.8-6.14-6.26S8.62 5.37 12 5.37c1.93 0 3.23.82 3.97 1.52l2.7-2.6C16.95 2.7 14.7 1.75 12 1.75 6.98 1.75 2.9 5.83 2.9 10.86S6.98 20 12 20c5.3 0 8.81-3.72 8.81-8.96 0-.6-.06-1.05-.13-1.5H12Z"/>
                        <path fill="#FBBC05" d="M4.04 7.29 7.17 9.57c.84-2.64 3.28-4.2 4.83-4.2 1.82 0 3 .83 3.9 1.52l2.8-2.69C16.95 2.7 14.74 1.75 12 1.75 8.05 1.75 4.66 4.11 4.04 7.29Z"/>
                        <path fill="#34A853" d="M12 20c2.68 0 4.93-.88 6.58-2.39l-3.04-2.5c-.82.56-1.94 1.04-3.54 1.04-2.72 0-5.03-1.8-5.86-4.28l-3.08 2.38C4.61 17.79 8.03 20 12 20Z"/>
                        <path fill="#4285F4" d="M7.15 12.1c-.2-.58-.32-1.2-.32-1.84 0-.64.12-1.26.31-1.84l-3.1-2.39A8.2 8.2 0 0 0 2.9 10.86c0 1.34.32 2.61.87 3.75l3.38-2.51Z"/>
                    </svg>
                <#elseif providerKey?contains("facebook")>
                    <svg viewBox="0 0 24 24" aria-hidden="true" class="h-4 w-4 fill-white">
                        <path d="M13.5 22v-8.3h2.8l.4-3.2h-3.2V8.4c0-.9.2-1.5 1.6-1.5h1.7V4c-.3 0-1.4-.1-2.6-.1-2.5 0-4.2 1.5-4.2 4.3v2.3H7v3.2h2.9V22h3.6Z"/>
                    </svg>
                <#elseif providerKey?contains("github")>
                    <svg viewBox="0 0 24 24" aria-hidden="true" class="h-4 w-4 fill-white">
                        <path d="M12 2.25a9.75 9.75 0 0 0-3.08 19c.49.1.67-.2.67-.47v-1.66c-2.75.6-3.33-1.17-3.33-1.17-.45-1.15-1.1-1.46-1.1-1.46-.9-.62.07-.61.07-.61 1 .07 1.53 1.04 1.53 1.04.88 1.51 2.3 1.07 2.86.82.09-.64.35-1.07.63-1.31-2.2-.25-4.52-1.1-4.52-4.88 0-1.08.38-1.96 1.02-2.65-.1-.25-.44-1.25.1-2.6 0 0 .84-.27 2.75 1.01a9.59 9.59 0 0 1 5 0c1.9-1.28 2.74-1.01 2.74-1.01.55 1.35.2 2.35.1 2.6.64.69 1.02 1.57 1.02 2.65 0 3.79-2.33 4.63-4.55 4.88.36.31.68.92.68 1.86v2.76c0 .28.18.58.68.47A9.75 9.75 0 0 0 12 2.25Z"/>
                    </svg>
                <#elseif providerKey?contains("saml") || providerKey?contains("okta") || providerKey?contains("azure") || providerKey?contains("microsoft")>
                    <svg viewBox="0 0 24 24" aria-hidden="true" class="h-4 w-4 fill-current">
                        <path d="M11.1 2.5 3 6.4v11.2l8.1 3.9 9-4.3V5.9l-9-3.4Zm-.1 2.1 6.6 2.5-6.6 3.2-6.1-2.7 6.1-3Zm-6 5.4 5.4 2.4v6.7l-5.4-2.6V10Zm7.4 9.1v-6.7l6.6-3.2v8.3l-6.6 1.6Z"/>
                    </svg>
                <#else>
                    <span style="${iconLabelStyle}">${fallbackLabel}</span>
                </#if>
            </span>
            <span class="min-w-0 uppercase tracking-wide">Continue with ${connection.name}</span>
        </button>
    </#if>
</#macro>

<#macro fieldError errors fieldName>
    <#if errors??>
        <#list errors.fieldErrors as err>
            <#if err.field == fieldName>
                <div class="text-red-500 text-sm mt-1">
                    ${err.defaultMessage}
                </div>
            </#if>
        </#list>
    </#if>
</#macro>