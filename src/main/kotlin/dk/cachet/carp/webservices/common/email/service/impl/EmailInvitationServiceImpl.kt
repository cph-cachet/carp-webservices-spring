package dk.cachet.carp.webservices.common.email.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.webservices.account.service.AccountService
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
    private val validationMessages: MessageBase,
    private val emailSendingJob: EmailSendingJob
) : EmailInvitationService {

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun inviteToStudy(
        email: String,
        invitation: StudyInvitation,
        emailType: EmailType
    ) {
        val emailAddress = validateEmailAddress(email)

        val mailContent = emailTemplate.inviteAccount(
            invitation.name,
            invitation.description,
            emailType
        )

        val request = EmailRequest(
            address = emailAddress,
            subject = invitation.name,
            message = mailContent,
        )

        emailSendingJob.send(request)
    }

    override fun sendEmail(address: String, subject: String, message: String) {
        val emailAddress = validateEmailAddress(address)
        val mailContent = emailTemplate.sendNotificationEmail(message)
        emailService.invoke(emailAddress, subject, mailContent)
    }

    private fun validateEmailAddress(emailAddress: String?): String {
        if (!this.emailValidator.isValid(emailAddress)) {
            LOGGER.info("Invalid [email] address format, e-mail = $emailAddress")
            throw BadRequestException(
                validationMessages.get(
                    "email.invitation.service.format.invalid",
                    emailAddress.toString()
                )
            )
        }

        return emailAddress!!
    }
}