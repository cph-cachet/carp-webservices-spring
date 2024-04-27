package dk.cachet.carp.webservices.deployment.authorization

import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceAuthorizer
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import org.springframework.stereotype.Service

@Service
class DeploymentServiceAuthorizer(
    private val auth: AuthorizationService
) : ApplicationServiceAuthorizer<DeploymentService, DeploymentServiceRequest<*>>
{
    override fun DeploymentServiceRequest<*>.authorize() =
        when ( this )
        {
            // Participants should be able to deploy themselves if needed,
            // we shouldn't restrict study deployment creation to researchers only
            is DeploymentServiceRequest.CreateStudyDeployment -> Unit
            is DeploymentServiceRequest.RemoveStudyDeployments ->
                auth.require( studyDeploymentIds.map { Claim.ManageDeployment( it ) }.toSet() )
            is DeploymentServiceRequest.GetStudyDeploymentStatus ->
                auth.require( Claim.InDeployment( studyDeploymentId ) )
            is DeploymentServiceRequest.GetStudyDeploymentStatusList ->
                auth.require( studyDeploymentIds.map { Claim.InDeployment( it ) }.toSet() )
            is DeploymentServiceRequest.RegisterDevice ->
                auth.require( Claim.InDeployment( studyDeploymentId ) )
            is DeploymentServiceRequest.UnregisterDevice ->
                auth.require( Claim.InDeployment( studyDeploymentId ) )
            is DeploymentServiceRequest.GetDeviceDeploymentFor ->
                auth.require( Claim.InDeployment( studyDeploymentId ) )
            is DeploymentServiceRequest.DeviceDeployed ->
                auth.require( Claim.InDeployment( studyDeploymentId ) )
            is DeploymentServiceRequest.Stop ->
                auth.require( Claim.InDeployment( studyDeploymentId ) )
        }

    override suspend fun DeploymentServiceRequest<*>.changeClaimsOnSuccess(result: Any? ) =
        when ( this )
        {
            is DeploymentServiceRequest.CreateStudyDeployment -> {
                require( result is StudyDeploymentStatus )

                auth.grantCurrentAuthentication(
                    setOf(
                        Claim.ManageDeployment( result.studyDeploymentId ),
                        Claim.InDeployment( result.studyDeploymentId )
                    )
                )
            }
            is DeploymentServiceRequest.RemoveStudyDeployments -> {
                studyDeploymentIds.forEach {
                    auth.revokeClaimsFromAllAccounts(
                        setOf(
                            Claim.InDeployment( it ),
                            Claim.ManageDeployment( it )
                        )
                    )
                }
            }
            is DeploymentServiceRequest.GetStudyDeploymentStatus,
            is DeploymentServiceRequest.GetStudyDeploymentStatusList,
            is DeploymentServiceRequest.RegisterDevice,
            is DeploymentServiceRequest.UnregisterDevice,
            is DeploymentServiceRequest.GetDeviceDeploymentFor,
            is DeploymentServiceRequest.DeviceDeployed,
            is DeploymentServiceRequest.Stop -> Unit
        }
}