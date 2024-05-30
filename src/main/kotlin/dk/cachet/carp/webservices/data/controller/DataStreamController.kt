package dk.cachet.carp.webservices.data.controller

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.data.service.DataStreamService
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class DataStreamController(
    private val dataStreamService: DataStreamService,
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val DATA_STREAM_SERVICE = "/api/data-stream-service"
        const val DATA_STREAM_SERVICE_OPEN = "/api/data-stream-service-zip"
    }

    @PostMapping(value = [DATA_STREAM_SERVICE])
    @Operation(tags = ["dataStream/getDataStream.json"])
    suspend fun invoke(
        @RequestBody request: DataStreamServiceRequest<*>,
    ): ResponseEntity<Any> {
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> ${ request::class.simpleName }")
        return dataStreamService.core.invoke(request).let { ResponseEntity.ok(it) }
    }

    // New endpoint specifically for AppendToDataStreams with zip file
    @PostMapping(value = ["/dataStreamService/open"], consumes = ["multipart/form-data"])
    @Operation(tags = ["dataStream/getDataStream.json"])
    fun appendToDataStreamWithZip(
        @RequestParam("studyDeploymentId") studyDeploymentId: UUID,
        @RequestParam("zipFile") zipFile: MultipartFile
    ): ResponseEntity<Any> = runBlocking {
        LOGGER.info("Start POST: /dataStreamService/open -> AppendToDataStreams with zip file")
        return@runBlocking try {
            // Process the MultipartFile and create a sequence of DataStreamSequence
            val batch = dataStreamService.extractFilesFromZip(studyDeploymentId, zipFile.bytes)
            // Create a request object for the existing service method
            val request = DataStreamServiceRequest.AppendToDataStreams(studyDeploymentId, batch)
            // Invoke the existing service method
            val result = dataStreamService.core.invoke(request)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            LOGGER.error("Error in POST: /dataStreamService/open -> AppendToDataStreams with zip file", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
        }
    }
}
