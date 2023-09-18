package dk.cachet.carp.webservices.summary.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.AuthorizationService
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import dk.cachet.carp.webservices.summary.service.ISummaryService
import org.springframework.stereotype.Service

@Service
class SummaryAuthorizationService(
        private val summaryService: ISummaryService,
        studyService: CoreStudyRepository,
        deploymentRepository: CoreDeploymentRepository,
        participantGroupRepository: ParticipantGroupRepository,
        objectMapper: ObjectMapper,
        authenticationService: AuthenticationService
): AuthorizationService(studyService, deploymentRepository, participantGroupRepository, objectMapper, authenticationService)
{
    fun canCreateSummary(studyId: String): Boolean {
        val accountId = getAccountId()

        return isResearcherPartOfTheStudy(studyId, accountId)
    }

    fun canDownloadSummary(id: String): Boolean {
        val summary = summaryService.getSummaryById(id)

        val accountId = getAccountId()

        return isResearcherPartOfTheStudy(summary.studyId, accountId)
    }

    fun canListSummaries(studyId: String?): Boolean {
        if (studyId.isNullOrEmpty()) {
            return isAccountResearcher()
        }

        val accountId = getAccountId()

        return isResearcherPartOfTheStudy(studyId, accountId)
    }
}