package dk.cachet.carp.webservices.protocol.controller

import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.common.input.WS_JSON
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.protocol.dto.ProtocolOverview
import dk.cachet.carp.webservices.protocol.serdes.ProtocolFactoryRequestSerializer
import dk.cachet.carp.webservices.protocol.serdes.ProtocolRequestSerializer
import dk.cachet.carp.webservices.protocol.service.ProtocolService
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.parameters.RequestBody as RequestBodySwagger

@RestController
class ProtocolController(
    private val services: CoreServiceContainer,
    private val authenticationService: AuthenticationService,
    private val protocolService: ProtocolService,
) {
    companion object {
        val LOGGER: Logger = LogManager.getLogger()
        val serializer: ProtocolRequestSerializer = ProtocolRequestSerializer()
        val factorySerializer: ProtocolFactoryRequestSerializer = ProtocolFactoryRequestSerializer()

        /** Path variables */
        const val PROTOCOL_SERVICE = "/api/protocol-service"
        const val PROTOCOL_FACTORY_SERVICE = "/api/protocol-factory-service"
        const val GET_PROTOCOL_OVERVIEW = "/api/protocols/{${PathVariableName.PROTOCOL_ID}}/latest"
        const val GET_PROTOCOLS_OVERVIEW = "/api/protocols-overview"
    }

    @GetMapping(value = [GET_PROTOCOL_OVERVIEW])
    @PreAuthorize("hasRole('RESEARCHER')")
    @ResponseStatus(HttpStatus.OK)
    suspend fun getSingleProtocolOverview(
        @PathVariable(PathVariableName.PROTOCOL_ID) protocolId: String,
    ): String {
        LOGGER.info("/api/protocols/$protocolId/latest")
        val overview =
            protocolService.getSingleProtocolOverview(protocolId)
                ?: throw ResourceNotFoundException("No protocol found with id $protocolId.")
        return WS_JSON.encodeToString(ProtocolOverview.serializer(), overview)
    }

    @GetMapping(value = [GET_PROTOCOLS_OVERVIEW])
    @PreAuthorize("hasRole('RESEARCHER')")
    @ResponseStatus(HttpStatus.OK)
    suspend fun getProtocolsOverview(): List<ProtocolOverview> {
        LOGGER.info("Start GET: /api/protocols")
        return protocolService.getProtocolsOverview(authenticationService.getId())
    }

    @PostMapping(value = [PROTOCOL_SERVICE])
    @RequestBodySwagger(
        description = "Body: SERIALIZED ProtocolServiceRequest (string). See below for possible request types.",
        content = [
            Content(
                schema =
                    Schema(
                        oneOf = [
                            ProtocolServiceRequest.Add::class,
                            ProtocolServiceRequest.AddVersion::class,
                            ProtocolServiceRequest.UpdateParticipantDataConfiguration::class,
                            ProtocolServiceRequest.GetBy::class,
                            ProtocolServiceRequest.GetAllForOwner::class,
                            ProtocolServiceRequest.GetVersionHistoryFor::class,
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
                            StudyProtocolSnapshot::class,
                            StudyProtocolSnapshot::class,
                            Array<StudyProtocolSnapshot>::class,
                            Array<ProtocolVersion>::class,
                        ],
                    ),
            ),
        ],
    )
    suspend fun protocols(
        @RequestBody httpMessage: String,
    ): ResponseEntity<Any> {
        val request = WS_JSON.decodeFromString(ProtocolServiceRequest.Serializer, httpMessage)
        LOGGER.info("Start POST: $PROTOCOL_SERVICE -> ${request::class.simpleName}")
        val result = protocolService.core.invoke(request)
        return serializer.serializeResponse(request, result).let { ResponseEntity.ok(it) }
    }

    @PostMapping(value = [PROTOCOL_FACTORY_SERVICE])
    @RequestBodySwagger(
        description = "Body: SERIALIZED ProtocolFactoryServiceRequest (string). See below for possible request types.",
        content = [
            Content(
                schema =
                    Schema(
                        oneOf = [
                            ProtocolFactoryServiceRequest.CreateCustomProtocol::class,
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
                        ],
                    ),
            ),
        ],
    )
    suspend fun protocolFactory(
        @RequestBody httpMessage: String,
    ): ResponseEntity<Any> {
        val request = WS_JSON.decodeFromString(ProtocolFactoryServiceRequest.Serializer, httpMessage)
        LOGGER.info("Start POST: $PROTOCOL_FACTORY_SERVICE -> ${request::class.simpleName}")
        val result = services.protocolFactoryService.invoke(request)
        return factorySerializer.serializeResponse(request, result).let { ResponseEntity.ok(it) }
    }
}
