package dk.cachet.carp.webservices.protocol.service.impl

import dk.cachet.carp.webservices.protocol.service.ProtocolFactoryService
import dk.cachet.carp.webservices.protocol.service.core.CoreProtocolFactoryService
import org.springframework.stereotype.Service

@Service
class ProtocolFactoryServiceImpl(
    coreProtocolFactoryService: CoreProtocolFactoryService
) : ProtocolFactoryService
{
    final override val core = coreProtocolFactoryService.instance
}