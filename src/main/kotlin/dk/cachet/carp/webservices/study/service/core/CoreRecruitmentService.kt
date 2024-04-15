package dk.cachet.carp.webservices.study.service.core

import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHost
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceDecorator
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceRequestAuthorizer
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import dk.cachet.carp.webservices.deployment.service.core.CoreDeploymentService
import dk.cachet.carp.webservices.study.authorization.RecruitmentServiceAuthorizer
import dk.cachet.carp.webservices.study.repository.CoreParticipantRepository
import org.springframework.stereotype.Component

@Component
class CoreRecruitmentService(
    participantRepository: CoreParticipantRepository,
    recruitmentServiceAuthorizer: RecruitmentServiceAuthorizer,
    coreEventBus: CoreEventBus,
    coreDeploymentService: CoreDeploymentService,
) {
    final val instance: RecruitmentServiceDecorator

    init
    {
        val service = RecruitmentServiceHost(
            participantRepository,
            coreDeploymentService.instance,
            coreEventBus.createApplicationServiceAdapter( RecruitmentService::class )
        )

        val authorizedService = RecruitmentServiceDecorator( service )
        {
            command -> ApplicationServiceRequestAuthorizer( recruitmentServiceAuthorizer, command )
        }

        instance = authorizedService
    }
}