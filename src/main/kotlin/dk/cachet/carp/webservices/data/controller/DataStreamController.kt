package dk.cachet.carp.webservices.data.controller

import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.data.service.DataStreamService
import io.swagger.v3.oas.annotations.Operation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class DataStreamController(
    private val dataStreamService: DataStreamService,
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val DATA_STREAM_SERVICE = "/api/data-stream-service"
    }

    @PostMapping(value = [DATA_STREAM_SERVICE])
    @Operation(tags = ["dataStream/getDataStream.json"])
    suspend fun invoke(
        @RequestBody request: DataStreamServiceRequest<*>,
    ): ResponseEntity<Any> {
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> ${ request::class.simpleName }")
        return dataStreamService.core.invoke(request).let { ResponseEntity.ok(it) }
    }
}
