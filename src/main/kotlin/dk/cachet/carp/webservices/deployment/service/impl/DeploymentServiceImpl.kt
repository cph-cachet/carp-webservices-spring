package dk.cachet.carp.webservices.deployment.service.impl

import dk.cachet.carp.webservices.deployment.service.DeploymentService
import dk.cachet.carp.webservices.deployment.service.core.CoreDeploymentService
import org.springframework.stereotype.Service

@Service
class DeploymentServiceImpl(
    coreDeploymentService: CoreDeploymentService
) : DeploymentService
{
    final override val core = coreDeploymentService.instance
}