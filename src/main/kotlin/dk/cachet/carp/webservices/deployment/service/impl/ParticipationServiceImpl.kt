package dk.cachet.carp.webservices.deployment.service.impl

import dk.cachet.carp.webservices.deployment.service.ParticipationService
import dk.cachet.carp.webservices.deployment.service.core.CoreParticipationService
import org.springframework.stereotype.Service

@Service
class ParticipationServiceImpl(
    coreParticipationService: CoreParticipationService
) : ParticipationService
{
    final override val core = coreParticipationService.instance
}