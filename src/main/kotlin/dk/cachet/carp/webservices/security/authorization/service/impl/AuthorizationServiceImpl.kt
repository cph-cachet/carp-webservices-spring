package dk.cachet.carp.webservices.security.authorization.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.exception.responses.ForbiddenException
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import org.springframework.stereotype.Service
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Service
class AuthorizationServiceImpl(
    private val authenticationService: AuthenticationService,
    private val accountService: AccountService,
) : AuthorizationService
{
    companion object
    {
        // TODO: move to resources
        private const val PERMISSION_DENIED_MSG = "Permission denied"
    }

    override fun require( claims: Set<Claim> ) = require( *claims.toTypedArray() ) { PERMISSION_DENIED_MSG  }

    override fun require( claim: Claim ) = require( claim ) { PERMISSION_DENIED_MSG }

    private inline fun require( vararg claims: Claim, crossinline lazyMessage: () -> Any = {} )
    {
        val account = authenticationService.getAuthentication()
        require( claims.all { account.carpClaims?.contains( it ) == true }, lazyMessage )
    }

    override fun require( role: Role ) = require( role ) { PERMISSION_DENIED_MSG }

    private inline fun require( role: Role, crossinline lazyMessage: () -> Any = {} )
    {
        val account = authenticationService.getAuthentication()
        require( account.role?.let { role >= it } ?: false , lazyMessage )
    }

    override fun requireOwner( ownerId: UUID ) = requireOwner( ownerId ) { PERMISSION_DENIED_MSG }

    private inline fun requireOwner( ownerId: UUID, crossinline lazyMessage: () -> Any = {} )
    {
        val account = authenticationService.getAuthentication()
        require( account.id == ownerId.toString(), lazyMessage )
    }

    override suspend fun grantCurrentAuthentication( claim: Claim ) = grantCurrentAuthentication( setOf( claim ) )

    override suspend fun grantCurrentAuthentication( claims: Set<Claim> )
    {
        val account = authenticationService.getAuthentication()
        accountService.grant( account.getIdentity(), claims )
    }

    @OptIn( ExperimentalContracts::class )
    private inline fun require( value: Boolean, lazyMessage: () -> Any )
    {
        contract {
            returns() implies value
        }

        if ( !value ) {
            val message = lazyMessage()
            throw ForbiddenException( message.toString() )
        }
    }
}