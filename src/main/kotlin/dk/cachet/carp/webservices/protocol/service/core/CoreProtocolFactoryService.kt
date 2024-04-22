package dk.cachet.carp.webservices.protocol.service.core

import dk.cachet.carp.protocols.application.ProtocolFactoryServiceHost
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceDecorator
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceRequestAuthorizer
import dk.cachet.carp.webservices.protocol.authorization.ProtocolFactoryServiceAuthorizer
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service


@Component
class CoreProtocolFactoryService(
    protocolFactoryServiceAuthorizer: ProtocolFactoryServiceAuthorizer
)
{
    final val instance: ProtocolFactoryServiceDecorator

    init
    {
        val service = ProtocolFactoryServiceHost( )

        val authorizedService = ProtocolFactoryServiceDecorator( service )
        {
            command -> ApplicationServiceRequestAuthorizer( protocolFactoryServiceAuthorizer, command )
        }

        instance = authorizedService
    }
}
