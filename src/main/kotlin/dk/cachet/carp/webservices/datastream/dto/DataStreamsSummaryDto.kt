package dk.cachet.carp.webservices.datastream.dto

import kotlinx.datetime.Instant

data class DataStreamsSummaryDto(
    val data: List<DayTaskQuantityTriple>,
    val studyId: String,
    val deploymentId: String?,
    val participantId: String?,
    val scope: String,
    val type: String,
    val from: Instant,
    val to: Instant,
)
