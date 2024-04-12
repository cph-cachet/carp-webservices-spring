package dk.cachet.carp.webservices.summary.domain

import dk.cachet.carp.common.application.UUID
import java.time.Instant

data class SummaryLog(
        var studyId: UUID,

        val createdAt: Instant?,

        val infoLogs: MutableList<String> = mutableListOf(),

        val errorLogs: MutableList<String> = mutableListOf(),
)