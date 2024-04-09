package dk.cachet.carp.webservices.security.authentication

import dk.cachet.carp.webservices.security.authentication.domain.Account
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

fun getAuthenticationOrThrow(): Account {
   val authentication = SecurityContextHolder.getContext().authentication
   require(authentication is JwtAuthenticationToken)

   return Account.fromJwt(authentication)
}
