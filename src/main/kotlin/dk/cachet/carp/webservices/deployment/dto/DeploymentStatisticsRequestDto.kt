package dk.cachet.carp.webservices.deployment.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

/**
 * The Data Class [DeploymentStatisticsRequestDto].
 * The [DeploymentStatisticsRequestDto] is a request DTO, which contains a list of deployment ID's.
 */
data class DeploymentStatisticsRequestDto(
    @field:NotNull
    @field:NotEmpty
    val deploymentIds: List<String>,
)
