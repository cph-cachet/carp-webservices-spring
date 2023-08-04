package dk.cachet.carp.webservices.collection.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.collection.repository.CollectionRepository
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.AuthorizationService
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CollectionAuthorizationService(
        private val collectionRepository: CollectionRepository,
        studyService: CoreStudyRepository,
        deploymentRepository: CoreDeploymentRepository,
        participantGroupRepository: ParticipantGroupRepository,
        objectMapper: ObjectMapper,
        authenticationService: AuthenticationService
): AuthorizationService(studyService, deploymentRepository, participantGroupRepository, objectMapper, authenticationService)
{
    fun canViewCollection(studyId: String): Boolean
    {
        if (isAccountSystemAdmin()) return true

        return isParticipantOrResearcherOfTheStudy(studyId)
    }

    fun canModifyCollection(studyId: String, collectionId: Int): Boolean
    {
        if (isAccountSystemAdmin()) return true

        return isCreatorById(collectionId) || isResearcherPartOfTheStudy(studyId)
    }

    private fun isCreatorById(collectionId: Int): Boolean =
        collectionRepository.findById(collectionId)
            .map { collection -> collection.createdBy == authenticationService.getCurrentPrincipal().id }
            .orElse(false)
}