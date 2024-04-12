package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.webservices.data.authorization.DataStreamServiceAuthorizer

object DataStreamDecorators {
    val authorizationDecorator: (DataStreamService) -> DataStreamService = {
        DataStreamServiceDecorator( it ) {
            DataStreamServiceAuthorizer.getRequiredClaims( it )
        }
    }
}