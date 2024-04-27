package dk.cachet.carp.webservices.security.authorization.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role

interface AuthorizationService
{
    /**
     * Require the current authentication to have the specified [claim].
     *
     * @throws ForbiddenException when the current authentication does not have the specified [claim].
     */
    fun require( claim: Claim )

    /**
     * Require the current authentication to have all specified [claims].
     *
     * @throws ForbiddenException when the current authentication does not have the specified [claim].
     */
    fun require( claims: Set<Claim> )

    /**
     * Require the current authentication to have the specified [role].
     *
     * @throws ForbiddenException when the current authentication does not have the specified [claim].
     */
    fun require( role: Role )

    /**
     * Require the current authentication to be the owner of the entity identified by [ownerId].
     *
     * @throws ForbiddenException when the current authentication does not have the specified [claim].
     */
    fun requireOwner( ownerId: UUID )

    /**
     * Grant the current authentication the specified [claim].
     */
    suspend fun grantCurrentAuthentication( claim: Claim )

    /**
     * Grant the current authentication all specified [claims].
     */
    suspend fun grantCurrentAuthentication( claims: Set<Claim> )
}