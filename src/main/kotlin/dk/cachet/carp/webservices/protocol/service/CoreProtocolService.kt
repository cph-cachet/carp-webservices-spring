package dk.cachet.carp.webservices.protocol.service

import dk.cachet.carp.protocols.application.ProtocolServiceHost
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceDecorator
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceRequestAuthorizer
import dk.cachet.carp.webservices.protocol.authorization.ProtocolServiceAuthorizer
import dk.cachet.carp.webservices.protocol.repository.CoreProtocolRepository
import org.springframework.stereotype.Service

@Service
class CoreProtocolService(
    protocolRepository: CoreProtocolRepository,
    protocolServiceAuthorizer: ProtocolServiceAuthorizer
)
{
    final val instance: ProtocolServiceDecorator

    init
    {
        val service = ProtocolServiceHost( protocolRepository )

        val authorizedService = ProtocolServiceDecorator( service )
        {
            command -> ApplicationServiceRequestAuthorizer( protocolServiceAuthorizer, command )
        }

        instance = authorizedService
    }
}