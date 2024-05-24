<#macro emailLayout>
<html>
    <body>
        <h2>${kcSanitize(msg("welcomeTitle"))?no_esc}</h2>
        <#nested>
        <a href="https://carp.cachet.dk/">
            <span>Copenhagen Research Platform (CARP)</span>
        </a>
        <br>
        <a href="https://www.dtu.dk/">
            <span>${kcSanitize(msg("dtu"))?no_esc}</span>
        </a>
        <br>
        <a href="https://carp.cachet.dk/privacy-policy/">
            <span>Privacy policy</span>
        </a>
    </body>
</html>
</#macro>