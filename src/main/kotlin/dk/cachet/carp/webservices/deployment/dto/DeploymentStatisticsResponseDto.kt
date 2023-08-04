package dk.cachet.carp.webservices.deployment.dto

/**
 * DTO for the statistical endpoint.
 * The structure is the following:
 * The first map's key is the deploymentId.
 * The second map's key is the dataFormat from [DataPointHeaderDto].
 * The final value is [StatisticsDto], which will contain the data per format.
 */
class DeploymentStatisticsResponseDto(val statistics: MutableMap<String, MutableMap<String, StatisticsDto>>)