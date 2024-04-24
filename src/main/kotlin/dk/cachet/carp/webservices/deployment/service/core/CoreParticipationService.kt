package dk.cachet.carp.webservices.deployment.service.core

import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHost
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceDecorator
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceRequestAuthorizer
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import dk.cachet.carp.webservices.deployment.authorization.ParticipationServiceAuthorizer
import dk.cachet.carp.webservices.deployment.repository.CoreParticipationRepository
import org.springframework.stereotype.Component

/**
 * Initiates [ParticipationServiceHost] as a singleton.
 *
 * This instance should be used wherever the core service is needed
 * to handle the event subscriptions properly.
 */
@Component
class CoreParticipationService(
    participationRepository: CoreParticipationRepository,
    accountService: AccountService,
    coreEventBus: CoreEventBus,
    participationServiceAuthorizer: ParticipationServiceAuthorizer
)
{
    final val instance: ParticipationServiceDecorator

    init
    {
        val service = ParticipationServiceHost(
            participationRepository,
            ParticipantGroupService(accountService),
            coreEventBus.createApplicationServiceAdapter(ParticipationService::class)
        )

        val authorizedService = ParticipationServiceDecorator( service )
        {
            command -> ApplicationServiceRequestAuthorizer( participationServiceAuthorizer, command )
        }

        instance = authorizedService
    }
}