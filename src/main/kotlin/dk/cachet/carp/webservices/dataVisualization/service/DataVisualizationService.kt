package dk.cachet.carp.webservices.dataVisualization.service;

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.domain.users.ParticipantRepository
import dk.cachet.carp.webservices.dataVisualization.dto.BarChartDataDto
import dk.cachet.carp.webservices.dataVisualization.dto.DayKeyQuantityTriple
import dk.cachet.carp.webservices.dataVisualization.dto.TimeSeriesEntryDto
import dk.cachet.carp.webservices.datastream.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.deployment.repository.CoreParticipationRepository
import dk.cachet.carp.webservices.study.service.RecruitmentService
import dk.cachet.carp.webservices.study.service.impl.RecruitmentServiceWrapper
import kotlinx.datetime.Instant
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class DataVisualizationService(
    val dataStreamService: DataStreamService,
    val dataStreamSequenceRepository: DataStreamSequenceRepository,
    val recruitmentServiceWrapper: RecruitmentService,
    val participantRepository: ParticipantRepository,
    val coreParticipationRepository: CoreParticipationRepository
) {
    suspend fun getBarChartData(
        studyId: UUID, deploymentId: UUID, participantId: UUID, scope: String, type: String, from: Long, to: Long
    ): BarChartDataDto {
        if (type == "survey") {
            return getBarChartDataForSurvey(studyId, deploymentId, participantId, scope, from, to)
        }

        return BarChartDataDto()
    }

    private suspend fun getBarChartDataForSurvey(
        studyId: UUID, deploymentId: UUID, participantId: UUID, scope: String, from: Long, to: Long
    ): BarChartDataDto {
        if (scope == "deployment") {
            val dataStreamIds = dataStreamService.findDataStreamIdsByDeploymentId(deploymentId)
            val kek = dataStreamSequenceRepository.idkhowtonamethis(
                dataStreamIds,
                Timestamp(from * 1000),
                Timestamp(to * 1000),
                studyId.toString()
            )

            return dayKeyQuantityTriplesToBarChartDataDto(kek)
        } else if (scope == "study") {
            val deploymentIds = participantRepository.getRecruitment(studyId)?.participantGroups?.keys?.toSet();
            if (deploymentIds == null) throw IllegalArgumentException("No deployment ids found for study $studyId")
            val dataStreamIds = deploymentIds
                .flatMap { dataStreamService.findDataStreamIdsByDeploymentId(it) }
                .toSet()

            val kek = dataStreamSequenceRepository.idkhowtonamethis(
                dataStreamIds.toList(),
                Timestamp(from * 1000),
                Timestamp(to * 1000),
                studyId.toString()
            )
            return dayKeyQuantityTriplesToBarChartDataDto(kek)
        } else {
            val participantGroup = coreParticipationRepository.getParticipantGroup(deploymentId)
            if (participantGroup == null) throw IllegalArgumentException("No participant group found for deployment $deploymentId")

            val participationHavingParticipantId = participantGroup.participations.find { it.participation.participantId == participantId}
            if (participationHavingParticipantId == null) throw IllegalArgumentException("No participation found for participant $participantId in deployment $deploymentId")

            val assignedPrimaryDeviceRoleNames = participationHavingParticipantId.assignedPrimaryDeviceRoleNames
            val dataStreamIds = dataStreamService.findDataStreamIdsByDeploymentIdAndDeviceRoleNames(deploymentId, assignedPrimaryDeviceRoleNames.toList()).toSet()
            val kek = dataStreamSequenceRepository.idkhowtonamethis(
                dataStreamIds.toList(),
                Timestamp(from * 1000),
                Timestamp(to * 1000),
                studyId.toString()
            )

            return dayKeyQuantityTriplesToBarChartDataDto(kek)
//            val recruitment = participantRepository.getRecruitment(studyId)
            println()
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

