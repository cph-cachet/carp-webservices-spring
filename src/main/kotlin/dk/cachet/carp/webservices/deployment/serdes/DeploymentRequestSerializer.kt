package dk.cachet.carp.webservices.deployment.serdes

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.webservices.common.serialisers.ResponseSerializer
import kotlinx.serialization.serializer

class DeploymentRequestSerializer : ResponseSerializer<DeploymentServiceRequest<*>>() {
    @Suppress("UNCHECKED_CAST")
    override fun <TService : ApplicationService<TService, *>> serializeResponse(
        request: ApplicationServiceRequest<TService, *>,
        content: Any?,
    ): Any? {
        return when (request) {
            is DeploymentServiceRequest.CreateStudyDeployment,
            is DeploymentServiceRequest.DeviceDeployed,
            is DeploymentServiceRequest.RegisterDevice,
            is DeploymentServiceRequest.GetStudyDeploymentStatus,
            is DeploymentServiceRequest.Stop,
            is DeploymentServiceRequest.UnregisterDevice,
            ->
                json.encodeToString(serializer<StudyDeploymentStatus>(), content as StudyDeploymentStatus)
            is DeploymentServiceRequest.RemoveStudyDeployments ->
                json.encodeToString(serializer<Set<UUID>>(), content as Set<UUID>)
            is DeploymentServiceRequest.GetDeviceDeploymentFor ->
                json.encodeToString(serializer<PrimaryDeviceDeployment>(), content as PrimaryDeviceDeployment)
            is DeploymentServiceRequest.GetStudyDeploymentStatusList ->
                json.encodeToString(
                    serializer<List<StudyDeploymentStatus>>(),
                    content as List<StudyDeploymentStatus>,
                )
            else -> content
        }
    }
}
