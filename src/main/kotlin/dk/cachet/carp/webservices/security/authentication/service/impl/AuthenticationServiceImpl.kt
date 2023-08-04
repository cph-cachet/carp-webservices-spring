package dk.cachet.carp.webservices.security.authentication.service.impl

import dk.cachet.carp.webservices.common.exception.responses.UnauthorizedException
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.domain.AccountFactory
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthenticationServiceImpl(
    private val accountFactory: AccountFactory
) : AuthenticationService {
    override fun getCurrentPrincipal(): Account {
        val jwt = SecurityContextHolder.getContext().authentication as JwtAuthenticationToken
        if (jwt.token == null) {
            throw UnauthorizedException("No JWT token found in authentication.")
        }
        return accountFactory.fromJwtAuthenticationToken(jwt)
    }
}