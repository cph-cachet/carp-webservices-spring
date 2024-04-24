package dk.cachet.carp.webservices.study.service.core

import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHost
import dk.cachet.carp.studies.infrastructure.StudyServiceDecorator
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceRequestAuthorizer
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import dk.cachet.carp.webservices.study.authorization.StudyServiceAuthorizer
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import org.springframework.stereotype.Component

/**
 * The Class [CoreStudyService].
 * Initiates the [StudyServiceHost] as a singleton.
 * This instance should be used wherever the core service is needed
 * to handle the event subscriptions properly.
 */
@Component
class CoreStudyService
(
    studyRepository: CoreStudyRepository,
    coreEventBus: CoreEventBus,
    studyServiceAuthorizer: StudyServiceAuthorizer
)
{
    final val instance: StudyServiceDecorator

    init
    {
        val service = StudyServiceHost(
            studyRepository,
            coreEventBus.createApplicationServiceAdapter(StudyService::class)
        )

        val authorizedService = StudyServiceDecorator( service )
        {
            command -> ApplicationServiceRequestAuthorizer( studyServiceAuthorizer, command )
        }

        instance = authorizedService
    }
}