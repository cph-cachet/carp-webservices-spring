package dk.cachet.carp.webservices.data.controller

import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.input.WS_JSON
import dk.cachet.carp.webservices.data.service.CawsDataStreamService
import dk.cachet.carp.webservices.data.service.impl.decompressGzip
import io.swagger.v3.oas.annotations.Operation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class DataStreamController(
    private val cawsDataStreamService: CawsDataStreamService,
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
        @RequestBody request: DataStreamServiceRequest<*>,
    ): ResponseEntity<Any> {
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> ${ request::class.simpleName }")
        return cawsDataStreamService.core.invoke(request).let { ResponseEntity.ok(it) }
    }

    @Deprecated("Use POST /api/data-stream-service-zip instead.")
    @PostMapping(value = ["null"], consumes = ["multipart/form-data"])
    @Operation(tags = ["dataStream/getDataStream.zip"])
    suspend fun processToInvoke(
        @RequestBody zipFile: MultipartFile,
    ): ResponseEntity<Any> {
        return cawsDataStreamService.processZipToInvoke(zipFile).let { ResponseEntity.ok(it) }
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
        return cawsDataStreamService.core.invoke(request).let { ResponseEntity.ok(it) }
    }
}
