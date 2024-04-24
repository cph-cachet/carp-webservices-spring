package dk.cachet.carp.webservices.summary.domain

import dk.cachet.carp.common.application.UUID

data class CreateSummaryRequest(
        val studyId: UUID,
        val deploymentIds: List<String>?
)