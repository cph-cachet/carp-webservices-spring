package dk.cachet.carp.webservices.common.authorization

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.common.infrastructure.services.Command

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

class ApplicationServiceRequestAuthorizer<
        TService: ApplicationService<TService, *>,
        TRequest: ApplicationServiceRequest<TService, *>
>(
    private val serviceAuthorizer: ApplicationServiceAuthorizer<TService, TRequest>,
    private val decoratee: Command<TRequest>
): Command<TRequest>
{
    override suspend fun invoke( request: TRequest ): Any?
    {
        serviceAuthorizer.authorizeRequest( request )

        val result = decoratee.invoke( request )

        serviceAuthorizer.grantClaimsOnSuccessfulRequest( request, result )

        return result
    }
}