package dk.cachet.carp.webservices.deployment.controller

import dk.cachet.carp.deployments.infrastructure.DeploymentServiceRequest
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceRequest
import dk.cachet.carp.webservices.dataPoint.service.DataPointService
import dk.cachet.carp.webservices.deployment.dto.DeploymentStatisticsRequestDto
import dk.cachet.carp.webservices.deployment.dto.DeploymentStatisticsResponseDto
import dk.cachet.carp.webservices.deployment.service.DeploymentService
import dk.cachet.carp.webservices.deployment.service.ParticipationService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class StudyDeploymentController(
    private val deploymentService: DeploymentService,
    private val participationService: ParticipationService,
    // should be removed when statistics endpoint gets removed
    private val dataPointService: DataPointService,
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val DEPLOYMENT_SERVICE = "/api/deployment-service"
        const val PARTICIPATION_SERVICE = "/api/participation-service"
        const val DEPLOYMENT_STATISTICS = "/api/deployment-service/statistics"
    }

    @PostMapping(value = [DEPLOYMENT_SERVICE])
    @Operation(tags = ["studyDeployment/deployments.json"])
    suspend fun deployments(
        @RequestBody request: DeploymentServiceRequest<*>,
    ): ResponseEntity<Any> {
        LOGGER.info("Start POST: $DEPLOYMENT_SERVICE -> ${ request::class.simpleName }")
        return deploymentService.core.invoke(request).let { ResponseEntity.ok(it) }
    }

    @PostMapping(value = [PARTICIPATION_SERVICE])
    @Operation(tags = ["studyDeployment/invitations.json"])
    suspend fun participation(
        @RequestBody request: ParticipationServiceRequest<*>,
    ): ResponseEntity<Any> {
        LOGGER.info("Start POST: $PARTICIPATION_SERVICE -> ${ request::class.simpleName }")
        return participationService.core.invoke(request).let { ResponseEntity.ok(it) }
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
    @PreAuthorize("false")
    @Operation(tags = ["studyDeployment/statistics.json"])
    fun statistics(
        @Valid @RequestBody request: DeploymentStatisticsRequestDto,
    ): DeploymentStatisticsResponseDto {
        LOGGER.info("Start POST: /api/deployment-service/statistics")
        return dataPointService.getStatistics(request.deploymentIds)
    }
}
