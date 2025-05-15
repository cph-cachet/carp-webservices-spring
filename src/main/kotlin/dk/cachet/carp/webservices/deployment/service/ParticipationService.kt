package dk.cachet.carp.webservices.deployment.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.domain.users.ParticipantGroup
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceDecorator

interface ParticipationService {
    val core: ParticipationServiceDecorator

    suspend fun getParticipantGroup(studyDeploymentId: UUID): ParticipantGroup?
}
