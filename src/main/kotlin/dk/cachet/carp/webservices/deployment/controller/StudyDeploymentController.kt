package dk.cachet.carp.webservices.deployment.controller

import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.responses.BadRequestException
import dk.cachet.carp.webservices.dataPoint.service.DataPointService
import dk.cachet.carp.webservices.deployment.authorizer.DeploymentAuthorizationService
import dk.cachet.carp.webservices.deployment.dto.DeploymentStatisticsRequestDto
import dk.cachet.carp.webservices.deployment.dto.DeploymentStatisticsResponseDto
import dk.cachet.carp.webservices.deployment.service.CoreDeploymentService
import dk.cachet.carp.webservices.deployment.service.CoreParticipationService
import dk.cachet.carp.webservices.security.authentication.requireAuthenticated
import dk.cachet.carp.webservices.security.authorization.*
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class StudyDeploymentController
(
    private val validationMessages: MessageBase,
    private val dataPointService: DataPointService,
    coreParticipationService: CoreParticipationService,
    coreDeploymentService: CoreDeploymentService
)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()


        /** Endpoint URI constants */
        const val DEPLOYMENT_SERVICE = "/api/deployment-service"
        const val PARTICIPATION_SERVICE = "/api/participation-service"
        const val DEPLOYMENT_STATISTICS = "/api/deployment-service/statistics"
    }

    private val participationService = coreParticipationService.instance

    private val deploymentService = coreDeploymentService.instance

    @PostMapping(value = [DEPLOYMENT_SERVICE])
    @Operation(tags = ["studyDeployment/deployments.json"])
    suspend fun deployments(@RequestBody request: DeploymentServiceRequest<*>): ResponseEntity<Any> =
        when (request)
        {
            is DeploymentServiceRequest.CreateStudyDeployment ->
            {
                requireRole( Role.RESEARCHER )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> CreateStudyDeployment")
                val result = deploymentService.createStudyDeployment(
                        request.id,
                        request.protocol,
                        request.invitations,
                        request.connectedDevicePreregistrations
                )
                ResponseEntity.status(HttpStatus.CREATED).body(result)
            }
            is DeploymentServiceRequest.RemoveStudyDeployments ->
            {
                requireClaims( Claim.ManageDeployment( request.studyDeploymentIds ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> RemoveStudyDeployments")
                val result = deploymentService.removeStudyDeployments(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.GetStudyDeploymentStatus ->
            {
                requireClaims( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> GetStudyDeploymentStatus")
                val result = deploymentService.getStudyDeploymentStatus(request.studyDeploymentId)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.GetStudyDeploymentStatusList ->
            {
                requireClaims( Claim.InDeployment( request.studyDeploymentIds ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> GetStudyDeploymentStatusList")
                val result = deploymentService.getStudyDeploymentStatusList(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.RegisterDevice ->
            {
                requireClaims( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> RegisterDevice")
                val result = deploymentService.registerDevice(request.studyDeploymentId, request.deviceRoleName, request.registration)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.UnregisterDevice ->
            {
                requireClaims( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> UnregisterDevice")
                val result = deploymentService.unregisterDevice(request.studyDeploymentId, request.deviceRoleName)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.GetDeviceDeploymentFor ->
            {
                requireClaims( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> GetDeviceDeploymentFor")
                val result = deploymentService.getDeviceDeploymentFor(request.studyDeploymentId, request.primaryDeviceRoleName)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.DeviceDeployed ->
            {
                requireClaims( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> DeploymentSuccessful")
                val result = deploymentService.deviceDeployed(request.studyDeploymentId, request.primaryDeviceRoleName, request.deviceDeploymentLastUpdatedOn)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.Stop ->
            {
                requireClaims( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> Stop")
                val result = deploymentService.stop(request.studyDeploymentId)
                ResponseEntity.ok(result)
            }
            else ->
            {
                throw BadRequestException(validationMessages.get("deployment.handle_all.invalid.request", request))
            }
        }

    @PostMapping(value = [PARTICIPATION_SERVICE])
    @Operation(tags = ["studyDeployment/invitations.json"])
    suspend fun invitations(@RequestBody request: ParticipationServiceRequest<*>): ResponseEntity<Any> =
        when (request)
        {
            is ParticipationServiceRequest.GetActiveParticipationInvitations ->
            {
                requireAuthenticated( request.accountId )

                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> GetActiveParticipationInvitations")
                val result = participationService.getActiveParticipationInvitations(request.accountId)
                ResponseEntity.ok(result)
            }
            is ParticipationServiceRequest.GetParticipantData ->
            {
                requireClaims( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> GetParticipantData")
                val result = participationService.getParticipantData(request.studyDeploymentId)
                ResponseEntity.ok(result)
            }
            is ParticipationServiceRequest.GetParticipantDataList ->
            {
                requireClaims( Claim.InDeployment( request.studyDeploymentIds ) )

                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> GetParticipantDataList")
                val result = participationService.getParticipantDataList(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            is ParticipationServiceRequest.SetParticipantData ->
            {
                requireClaims( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> SetParticipantData")
                val result = participationService.setParticipantData(request.studyDeploymentId, request.data, request.inputByParticipantRole)
                ResponseEntity.ok(result)
            }
            else ->
            {
                throw BadRequestException(validationMessages.get("deployment.handle_all.invalid.request", request))
            }
        }

    /**
     * Statistics endpoint is disabled, due to a refactor of the authorization
     * services with clear service boundaries. Also, none of the current clients
     * rely on this functionality.
     *
     * If there is ever a need for a statistics endpoint, there should probably be
     * at least two of those: one for study management, that takes in a study ID and
     * calculates all the relevant statistics for a study, and one which takes a single
     * deployment ID as parameter, this could be used for displaying study related
     * statistics for a single participant group.
     */
    @PostMapping(value = [DEPLOYMENT_STATISTICS])
    @PreAuthorize("#{false}")
    @Operation(tags = ["studyDeployment/statistics.json"])
    fun statistics(@Valid @RequestBody request: DeploymentStatisticsRequestDto): DeploymentStatisticsResponseDto
    {
        LOGGER.info("Start POST: /api/deployment-service/statistics")
        return dataPointService.getStatistics(request.deploymentIds)
    }
}