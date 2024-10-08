package dk.cachet.carp.webservices.common.email.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.email.domain.EmailRequest
import dk.cachet.carp.webservices.common.email.domain.EmailType
import dk.cachet.carp.webservices.common.email.listener.EmailSendingJob
import dk.cachet.carp.webservices.common.email.service.EmailInvitationService
import dk.cachet.carp.webservices.common.email.service.impl.javamail.EmailServiceImpl
import dk.cachet.carp.webservices.common.email.util.EmailTemplateUtil
import dk.cachet.carp.webservices.common.email.util.EmailValidatorUtil
import dk.cachet.carp.webservices.common.exception.responses.BadRequestException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EmailInvitationServiceImpl(
    private val emailValidator: EmailValidatorUtil,
    private val emailTemplate: EmailTemplateUtil,
    private val emailService: EmailServiceImpl,
    private val emailSendingJob: EmailSendingJob,
) : EmailInvitationService {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }
        override fun inviteToStudy(
        email: String,
        deploymentId: UUID,
        invitation: StudyInvitation,
        emailType: EmailType,
    ) {
        if (!isEmailAddressValid(email)) {
            LOGGER.warn("Invalid [email] address format, e-mail = $email. Email invitation was not sent!")
            return
        }

        val mailContent =
            emailTemplate.inviteAccount(
                invitation.name,
                invitation.description,
                emailType,
            )

        val request =
            EmailRequest(
                destinationEmail = email,
                subject = invitation.name,
                content = mailContent,
                deploymentId = deploymentId.stringRepresentation,
                id = UUID.randomUUID().stringRepresentation,
            )

        emailSendingJob.send(request)
    }

    override fun sendNotificationEmail(
        recipient: String?,
        subject: String?,
        message: String?,
    ) {
        if (!isEmailAddressValid(recipient)) {
            LOGGER.warn("Invalid [email] address format, e-mail = $recipient. Email invitation was not sent!")
            return
        }

        val mailContent = emailTemplate.sendNotificationEmail(message)
        emailService.invoke(recipient!!, subject!!, mailContent)
    }

    private fun isEmailAddressValid(emailAddress: String?): Boolean {
        return this.emailValidator.isValid(emailAddress);
    }
}
