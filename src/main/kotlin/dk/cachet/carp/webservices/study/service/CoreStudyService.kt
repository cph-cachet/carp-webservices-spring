package dk.cachet.carp.webservices.study.service

import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHost
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
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
        coreEventBus: CoreEventBus
)
{
    final val instance: StudyService = StudyServiceHost(
            studyRepository,
            coreEventBus.createApplicationServiceAdapter(StudyService::class)
    )

}