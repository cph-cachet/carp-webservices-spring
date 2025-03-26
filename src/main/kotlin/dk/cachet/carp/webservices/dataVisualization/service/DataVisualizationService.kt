package dk.cachet.carp.webservices.dataVisualization.service;

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.domain.users.ParticipantRepository
import dk.cachet.carp.webservices.dataVisualization.dto.BarChartDataDto
import dk.cachet.carp.webservices.dataVisualization.dto.DayKeyQuantityTriple
import dk.cachet.carp.webservices.dataVisualization.dto.TimeSeriesEntryDto
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.deployment.repository.CoreParticipationRepository
import kotlinx.datetime.Instant
import org.springframework.stereotype.Service

@Service
class DataVisualizationService(
    val dataStreamService: DataStreamService,
    val participantRepository: ParticipantRepository,
    val coreParticipationRepository: CoreParticipationRepository
) {
    companion object {
        private val validScopes = setOf("study", "deployment", "participant")
    }
    suspend fun getBarChartData(
        studyId: UUID,
        deploymentId: UUID?,
        participantId: UUID?,
        scope: String,
        type: String,
        from: Instant,
        to: Instant
    ): BarChartDataDto {
        val dataStreamIds = getDataStreamIds(scope, studyId, deploymentId, participantId)

        val dayKeyQuantityTriples = dataStreamService.getDayKeyQuantityListByDataStreamIdsAndOtherParameters(
            dataStreamIds, from, to, studyId.toString(), type
        )

        return dayKeyQuantityTriplesToBarChartDataDto(dayKeyQuantityTriples)
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

    private suspend fun getDataStreamIds(
        scope: String, studyId: UUID, deploymentId: UUID?, participantId: UUID?
    ): List<Int> {
        val dataStreamIds: List<Int>
        if (scope == "deployment") {
            if (deploymentId == null) throw IllegalArgumentException("Deployment ID must be provided when scope is 'deployment'.")
            dataStreamIds = getDataStreamIdsForDeployment(deploymentId)
        } else if (scope == "study") {
            dataStreamIds = getDataStreamIdsForStudy(studyId)
        } else if (scope == "participant") {
            if (participantId == null) throw IllegalArgumentException("Participant ID must be provided when scope is 'participant'.")
            if (deploymentId == null) throw IllegalArgumentException("Deployment ID must be provided when scope is 'participant'.")
            dataStreamIds = getDataStreamIdsForParticipant(participantId, deploymentId)
        } else {
            throw IllegalArgumentException("Invalid scope: $scope. Allowed values: $validScopes")
        }

        return dataStreamIds
    }

    private fun getDataStreamIdsForDeployment(deploymentId: UUID): List<Int> {
        return dataStreamService.findDataStreamIdsByDeploymentId(deploymentId)
    }

    private suspend fun getDataStreamIdsForStudy(studyId: UUID): List<Int> {
        val deploymentIds = participantRepository.getRecruitment(studyId)?.participantGroups?.keys?.toSet()
            ?: throw IllegalArgumentException("No deployment ids found for study $studyId");
        val dataStreamIds =
            deploymentIds.flatMap { dataStreamService.findDataStreamIdsByDeploymentId(it) }.toSet().toList()

        return dataStreamIds
    }

    private suspend fun getDataStreamIdsForParticipant(participantId: UUID, deploymentId: UUID): List<Int> {
        val participantGroup = coreParticipationRepository.getParticipantGroup(deploymentId)
            ?: throw IllegalArgumentException("No participant group found for deployment $deploymentId")

        val participationHavingParticipantId =
            participantGroup.participations.find { it.participation.participantId == participantId }
        if (participationHavingParticipantId == null)
            throw IllegalArgumentException("No participation found for participant $participantId in deployment $deploymentId")

        val assignedPrimaryDeviceRoleNames = participationHavingParticipantId.assignedPrimaryDeviceRoleNames
        val dataStreamIds = dataStreamService.findDataStreamIdsByDeploymentIdAndDeviceRoleNames(
            deploymentId, assignedPrimaryDeviceRoleNames.toList()
        ).toSet().toList()

        return dataStreamIds
    }
}

