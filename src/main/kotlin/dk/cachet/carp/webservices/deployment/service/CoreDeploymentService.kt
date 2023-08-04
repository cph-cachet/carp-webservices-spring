package dk.cachet.carp.webservices.deployment.service

import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import dk.cachet.carp.webservices.data.service.CoreDataStreamService
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import org.springframework.stereotype.Component

/**
 * The Class [CoreDeploymentService].
 * Initiates the [DeploymentServiceHost] as a singleton.
 * This instance should be used wherever the core service is needed
 * to handle the event subscriptions properly.
 */
@Component
class CoreDeploymentService
(
        deploymentRepository: CoreDeploymentRepository,
        coreEventBus: CoreEventBus,
        coreDataStreamService: CoreDataStreamService
)
{
    final val instance: DeploymentService = DeploymentServiceHost(
            deploymentRepository,
            coreDataStreamService,
            coreEventBus.createApplicationServiceAdapter(DeploymentService::class)
    )
}