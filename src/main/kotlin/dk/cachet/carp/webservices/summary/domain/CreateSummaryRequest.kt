package dk.cachet.carp.webservices.summary.domain

data class CreateSummaryRequest(
        val studyId: String,
        val deploymentIds: List<String>?
)