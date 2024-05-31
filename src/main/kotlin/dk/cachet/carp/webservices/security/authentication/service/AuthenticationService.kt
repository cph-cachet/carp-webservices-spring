package dk.cachet.carp.webservices.security.authentication.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role

interface AuthenticationService {
    fun getId(): UUID

    fun getRole(): Role

    fun getClaims(): Collection<Claim>

    fun getCarpIdentity(): AccountIdentity
}
