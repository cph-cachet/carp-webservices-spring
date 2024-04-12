package dk.cachet.carp.webservices.data.controller

import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.authorization.createAuthorizedCommand
import dk.cachet.carp.webservices.data.authorization.DataStreamServiceAuthorizer
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class DataStreamController(
    dataStreamServiceAuthorizer: DataStreamServiceAuthorizer,
    dataStreamService: DataStreamService
)
{
    companion object
    {
        /** Endpoint URI constants */
        const val DATA_STREAM_SERVICE = "/api/data-stream-service"
    }

    private final val service = DataStreamServiceDecorator( dataStreamService )
    {
       command -> createAuthorizedCommand( dataStreamServiceAuthorizer, command::invoke )
    }

    @PostMapping(value = [DATA_STREAM_SERVICE])
    @Operation(tags = ["dataStream/getDataStream.json"])
    suspend fun invoke( @RequestBody request: DataStreamServiceRequest<*> ) : ResponseEntity<Any> =
        service.invoke( request ).let { ResponseEntity.ok( it ) }
}