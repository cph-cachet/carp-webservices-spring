package dk.cachet.carp.webservices.file.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.file.repository.FileRepository
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.AuthorizationService
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import org.springframework.stereotype.Service

@Service
class FileAuthorizationService(
    private val fileRepository: FileRepository,
    studyService: CoreStudyRepository,
    deploymentRepository: CoreDeploymentRepository,
    participantGroupRepository: ParticipantGroupRepository,
    objectMapper: ObjectMapper,
    authenticationService: AuthenticationService,
) : AuthorizationService(
    studyService,
    deploymentRepository,
    participantGroupRepository,
    objectMapper,
    authenticationService
) {
    fun canViewAllFiles(studyId: String): Boolean {
        if (isAccountSystemAdmin()) return true

        val accountId = getAccountId()

        return isResearcherPartOfTheStudy(studyId, accountId)
    }

    fun canViewFile(studyId: String, fileId: Int): Boolean {
        if (isAccountSystemAdmin()) return true

        val accountId = getAccountId()

        return isResearcherPartOfTheStudy(studyId, accountId) || isCreator(fileId)
    }

    fun canCreateFile(studyId: String): Boolean {
        if (isAccountSystemAdmin()) return true

        val accountId = getAccountId()

        return isResearcherPartOfTheStudy(studyId, accountId) || isParticipantPartOfStudy(studyId)
    }

    fun isCreator(fileId: Int): Boolean =
        fileRepository.findById(fileId)
            .map { file ->
                file.createdBy == getAccountId()
            }
            .orElse(false)

}