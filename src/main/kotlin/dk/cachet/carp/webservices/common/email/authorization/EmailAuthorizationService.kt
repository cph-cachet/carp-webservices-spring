package dk.cachet.carp.webservices.common.email.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.webservices.common.email.domain.NotificationRequest
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.AuthorizationService
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import org.springframework.stereotype.Service

@Service
class EmailAuthorizationService(
    studyService: CoreStudyRepository,
    deploymentRepository: CoreDeploymentRepository,
    participantGroupRepository: ParticipantGroupRepository,
    objectMapper: ObjectMapper,
    authenticationService: AuthenticationService
): AuthorizationService( studyService, deploymentRepository, participantGroupRepository, objectMapper, authenticationService ) {

    fun canAccountSendMail(notificationRequest: NotificationRequest): Boolean {
        val accountId = getAccountId()

        return isResearcherPartOfTheDeployment(notificationRequest.deploymentId, accountId) &&
                isParticipantPartOfTheDeployment(notificationRequest.deploymentId, notificationRequest.recipientAccountId)
    }
}