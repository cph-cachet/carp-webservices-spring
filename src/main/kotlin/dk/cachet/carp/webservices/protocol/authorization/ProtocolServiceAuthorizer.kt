package dk.cachet.carp.webservices.protocol.authorization

import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceAuthorizer
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import org.springframework.stereotype.Service

@Service
class ProtocolServiceAuthorizer(
    private val auth: AuthorizationService
) : ApplicationServiceAuthorizer<ProtocolService, ProtocolServiceRequest<*>>
{
    override fun ProtocolServiceRequest<*>.authorize() =
        when ( this )
        {
            is ProtocolServiceRequest.Add -> {
                auth.require( Role.RESEARCHER )
                auth.requireOwner( protocol.ownerId )
            }
            is ProtocolServiceRequest.AddVersion -> auth.requireOwner( protocol.ownerId )
            is ProtocolServiceRequest.UpdateParticipantDataConfiguration ->
                auth.require( Claim.ProtocolOwner( protocolId ))
            is ProtocolServiceRequest.GetBy -> auth.require( Claim.ProtocolOwner( protocolId ) )
            is ProtocolServiceRequest.GetAllForOwner -> auth.requireOwner( ownerId )
            is ProtocolServiceRequest.GetVersionHistoryFor -> auth.require( Claim.ProtocolOwner( protocolId ) )
        }

    override suspend fun ProtocolServiceRequest<*>.grantClaimsOnSuccess( result: Any? ) =
        when ( this )
        {
            is ProtocolServiceRequest.Add ->
                auth.grantCurrentAuthentication( Claim.ProtocolOwner( protocol.ownerId ) )
            is ProtocolServiceRequest.AddVersion,
            is ProtocolServiceRequest.UpdateParticipantDataConfiguration,
            is ProtocolServiceRequest.GetBy,
            is ProtocolServiceRequest.GetAllForOwner,
            is ProtocolServiceRequest.GetVersionHistoryFor -> Unit
        }
}