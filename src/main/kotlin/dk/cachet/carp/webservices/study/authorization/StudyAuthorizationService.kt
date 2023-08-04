package dk.cachet.carp.webservices.study.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.AuthorizationService
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import org.springframework.stereotype.Service

@Service
class StudyAuthorizationService(
        val studyService: CoreStudyRepository,
        deploymentRepository: CoreDeploymentRepository,
        participantGroupRepository: ParticipantGroupRepository,
        objectMapper: ObjectMapper,
        authenticationService: AuthenticationService,
): AuthorizationService(studyService, deploymentRepository, participantGroupRepository, objectMapper, authenticationService)
{
    fun canCreateStudy(): Boolean
    {
        if (isAccountSystemAdmin()) return true

        return isAccountResearcher()
    }

    fun canAccessStudy(studyId: String): Boolean
    {
        if (isAccountSystemAdmin()) return true

        return isResearcherPartOfTheStudy(studyId)
    }
}