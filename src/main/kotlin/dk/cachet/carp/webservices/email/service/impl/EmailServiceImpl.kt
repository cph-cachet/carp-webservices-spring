package dk.cachet.carp.webservices.email.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.webservices.email.domain.EmailRequest
import dk.cachet.carp.webservices.email.domain.EmailType
import dk.cachet.carp.webservices.email.listener.EmailSendingJob
import dk.cachet.carp.webservices.email.service.EmailService
import dk.cachet.carp.webservices.email.service.impl.javamail.EmailSenderImpl
import dk.cachet.carp.webservices.email.dto.GenericEmailRequestDto
import dk.cachet.carp.webservices.email.util.EmailTemplateUtil
import dk.cachet.carp.webservices.email.util.EmailValidatorUtil
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EmailServiceImpl(
    private val emailValidator: EmailValidatorUtil,
    private val emailTemplate: EmailTemplateUtil,
    private val emailService: EmailSenderImpl,
    private val emailSendingJob: EmailSendingJob,
) : EmailService {
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

    override fun sendGenericEmail(requestDto: GenericEmailRequestDto) {
        val request =
            EmailRequest(
                destinationEmail = requestDto.recipient,
                subject = requestDto.subject,
                content = "Sent by ${requestDto.sender}",
                deploymentId = null,
                id = UUID.randomUUID().stringRepresentation,
            )

        emailSendingJob.send(request)
    }

    private fun isEmailAddressValid(emailAddress: String?): Boolean {
        return this.emailValidator.isValid(emailAddress)
    }
}
