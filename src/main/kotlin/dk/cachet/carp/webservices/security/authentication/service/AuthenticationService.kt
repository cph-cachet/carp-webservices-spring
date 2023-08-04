package dk.cachet.carp.webservices.security.authentication.service

import dk.cachet.carp.webservices.security.authentication.domain.Account

interface AuthenticationService {
    fun getCurrentPrincipal(): Account
}