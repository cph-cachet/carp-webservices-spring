package dk.cachet.carp.webservices.data.authorization

import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceAuthorizer
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import org.springframework.stereotype.Service

@Service
class DataStreamServiceAuthorizer(
    private val auth: AuthorizationService,
) : ApplicationServiceAuthorizer<DataStreamService, DataStreamServiceRequest<*>> {
    override fun DataStreamServiceRequest<*>.authorize() =
        when (this) {
            is DataStreamServiceRequest.OpenDataStreams ->
                auth.require(Claim.InDeployment(configuration.studyDeploymentId))
            is DataStreamServiceRequest.AppendToDataStreams ->
                auth.require(Claim.InDeployment(studyDeploymentId))
            is DataStreamServiceRequest.GetDataStream ->
                auth.require(Claim.InDeployment(dataStream.studyDeploymentId))
            is DataStreamServiceRequest.CloseDataStreams ->
                auth.require(studyDeploymentIds.map { Claim.ManageDeployment(it) }.toSet())
            is DataStreamServiceRequest.RemoveDataStreams ->
                auth.require(studyDeploymentIds.map { Claim.ManageDeployment(it) }.toSet())
        }

    override suspend fun DataStreamServiceRequest<*>.changeClaimsOnSuccess(result: Any?) =
        when (this) {
            is DataStreamServiceRequest.OpenDataStreams,
            is DataStreamServiceRequest.AppendToDataStreams,
            is DataStreamServiceRequest.GetDataStream,
            is DataStreamServiceRequest.CloseDataStreams,
            is DataStreamServiceRequest.RemoveDataStreams,
            -> Unit
        }
}
