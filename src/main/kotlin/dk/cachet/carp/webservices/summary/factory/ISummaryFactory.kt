package dk.cachet.carp.webservices.summary.factory

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.summary.domain.Summary

interface ISummaryFactory {

    fun create(studyId: UUID, deploymentIds: List<String>?): Summary
}