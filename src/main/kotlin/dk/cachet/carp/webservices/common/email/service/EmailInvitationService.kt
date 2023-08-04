package dk.cachet.carp.webservices.common.email.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.webservices.common.email.domain.EmailType

interface EmailInvitationService
{
    fun inviteToStudy(email: String, deploymentId: UUID, invitation: StudyInvitation, emailType: EmailType)

    fun sendNotificationEmail(recipient: String?, subject: String?, message: String?)
}