package dk.cachet.carp.webservices.dataVisualization.service;

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.dataVisualization.dto.BarChartDataDto
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import kotlinx.datetime.Instant
import org.springframework.stereotype.Service

@Service
class DataVisualizationService(
    val dataStreamService: DataStreamService,
) {
    fun getBarChartData(
        studyId: UUID, deploymentId: UUID, scope: String, type: String, from: Instant, to: Instant
    ): BarChartDataDto {
        if (type == "survey") {
            return getBarChartDataForSurvey(studyId, deploymentId, scope, from, to)
        }

        return BarChartDataDto()
    }

    private fun getBarChartDataForSurvey(
        studyId: UUID, deploymentId: UUID, scope: String, from: Instant, to: Instant
    ): BarChartDataDto {
        if (scope == "deployment") {
            val dataStreamIds = dataStreamService.findDataStreamIdsByDeploymentId(deploymentId)
//            return getBarChartDataForSurveyInStudy(studyId, from, to)
        }
        return BarChartDataDto()
    }
}
