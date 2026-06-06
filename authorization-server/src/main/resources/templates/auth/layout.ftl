<#macro body title="">
<!doctype html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <meta name="description" content="Modern Identity & Access Management">
    <link rel="stylesheet" href="/css/style.css">
    <title>${title} | Authora</title>
</head>
<body>

    <main class="w-full h-screen flex justify-center items-center">
        <#nested >
    </main>
</body>
</html>
</#macro>