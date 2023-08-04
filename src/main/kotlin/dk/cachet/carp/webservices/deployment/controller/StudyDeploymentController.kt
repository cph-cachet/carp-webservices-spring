package dk.cachet.carp.webservices.deployment.controller

import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.responses.BadRequestException
import dk.cachet.carp.webservices.dataPoint.service.IDataPointService
import dk.cachet.carp.webservices.deployment.authorizer.DeploymentAuthorizationService
import dk.cachet.carp.webservices.deployment.dto.DeploymentStatisticsRequestDto
import dk.cachet.carp.webservices.deployment.dto.DeploymentStatisticsResponseDto
import dk.cachet.carp.webservices.deployment.service.CoreDeploymentService
import dk.cachet.carp.webservices.deployment.service.CoreParticipationService
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
    private val deploymentAuthorizationService: DeploymentAuthorizationService,
    private val dataPointService: IDataPointService,
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
    fun deployments(@RequestBody request: DeploymentServiceRequest<*>): ResponseEntity<Any> = runBlocking {

        return@runBlocking when (request)
        {
            is DeploymentServiceRequest.CreateStudyDeployment ->
            {
                if (!deploymentAuthorizationService.canCreateDeployment())
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> CreateStudyDeployment")
                val result = deploymentService.createStudyDeployment(
                        request.id,
                        request.protocol,
                        request.invitations,
                        request.connectedDevicePreregistrations
                )
                ResponseEntity.status(HttpStatus.CREATED).body(result)
            }
            is DeploymentServiceRequest.GetStudyDeploymentStatus ->
            {
                if (!deploymentAuthorizationService.canViewDeployment(request.studyDeploymentId.stringRepresentation))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> GetStudyDeploymentStatus")
                val result = deploymentService.getStudyDeploymentStatus(request.studyDeploymentId)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.RegisterDevice ->
            {
                if (!deploymentAuthorizationService.canRegisterDevice(request))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> RegisterDevice")
                val result = deploymentService.registerDevice(request.studyDeploymentId, request.deviceRoleName, request.registration)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.GetDeviceDeploymentFor ->
            {
                if (!deploymentAuthorizationService.canGetMasterDeviceDeployments(request.studyDeploymentId.stringRepresentation))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> GetDeviceDeploymentFor")
                val result = deploymentService.getDeviceDeploymentFor(request.studyDeploymentId, request.primaryDeviceRoleName)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.DeviceDeployed ->
            {
                if (!deploymentAuthorizationService.canGetDeploymentSuccessfulStatus(request.studyDeploymentId.stringRepresentation))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> DeploymentSuccessful")
                val result = deploymentService.deviceDeployed(request.studyDeploymentId, request.primaryDeviceRoleName, request.deviceDeploymentLastUpdatedOn)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.UnregisterDevice ->
            {
                if (!deploymentAuthorizationService.canUnregisterDevice(request.studyDeploymentId.stringRepresentation))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> UnregisterDevice")
                val result = deploymentService.unregisterDevice(request.studyDeploymentId, request.deviceRoleName)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.Stop ->
            {
                if (!deploymentAuthorizationService.canStop(request.studyDeploymentId.stringRepresentation))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> Stop")
                val result = deploymentService.stop(request.studyDeploymentId)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.GetStudyDeploymentStatusList ->
            {
                if (!deploymentAuthorizationService.canGetStudyDeploymentStatusList(request))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> GetStudyDeploymentStatusList")
                val result = deploymentService.getStudyDeploymentStatusList(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            is DeploymentServiceRequest.RemoveStudyDeployments ->
            {
                if (!deploymentAuthorizationService.canDeleteDeployments(request.studyDeploymentIds))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> RemoveStudyDeployments")
                val result = deploymentService.removeStudyDeployments(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            else ->
            {
                throw BadRequestException(validationMessages.get("deployment.handle_all.invalid.request", request))
            }
        }
    }

    @PostMapping(value = [PARTICIPATION_SERVICE])
    @Operation(tags = ["studyDeployment/invitations.json"])
    fun invitations(@RequestBody request: ParticipationServiceRequest<*>): ResponseEntity<Any> = runBlocking {
        return@runBlocking when (request)
        {
                is ParticipationServiceRequest.GetActiveParticipationInvitations ->
            {
                if (!deploymentAuthorizationService.canGetParticipationInvitations(request.accountId.stringRepresentation))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> GetActiveParticipationInvitations")
                val result = participationService.getActiveParticipationInvitations(request.accountId)
                ResponseEntity.ok(result)
            }
            is ParticipationServiceRequest.GetParticipantData ->
            {
                if (!deploymentAuthorizationService.canGetParticipantData(request.studyDeploymentId.stringRepresentation))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> GetParticipantData")
                val result = participationService.getParticipantData(request.studyDeploymentId)
                ResponseEntity.ok(result)
            }
            is ParticipationServiceRequest.GetParticipantDataList ->
            {
                if (!deploymentAuthorizationService.canGetParticipantDataList(request.studyDeploymentIds))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> GetParticipantDataList")
                val result = participationService.getParticipantDataList(request.studyDeploymentIds)
                ResponseEntity.ok(result)
            }
            is ParticipationServiceRequest.SetParticipantData ->
            {
                if (!deploymentAuthorizationService.canSetParticipantData(request.studyDeploymentId.stringRepresentation))
                {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> SetParticipantData")
                val result = participationService.setParticipantData(request.studyDeploymentId, request.data, request.inputByParticipantRole)
                ResponseEntity.ok(result)
            }
            else ->
            {
                throw BadRequestException(validationMessages.get("deployment.handle_all.invalid.request", request))
            }
        }
    }

    @PostMapping(value = [DEPLOYMENT_STATISTICS])
    @PreAuthorize("@deploymentAuthorizationService.canGetStatistics(#request.deploymentIds)")
    @Operation(tags = ["studyDeployment/statistics.json"])
    fun statistics(@Valid @RequestBody request: DeploymentStatisticsRequestDto): DeploymentStatisticsResponseDto
    {
        LOGGER.info("Start POST: /api/deployment-service/statistics")
        return dataPointService.getStatistics(request.deploymentIds)
    }
}