package dk.cachet.carp.webservices.email.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.webservices.email.domain.EmailType
import dk.cachet.carp.webservices.email.dto.GenericEmailRequestDto

interface EmailService {
    fun inviteToStudy(
        email: String,
        deploymentId: UUID,
        invitation: StudyInvitation,
        emailType: EmailType,
    )

    fun sendNotificationEmail(
        recipient: String?,
        subject: String?,
        message: String?,
    )

    fun sendGenericEmail(
        requestDto: GenericEmailRequestDto
    )
}
