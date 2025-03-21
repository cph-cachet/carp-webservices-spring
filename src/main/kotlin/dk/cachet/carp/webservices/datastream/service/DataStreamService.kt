package dk.cachet.carp.webservices.datastream.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.webservices.dataVisualization.dto.DayKeyQuantityTriple
import kotlinx.datetime.Instant

interface DataStreamService {
    val core: DataStreamServiceDecorator

    fun getLatestUpdatedAt(deploymentId: UUID): Instant?

    fun findDataStreamIdsByDeploymentId(deploymentId: UUID): List<Int>

    fun idkhowtonamethisForSurveys(
        dataStreamIds: List<Int>,
        from: Instant,
        to: Instant,
        studyId: String
    ): List<DayKeyQuantityTriple>
}
