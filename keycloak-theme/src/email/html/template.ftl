<#macro emailLayout>
<html>
    <body>
        <h3 style="text-align: center;"><strong>${kcSanitize(msg("welcomeTitle"))?no_esc}</strong></h3>
        <#nested>
        <a href="https://carp.dk/">
            <span>Copenhagen Research Platform (CARP)</span>
        </a>
        <br>
        <a href="https://www.dtu.dk/">
            <span>${kcSanitize(msg("dtu"))?no_esc}</span>
        </a>
        <br>
        <a href="https://carp.dk/privacy-policy/">
            <span>Privacy policy</span>
        </a>
    </body>
</html>
</#macro>