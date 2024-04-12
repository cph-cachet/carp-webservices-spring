package dk.cachet.carp.webservices.data.authorization

import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceAuthorizer
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import org.springframework.stereotype.Service

@Service
class DataStreamServiceAuthorizer(
   private val auth: AuthorizationService
): ApplicationServiceAuthorizer<DataStreamService, DataStreamServiceRequest<*>>
{
    override fun DataStreamServiceRequest<*>.authorize() =
        when ( this ) {
            is DataStreamServiceRequest.OpenDataStreams ->
                auth.require( setOf( Claim.InDeployment( configuration.studyDeploymentId ) ) )
            is DataStreamServiceRequest.AppendToDataStreams ->
                auth.require( setOf( Claim.InDeployment( studyDeploymentId ) ) )
            is DataStreamServiceRequest.GetDataStream ->
                auth.require( setOf( Claim.InDeployment( dataStream.studyDeploymentId ) ) )
            is DataStreamServiceRequest.CloseDataStreams ->
                auth.require( studyDeploymentIds.map { Claim.ManageDeployment( it ) }.toSet() )
            is DataStreamServiceRequest.RemoveDataStreams ->
                auth.require( studyDeploymentIds.map { Claim.ManageDeployment( it ) }.toSet() )
        }

    override suspend fun <TResult> DataStreamServiceRequest<*>.grantClaimsOnSuccess( result: TResult ) =
        when ( this ) {
            is DataStreamServiceRequest.OpenDataStreams,
            is DataStreamServiceRequest.AppendToDataStreams,
            is DataStreamServiceRequest.GetDataStream,
            is DataStreamServiceRequest.CloseDataStreams,
            is DataStreamServiceRequest.RemoveDataStreams -> Unit
        }
}