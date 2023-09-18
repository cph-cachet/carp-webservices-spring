package dk.cachet.carp.webservices.common.email.service

import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.webservices.common.email.domain.EmailType

interface EmailInvitationService
{
    fun inviteToStudy(email: String, invitation: StudyInvitation, emailType: EmailType)

    fun sendEmail(address: String, subject: String, message: String)
}