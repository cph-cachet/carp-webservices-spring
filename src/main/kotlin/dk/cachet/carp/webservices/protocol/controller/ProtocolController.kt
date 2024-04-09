package dk.cachet.carp.webservices.protocol.controller

import dk.cachet.carp.protocols.application.ProtocolFactoryService
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceHost
import dk.cachet.carp.protocols.application.ProtocolService
import dk.cachet.carp.protocols.application.ProtocolServiceHost
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceRequest
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.exception.responses.BadRequestException
import dk.cachet.carp.webservices.protocol.authorization.ProtocolAuthorizationService
import dk.cachet.carp.webservices.protocol.dto.GetLatestProtocolResponseDto
import dk.cachet.carp.webservices.protocol.repository.CoreProtocolRepository
import dk.cachet.carp.webservices.security.authorization.*
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
class ProtocolController
(
    private val coreProtocolRepository: CoreProtocolRepository,
    private val protocolAuthorizationService: ProtocolAuthorizationService,
    private val validationMessages: MessageBase
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

    private val protocolService: ProtocolService = ProtocolServiceHost(coreProtocolRepository)

    private val protocolFactoryService: ProtocolFactoryService = ProtocolFactoryServiceHost()

    @PostMapping(value = [PROTOCOL_SERVICE])
    @Operation(tags = ["protocol/protocols.json"])
    suspend fun protocols(@RequestBody request: ProtocolServiceRequest<*>): ResponseEntity<Any> =
        when (request)
        {
            is ProtocolServiceRequest.Add ->
            {
                requireRole( Role.RESEARCHER )
                requireAuthenticated( request.protocol.ownerId )

                LOGGER.info("Start POST: $PROTOCOL_SERVICE -> Add")
                protocolService.add(request.protocol, request.versionTag)
                ResponseEntity.status(HttpStatus.CREATED).build()

                // Grant protocol owner
            }
            is ProtocolServiceRequest.AddVersion ->
            {
                requireAuthenticated( request.protocol.ownerId )

                LOGGER.info("Start POST: $PROTOCOL_SERVICE -> AddVersion")
                protocolService.addVersion(request.protocol, request.versionTag)
                ResponseEntity.status(HttpStatus.OK).build()
            }
            is ProtocolServiceRequest.UpdateParticipantDataConfiguration ->
            {
                requireClaims( Claim.ProtocolOwner( request.protocolId ) )

                LOGGER.info("Start POST: $PROTOCOL_SERVICE -> UpdateParticipantDataConfiguration")
                val results = protocolService.updateParticipantDataConfiguration(request.protocolId, request.versionTag, request.expectedParticipantData)
                ResponseEntity.ok(results)
            }
            is ProtocolServiceRequest.GetBy ->
            {
                requireClaims( Claim.ProtocolOwner( request.protocolId ) )

                LOGGER.info("Start POST: $PROTOCOL_SERVICE -> GetBy")
                val result = protocolService.getBy(request.protocolId, request.versionTag)
                ResponseEntity.ok(result)
            }
            is ProtocolServiceRequest.GetAllForOwner ->
            {
                requireAuthenticated( request.ownerId )

                LOGGER.info("Start POST: $PROTOCOL_SERVICE -> GetAllFor")
                val results = protocolService.getAllForOwner(request.ownerId)
                ResponseEntity.ok(results)
            }
            is ProtocolServiceRequest.GetVersionHistoryFor ->
            {
                requireClaims( Claim.ProtocolOwner( request.protocolId ) )

                LOGGER.info("Start POST: $PROTOCOL_SERVICE -> GetVersionHistoryFor")
                val results = protocolService.getVersionHistoryFor(request.protocolId)
                ResponseEntity.ok(results)
            }
            else ->
            {
                LOGGER.warn("Invalid protocol request!")
                throw BadRequestException(validationMessages.get("protocol.service.handle_all.invalid.request", request))
            }
        }

    @PostMapping(value = [PROTOCOL_FACTORY_SERVICE])
    @Operation(tags = ["protocol/protocolFactory.json"])
    suspend fun protocolFactory(@RequestBody request: ProtocolFactoryServiceRequest<*>): ResponseEntity<Any> =
        when (request)
        {
            is ProtocolFactoryServiceRequest.CreateCustomProtocol ->
            {
                requireRole( Role.RESEARCHER )

                LOGGER.info("Start POST: $PROTOCOL_FACTORY_SERVICE -> CreateCustomProtocol")
                val result = protocolFactoryService.createCustomProtocol(
                    request.ownerId,
                    request.name,
                    request.customProtocol,
                    request.description)
                ResponseEntity.ok(result)
            }
            else ->
            {
                LOGGER.warn("Invalid protocol request!")
                throw BadRequestException(validationMessages.get("protocol.factory.invalid.request", request))
            }
        }


    @GetMapping(value = [GET_LATEST_PROTOCOL])
    @PreAuthorize("@protocolAuthorizationService.canViewProtocol()")
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