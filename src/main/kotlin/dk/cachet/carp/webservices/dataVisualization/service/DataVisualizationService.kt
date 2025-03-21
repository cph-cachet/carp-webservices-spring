package dk.cachet.carp.webservices.dataVisualization.service;

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.dataVisualization.dto.BarChartDataDto
import dk.cachet.carp.webservices.dataVisualization.dto.DayKeyQuantityTriple
import dk.cachet.carp.webservices.dataVisualization.dto.TimeSeriesEntryDto
import dk.cachet.carp.webservices.datastream.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import kotlinx.datetime.Instant
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class DataVisualizationService(
    val dataStreamService: DataStreamService,
    val dataStreamSequenceRepository: DataStreamSequenceRepository
) {
    fun getBarChartData(
        studyId: UUID, deploymentId: UUID, scope: String, type: String, from: Long, to: Long
    ): BarChartDataDto {
        if (type == "survey") {
            return getBarChartDataForSurvey(studyId, deploymentId, scope, from, to)
        }

        return BarChartDataDto()
    }

    private fun getBarChartDataForSurvey(
        studyId: UUID, deploymentId: UUID, scope: String, from: Long, to: Long
    ): BarChartDataDto {
        if (scope == "deployment") {
            val dataStreamIds = dataStreamService.findDataStreamIdsByDeploymentId(deploymentId)
//            val listOfDayKeyQuantityTriple = dataStreamService.idkhowtonamethisForSurveys(dataStreamIds, from, to, studyId.toString())
            val kek = dataStreamSequenceRepository.idkhowtonamethis(
                dataStreamIds,
                Timestamp(from * 1000),
                Timestamp(to * 1000),
                studyId.toString()
            )

            return dayKeyQuantityTriplesToBarChartDataDto   (kek)


            println()
//            return getBarChartDataForSurveyInStudy(studyId, from, to)
        }
        return BarChartDataDto()
    }

    private fun dayKeyQuantityTriplesToBarChartDataDto(dayKeyQuantityTriple: List<DayKeyQuantityTriple>): BarChartDataDto {
        val dto = BarChartDataDto()
        for (triple in dayKeyQuantityTriple) {
            val existingTimeSeriesEntry = dto.timeSeries.find { it.x == triple.day }
            if (existingTimeSeriesEntry != null) {
                existingTimeSeriesEntry.y.put(triple.key, triple.quantity)
            } else {
                val newTimeSeriesEntry = TimeSeriesEntryDto(triple.day, hashMapOf(Pair(triple.key, triple.quantity)))
                dto.timeSeries.add(newTimeSeriesEntry)
            }
        }

        return dto
    }
}

