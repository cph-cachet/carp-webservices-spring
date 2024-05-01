package dk.cachet.carp.webservices.protocol.controller

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.protocol.dto.GetLatestProtocolResponseDto
import dk.cachet.carp.webservices.protocol.service.ProtocolService
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
class ProtocolController
(
    private val services: CoreServiceContainer,
    private val protocolService: ProtocolService,
)
{
    companion object
    {
        val LOGGER: Logger = LogManager.getLogger()

        /** Path variables */
        const val PROTOCOL_SERVICE = "/api/protocol-service"
        const val PROTOCOL_FACTORY_SERVICE = "/api/protocol-factory-service"
        const val GET_LATEST_PROTOCOL = "/api/protocols/{${PathVariableName.PROTOCOL_ID}}/latest"
    }

    @PostMapping(value = [PROTOCOL_SERVICE])
    @Operation(tags = ["protocol/protocols.json"])
    suspend fun protocols(@RequestBody request: ProtocolServiceRequest<*>): ResponseEntity<Any>
    {
        LOGGER.info("Start POST: $PROTOCOL_SERVICE -> ${ request::class.simpleName }")
        return protocolService.core.invoke( request ).let { ResponseEntity.ok( it ) }
    }

    @PostMapping(value = [PROTOCOL_FACTORY_SERVICE])
    @Operation(tags = ["protocol/protocolFactory.json"])
    suspend fun protocolFactory(@RequestBody request: ProtocolFactoryServiceRequest<*>): ResponseEntity<Any>
    {
        LOGGER.info("Start POST: $PROTOCOL_FACTORY_SERVICE -> ${ request::class.simpleName }")
        return services.protocolFactoryService.invoke( request ).let { ResponseEntity.ok( it ) }
    }

    @GetMapping(value = [GET_LATEST_PROTOCOL])
    @PreAuthorize("isProtocolOwner(#protocolId)")
    @Operation(tags = ["protocol/getLatestProtocolById.json"])
    fun getLatestProtocolById (
        @PathVariable(PathVariableName.PROTOCOL_ID) protocolId: UUID
    ): GetLatestProtocolResponseDto?
    {
        return runBlocking {
            LOGGER.info("/api/protocols/$protocolId/latest")
            return@runBlocking protocolService.getLatestProtocolById(protocolId.stringRepresentation)
        }
    }
}