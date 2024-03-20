package dk.cachet.carp.webservices.protocol.service

import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceHost
import dk.cachet.carp.webservices.protocol.repository.CoreProtocolRepository
import org.springframework.stereotype.Component

/**
 * The Class [CoreProtocolService].
 * Access only the [CoreProtocolRepository].
 * This instance should be used wherever the core service is NOT needed.
 */
@Component
class CoreProtocolService
(
    coreProtocolRepository: CoreProtocolRepository,
)
{
    final val instance: ProtocolService = ProtocolServiceHost(
        coreProtocolRepository)
}