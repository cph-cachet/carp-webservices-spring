package dk.cachet.carp.webservices.summary.domain

import java.time.Instant

data class SummaryLog
(
        var studyId: String,

        val createdAt: Instant?,

        val infoLogs: MutableList<String> = mutableListOf(),

        val errorLogs: MutableList<String> = mutableListOf(),
)