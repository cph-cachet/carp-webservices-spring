package dk.cachet.carp.webservices.consent.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.consent.repository.ConsentDocumentRepository
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.AuthorizationService
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import org.springframework.stereotype.Service

@Service
class ConsentDocumentAuthorizationService(
        private val consentDocumentRepository: ConsentDocumentRepository,
        studyService: CoreStudyRepository,
        deploymentRepository: CoreDeploymentRepository,
        participantGroupRepository: ParticipantGroupRepository,
        objectMapper: ObjectMapper,
        authenticationService: AuthenticationService
): AuthorizationService(studyService, deploymentRepository, participantGroupRepository, objectMapper, authenticationService)
{
    fun canViewAllConsentDocuments(deploymentId: String): Boolean {
        if (isAccountSystemAdmin()) return true

        return isResearcherPartOfTheDeployment(deploymentId)
    }

    fun canViewConsentDocument(deploymentId: String, consentDocumentId: Int): Boolean {
        if (isAccountSystemAdmin()) return true

        return isResearcherPartOfTheDeployment(deploymentId) || isCreator(consentDocumentId)
    }

    fun canCreateConsentDocument(deploymentId: String): Boolean {
        if (isAccountSystemAdmin()) return true

        return isResearcherPartOfTheDeployment(deploymentId) || isParticipantPartOfTheDeployment(deploymentId)
    }

    private fun isCreator(dataPointId: Int): Boolean =
        consentDocumentRepository.findById(dataPointId)
            .map { consentDocument -> consentDocument.createdBy == authenticationService.getCurrentPrincipal().id }
            .orElse(false)
}