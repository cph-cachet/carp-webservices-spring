package dk.cachet.carp.webservices.data.controller

import dk.cachet.carp.data.application.DataStreamService
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.responses.BadRequestException
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.requireClaims
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class DataStreamController(
        private val validationMessages: MessageBase,
        private val dataStreamService: DataStreamService
)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */

        const val DATA_STREAM_SERVICE = "/api/data-stream-service"
    }


    @PostMapping(value = [DATA_STREAM_SERVICE])
    @Operation(tags = ["dataStream/getDataStream.json"])
    suspend fun getDataStream(
            @RequestBody request: DataStreamServiceRequest<*>
    ): ResponseEntity<Any> =
        when (request)
        {
            is DataStreamServiceRequest.OpenDataStreams -> {
                requireClaims( Claim.ManageDeployment( request.configuration.studyDeploymentId ) )

                LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> OpenDataStreams")
                val result = dataStreamService.openDataStreams(request.configuration)
                ResponseEntity.status(HttpStatus.CREATED).body(result)
            }
            is DataStreamServiceRequest.AppendToDataStreams -> {
                requireClaims( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> AppendToDataStreams")
                val result = dataStreamService.appendToDataStreams(request.studyDeploymentId, request.batch)
                ResponseEntity.ok(result)
            }
            is DataStreamServiceRequest.GetDataStream -> {
                requireClaims( Claim.InDeployment( request.dataStream.studyDeploymentId ) )

                LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> GetDataStream")
                val result = dataStreamService.getDataStream(request.dataStream, request.fromSequenceId, request.toSequenceIdInclusive)
                ResponseEntity.ok(result)
            }
            is DataStreamServiceRequest.CloseDataStreams -> {
                requireClaims( Claim.ManageDeployment( request.studyDeploymentIds ) )

                LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> CloseDataStreams")
                val result = dataStreamService.closeDataStreams(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            is DataStreamServiceRequest.RemoveDataStreams -> {
                requireClaims( Claim.ManageDeployment( request.studyDeploymentIds ) )

                LOGGER.info("Start POST: $DATA_STREAM_SERVICE -> RemoveDataStreams")
                val result = dataStreamService.removeDataStreams(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            else ->
            {
                throw BadRequestException(validationMessages.get("data.stream.service.request.error", request))
            }
        }
}