package dk.cachet.carp.webservices.deployment.controller

import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceRequest
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.responses.BadRequestException
import dk.cachet.carp.webservices.dataPoint.service.DataPointService
import dk.cachet.carp.webservices.deployment.dto.DeploymentStatisticsRequestDto
import dk.cachet.carp.webservices.deployment.dto.DeploymentStatisticsResponseDto
import dk.cachet.carp.webservices.deployment.service.CoreDeploymentService
import dk.cachet.carp.webservices.deployment.service.CoreParticipationService
import dk.cachet.carp.webservices.security.authorization.*
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
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
    private val authorizationService: AuthorizationService,
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
                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> CreateStudyDeployment")
                val result = deploymentService.createStudyDeployment(
                        request.id,
                        request.protocol,
                        request.invitations,
                        request.connectedDevicePreregistrations
                )

                authorizationService.grantCurrentAuthentication(
                    setOf(
                        Claim.ManageDeployment(result.studyDeploymentId),
                        Claim.InDeployment( result.studyDeploymentId )
                    )
                )

                ResponseEntity.status(HttpStatus.CREATED).body(result)
            }
            is DeploymentServiceRequest.RemoveStudyDeployments ->
            {
                authorizationService.require( request.studyDeploymentIds.map { Claim.ManageDeployment(it) }.toSet() )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> RemoveStudyDeployments")
                val result = deploymentService.removeStudyDeployments(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.GetStudyDeploymentStatus ->
            {
                authorizationService.require( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> GetStudyDeploymentStatus")
                val result = deploymentService.getStudyDeploymentStatus(request.studyDeploymentId)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.GetStudyDeploymentStatusList ->
            {
                authorizationService.require( request.studyDeploymentIds.map { Claim.InDeployment(it) }.toSet() )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> GetStudyDeploymentStatusList")
                val result = deploymentService.getStudyDeploymentStatusList(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.RegisterDevice ->
            {
                authorizationService.require( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> RegisterDevice")
                val result = deploymentService.registerDevice(request.studyDeploymentId, request.deviceRoleName, request.registration)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.UnregisterDevice ->
            {
                authorizationService.require( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> UnregisterDevice")
                val result = deploymentService.unregisterDevice(request.studyDeploymentId, request.deviceRoleName)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.GetDeviceDeploymentFor ->
            {
                authorizationService.require( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> GetDeviceDeploymentFor")
                val result = deploymentService.getDeviceDeploymentFor(request.studyDeploymentId, request.primaryDeviceRoleName)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.DeviceDeployed ->
            {
                authorizationService.require( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> DeploymentSuccessful")
                val result = deploymentService.deviceDeployed(request.studyDeploymentId, request.primaryDeviceRoleName, request.deviceDeploymentLastUpdatedOn)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.Stop ->
            {
                authorizationService.require( Claim.InDeployment( request.studyDeploymentId ) )

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
                authorizationService.requireOwner( request.accountId )

                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> GetActiveParticipationInvitations")
                val result = participationService.getActiveParticipationInvitations(request.accountId)
                ResponseEntity.ok(result)
            }
            is ParticipationServiceRequest.GetParticipantData ->
            {
                authorizationService.require( Claim.InDeployment( request.studyDeploymentId ) )

                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> GetParticipantData")
                val result = participationService.getParticipantData(request.studyDeploymentId)
                ResponseEntity.ok(result)
            }
            is ParticipationServiceRequest.GetParticipantDataList ->
            {
                authorizationService.require( request.studyDeploymentIds.map { Claim.InDeployment(it) }.toSet() )

                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> GetParticipantDataList")
                val result = participationService.getParticipantDataList(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            is ParticipationServiceRequest.SetParticipantData ->
            {
                authorizationService.require( Claim.InDeployment( request.studyDeploymentId ) )

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