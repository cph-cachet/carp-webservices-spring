package dk.cachet.carp.webservices.deployment.service

import dk.cachet.carp.deployments.infrastructure.ParticipationServiceDecorator
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceRequest

interface ParticipationService {
    val core: ParticipationServiceDecorator

    suspend fun participationDataRequest(request: ParticipationServiceRequest<*>)
}
