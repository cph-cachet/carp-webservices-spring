package dk.cachet.carp.webservices.deployment.service

import dk.cachet.carp.deployments.infrastructure.ParticipationServiceDecorator

interface ParticipationService {
    val core: ParticipationServiceDecorator
}