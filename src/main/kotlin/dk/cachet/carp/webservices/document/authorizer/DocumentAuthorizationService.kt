package dk.cachet.carp.webservices.document.authorizer

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.security.authentication.service.impl.AuthenticationServiceImpl
import dk.cachet.carp.webservices.security.authorization.AuthorizationService
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import org.springframework.stereotype.Service

@Service
class DocumentAuthorizationService(
        studyService: CoreStudyRepository,
        deploymentRepository: CoreDeploymentRepository,
        participantGroupRepository: ParticipantGroupRepository,
        objectMapper: ObjectMapper,
        authenticationService: AuthenticationServiceImpl
): AuthorizationService(studyService, deploymentRepository, participantGroupRepository, objectMapper, authenticationService)
{
    fun canViewAllDocuments(studyId: String): Boolean {
        if (isAccountSystemAdmin()) return true

        val accountId = getAccountId()

        return isResearcherPartOfTheStudy(studyId, accountId)
    }

    fun canViewDocument(studyId: String): Boolean {
        if (isAccountSystemAdmin()) return true

        return isParticipantOrResearcherOfTheStudy(studyId)
    }
}