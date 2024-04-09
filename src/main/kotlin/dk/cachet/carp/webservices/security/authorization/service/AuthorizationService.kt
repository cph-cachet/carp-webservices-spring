package dk.cachet.carp.webservices.security.authorization.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role

interface AuthorizationService {
    fun require( claim: Claim )

    fun require( claims: Set<Claim> )

    fun require( role: Role )

    fun requireOwner( ownerId: UUID )

    suspend fun grantCurrentAuthentication( claim: Claim )

    suspend fun grantCurrentAuthentication( claims: Set<Claim> )
}