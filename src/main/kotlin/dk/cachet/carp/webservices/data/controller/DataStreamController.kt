package dk.cachet.carp.webservices.data.controller

import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequestLogger
import dk.cachet.carp.common.infrastructure.services.EventBusLog
import dk.cachet.carp.common.infrastructure.services.LoggedRequest
import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceRequestAuthorizer
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import dk.cachet.carp.webservices.data.authorization.DataStreamServiceAuthorizer
import io.swagger.v3.oas.annotations.Operation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class DataStreamController(
    dataStreamServiceAuthorizer: DataStreamServiceAuthorizer,
    dataStreamService: DataStreamService,
)
{
    final val service: DataStreamServiceDecorator

    init
    {
        val authorizedService = DataStreamServiceDecorator( dataStreamService )
        {
            command -> ApplicationServiceRequestAuthorizer( dataStreamServiceAuthorizer, command )
        }

        service = authorizedService
    }

    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val DATA_STREAM_SERVICE = "/api/data-stream-service"
    }

    @PostMapping(value = [DATA_STREAM_SERVICE])
    @Operation(tags = ["dataStream/getDataStream.json"])
    suspend fun invoke( @RequestBody request: DataStreamServiceRequest<*> ) : ResponseEntity<Any>
    {
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> ${ request::class.simpleName }")
        return service.invoke( request ).let { ResponseEntity.ok( it ) }
    }
}