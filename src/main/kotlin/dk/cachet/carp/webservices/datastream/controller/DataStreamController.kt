package dk.cachet.carp.webservices.datastream.controller

import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.configuration.swagger.SetOfUUID
import dk.cachet.carp.webservices.common.input.WS_JSON
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.datastream.service.impl.decompressGzip
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as RequestBodySwagger

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
    @RequestBodySwagger(
        description = "Body: SERIALIZED DataStreamServiceRequest (string). See below for possible request types.",
        content = [
            Content(
                schema =
                    Schema(
                        oneOf = [
                            DataStreamServiceRequest.OpenDataStreams::class,
                            DataStreamServiceRequest.AppendToDataStreams::class,
                            DataStreamServiceRequest.GetDataStream::class,
                            DataStreamServiceRequest.CloseDataStreams::class,
                            DataStreamServiceRequest.RemoveDataStreams::class,
                        ],
                    ),
            ),
        ],
    )
    @ApiResponse(
        responseCode = "200",
        description = "Returns serialized response (as a string).",
        content = [
            Content(
                schema =
                    Schema(
                        oneOf = [
                            Unit::class,
                            Unit::class,
                            DataStreamBatch::class,
                            Unit::class,
                            SetOfUUID::class,
                        ],
                    ),
            ),
        ],
    )
    suspend fun invoke(
        @RequestBody httpMessage: String,
    ): ResponseEntity<Any> {
        val request = WS_JSON.decodeFromString(DataStreamServiceRequest.Serializer, httpMessage)
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> ${request::class.simpleName}")
        val ret = dataStreamService.core.invoke(request)
        return serializer.serializeResponse(request, ret).let { ResponseEntity.ok(it) }
    }

    @PostMapping(value = [DATA_STREAM_SERVICE_GZIP])
    @RequestBodySwagger(
        description =
            "Same as data-stream-service, but take ByteArray instead of JSON string," +
                " the ByteArray should be a compressed DataStreamServiceRequest via Gzip. " +
                "See below for possible request types.",
        content = [
            Content(
                schema =
                    Schema(
                        oneOf = [
                            DataStreamServiceRequest.OpenDataStreams::class,
                            DataStreamServiceRequest.AppendToDataStreams::class,
                            DataStreamServiceRequest.GetDataStream::class,
                            DataStreamServiceRequest.CloseDataStreams::class,
                            DataStreamServiceRequest.RemoveDataStreams::class,
                        ],
                    ),
            ),
        ],
    )
    @ApiResponse(
        responseCode = "200",
        description = "Returns serialized response (as a string).",
        content = [
            Content(
                schema =
                    Schema(
                        oneOf = [
                            Unit::class,
                            Unit::class,
                            DataStreamBatch::class,
                            Unit::class,
                            SetOfUUID::class,
                        ],
                    ),
            ),
        ],
    )
    suspend fun handleCompressedData(
        @RequestBody data: ByteArray,
    ): ResponseEntity<Any> {
        LOGGER.info("Start POST: $DATA_STREAM_SERVICE_GZIP")
        val decompressedData = decompressGzip(data)
        val request = WS_JSON.decodeFromString(DataStreamServiceRequest.Serializer, decompressedData)
        return dataStreamService.core.invoke(request).let { ResponseEntity.ok(it) }
    }
}
