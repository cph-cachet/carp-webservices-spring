package dk.cachet.carp.webservices.deployment.service

import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHost
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import dk.cachet.carp.webservices.deployment.repository.CoreParticipationRepository
import org.springframework.stereotype.Component

/**
 * The Class [CoreParticipationService].
 * Initiates the [ParticipationServiceHost] as a singleton.
 * This instance should be used wherever the core service is needed
 * to handle the event subscriptions properly.
 */
@Component
class CoreParticipationService
(
    participationRepository: CoreParticipationRepository,
    accountService: AccountService,
    coreEventBus: CoreEventBus
)
{
    private val participantGroupService = ParticipantGroupService(accountService)

    final val instance: ParticipationService = ParticipationServiceHost(
            participationRepository,
            participantGroupService,
            coreEventBus.createApplicationServiceAdapter(ParticipationService::class)
    )
}