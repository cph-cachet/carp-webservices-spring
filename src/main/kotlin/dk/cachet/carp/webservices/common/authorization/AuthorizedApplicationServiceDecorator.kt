package dk.cachet.carp.webservices.common.authorization

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceDecorator
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceInvoker
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.common.infrastructure.services.Command

class AuthorizedApplicationServiceDecorator<
        TService: ApplicationService<TService, *>,
        TRequest: ApplicationServiceRequest<TService, *>>
(
    authorizer: ApplicationServiceAuthorizer<TService, TRequest>,
    service: TService,
    requestInvoker: ApplicationServiceInvoker<TService, TRequest>,
) : ApplicationServiceDecorator<TService, TRequest>(
    service,
    requestInvoker,
    {
        command: Command<TRequest> -> createAuthorizedCommand( authorizer, command::invoke )
    }
)

private fun <TService: ApplicationService<TService, *>, TRequest: ApplicationServiceRequest<TService, *>>
        createAuthorizedCommand(
            authorizer: ApplicationServiceAuthorizer<TService, TRequest>,
            invocation: suspend (TRequest) -> Any?
        ): Command<TRequest>
{
    return object : Command<TRequest>
    {
        override suspend fun invoke(request: TRequest): Any?
        {
            authorizer.authorizeRequest( request )

            val result = invocation( request )

            requireNotNull( result )

            authorizer.grantClaimsOnSuccessfulRequest( request, result )

            return result
        }
    }
}
