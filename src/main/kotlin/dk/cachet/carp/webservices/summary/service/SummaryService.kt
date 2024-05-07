package dk.cachet.carp.webservices.summary.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.summary.domain.Summary
import org.springframework.core.io.Resource

interface SummaryService
{
    fun createSummaryForStudy(studyId: UUID, deploymentIds: List<String>? ): Summary

    fun downloadSummary(id: UUID): Resource

    fun listSummaries(accountId: UUID, studyId: UUID?): List<Summary>

    fun deleteSummaryById(id: UUID): UUID

    fun getSummaryById(id: UUID): Summary
}