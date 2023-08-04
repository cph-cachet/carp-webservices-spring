package dk.cachet.carp.webservices.summary.service

import dk.cachet.carp.webservices.summary.domain.Summary
import org.springframework.core.io.Resource

interface ISummaryService
{
    fun createSummaryForStudy(studyId: String, deploymentIds: List<String>? ): Summary

    fun downloadSummary(id: String): Resource

    fun listSummaries(accountId: String, studyId: String?): List<Summary>

    fun deleteSummaryById(id: String): String

    fun getSummaryById(id: String): Summary
}