package dk.cachet.carp.webservices.data.controller

import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.webservices.data.domain.DataStreamServiceRequestDTO
import dk.cachet.carp.webservices.data.service.DataStreamService
import io.swagger.v3.oas.annotations.Operation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
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
    @PreAuthorize("canManageStudy((#request.studyDeploymentId))")
    @Operation(tags = ["dataStream/getDataStream.json"])
    suspend fun invoke(
        @RequestBody request: DataStreamServiceRequestDTO,
    ): ResponseEntity<Any> {
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> ${ request::class.simpleName }")
        val serviceRequest = request.toDataStreamServiceRequest()

        @Suppress("UNCHECKED_CAST")
        val applicationServiceRequest =
            serviceRequest as ApplicationServiceRequest<dk.cachet.carp.data.application.DataStreamService, *>?
                ?: throw IllegalArgumentException("Unsupported request type: ${ request.apiVersion }")
        return dataStreamService.core.invoke(applicationServiceRequest).let { ResponseEntity.ok(it) }
    }
}
