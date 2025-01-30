package dk.cachet.carp.webservices.datastream.controller

import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.input.WS_JSON
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.datastream.service.impl.decompressGzip
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
        private val serializer = DataStreamRequestSerializer()

        /** Endpoint URI constants */
        const val DATA_STREAM_SERVICE = "/api/data-stream-service"
        const val DATA_STREAM_SERVICE_GZIP = "/api/data-stream-service-zip"
    }

    @PostMapping(value = [DATA_STREAM_SERVICE])
    @Operation(tags = ["dataStream/invoke.json"])
    suspend fun invoke(
        @RequestBody httpMessage: String,
    ): ResponseEntity<Any> {
        val request = WS_JSON.decodeFromString(DataStreamServiceRequest.Serializer, httpMessage)
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> ${request::class.simpleName}")
        val ret = dataStreamService.core.invoke(request)
        return serializer.serializeResponse(request, ret).let { ResponseEntity.ok(it) }
    }

    @Operation(tags = ["dataStream/handleCompressedData.json"])
    @PostMapping(value = [DATA_STREAM_SERVICE_GZIP])
    suspend fun handleCompressedData(
        @RequestBody data: ByteArray,
    ): ResponseEntity<Any> {
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE_GZIP")
        val decompressedData = decompressGzip(data)
        val request = WS_JSON.decodeFromString(DataStreamServiceRequest.Serializer, decompressedData)
        return dataStreamService.core.invoke(request).let { ResponseEntity.ok(it) }
    }
}
