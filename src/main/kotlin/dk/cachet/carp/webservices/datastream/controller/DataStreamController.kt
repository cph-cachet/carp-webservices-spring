package dk.cachet.carp.webservices.datastream.controller

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.input.WS_JSON
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.datastream.service.impl.decompressGzip
import io.swagger.v3.oas.annotations.Operation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class DataStreamController(
    private val dataStreamService: DataStreamService,
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val DATA_STREAM_SERVICE = "/api/data-stream-service"
        const val DATA_STREAM_SERVICE_ZIP = "/api/data-stream-service-zip"
    }

    @PostMapping(value = [DATA_STREAM_SERVICE])
    @Operation(tags = ["dataStream/getDataStream.json"])
    suspend fun invoke(
        @RequestBody httpMessage: String,
    ): ResponseEntity<Any> {
        val request = JSON.decodeFromString(DataStreamServiceRequest.Serializer, httpMessage)
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> ${ request::class.simpleName }")
        return dataStreamService.core.invoke(request).let { ResponseEntity.ok(it) }
    }

    @PostMapping(value = [DATA_STREAM_SERVICE_ZIP])
    @Operation(tags = ["dataStream/zipRequest.json"])
    @ResponseStatus(HttpStatus.OK)
    suspend fun handleCompressedData(
        @RequestBody data: ByteArray,
    ): ResponseEntity<Any> {
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE_ZIP")
        val decompressedData = decompressGzip(data)
        val request = WS_JSON.decodeFromString(DataStreamServiceRequest.Serializer, decompressedData)
        return dataStreamService.core.invoke(request).let { ResponseEntity.ok(it) }
    }
}
