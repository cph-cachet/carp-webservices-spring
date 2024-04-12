package dk.cachet.carp.webservices.security.authentication.service.impl

import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthenticationServiceImpl : AuthenticationService
{
    override fun getAuthentication() : Account
    {
        val authentication = SecurityContextHolder.getContext().authentication

        require( authentication is JwtAuthenticationToken )

        return Account.fromJwt( authentication )
    }
}