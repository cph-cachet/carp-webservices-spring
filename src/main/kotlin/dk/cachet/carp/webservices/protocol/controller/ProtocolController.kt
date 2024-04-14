package dk.cachet.carp.webservices.protocol.controller

import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.deployment.controller.StudyDeploymentController
import dk.cachet.carp.webservices.deployment.controller.StudyDeploymentController.Companion
import dk.cachet.carp.webservices.deployment.controller.StudyDeploymentController.Companion.DEPLOYMENT_SERVICE
import dk.cachet.carp.webservices.protocol.dto.GetLatestProtocolResponseDto
import dk.cachet.carp.webservices.protocol.repository.CoreProtocolRepository
import dk.cachet.carp.webservices.protocol.service.CoreProtocolFactoryService
import dk.cachet.carp.webservices.protocol.service.CoreProtocolService
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
    // TODO: repository acts as a service (it shouldn't) when this gets sorted out, remove the reference
    private val coreProtocolRepository: CoreProtocolRepository,
    protocolService: CoreProtocolService,
    protocolFactoryService: CoreProtocolFactoryService
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

    private val protocolService = protocolService.instance
    private val protocolFactoryService = protocolFactoryService.instance

    @PostMapping(value = [PROTOCOL_SERVICE])
    @Operation(tags = ["protocol/protocols.json"])
    suspend fun protocols(@RequestBody request: ProtocolServiceRequest<*>): ResponseEntity<Any>
    {
        LOGGER.info("Start POST: $PROTOCOL_SERVICE -> ${ request::class.simpleName }")
        return protocolService.invoke( request ).let { ResponseEntity.ok( it ) }
    }

    @PostMapping(value = [PROTOCOL_FACTORY_SERVICE])
    @Operation(tags = ["protocol/protocolFactory.json"])
    suspend fun protocolFactory(@RequestBody request: ProtocolFactoryServiceRequest<*>): ResponseEntity<Any>
    {
        LOGGER.info("Start POST: $PROTOCOL_FACTORY_SERVICE -> ${ request::class.simpleName }")
        return protocolFactoryService.invoke( request ).let { ResponseEntity.ok( it ) }
    }

    @GetMapping(value = [GET_LATEST_PROTOCOL])
    @PreAuthorize("#{false}")
    @Operation(tags = ["protocol/getLatestProtocolById.json"])
    fun getLatestProtocolById (
        @PathVariable(PathVariableName.PROTOCOL_ID) protocolId: String
    ): GetLatestProtocolResponseDto?
    {
        return runBlocking {
            LOGGER.info("/api/protocols/$protocolId/latest")
            return@runBlocking coreProtocolRepository.getLatestProtocolById(protocolId)
        }
    }
}