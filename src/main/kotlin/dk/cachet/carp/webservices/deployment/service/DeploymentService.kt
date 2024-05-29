package dk.cachet.carp.webservices.deployment.service

import dk.cachet.carp.deployments.infrastructure.DeploymentServiceDecorator

interface DeploymentService {
    val core: DeploymentServiceDecorator
}
