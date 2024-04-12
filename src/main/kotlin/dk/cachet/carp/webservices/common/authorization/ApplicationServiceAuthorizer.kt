package dk.cachet.carp.webservices.common.authorization

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest

interface ApplicationServiceAuthorizer<
        TService: ApplicationService<TService, *>,
        TRequest: ApplicationServiceRequest<TService, *>
        >
{
    fun TRequest.authorize()
    suspend fun TRequest.grantClaimsOnSuccess( result: Any? )
    fun authorizeRequest( request: TRequest ) = request.authorize()
    suspend fun grantClaimsOnSuccessfulRequest( request: TRequest, result: Any? ) =
        request.grantClaimsOnSuccess( result )
}

