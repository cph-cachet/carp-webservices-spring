package dk.cachet.carp.webservices.common.email.service.impl.javamail

import dk.cachet.carp.webservices.common.email.domain.EmailSendResult
import dk.cachet.carp.webservices.common.email.util.EmailTemplateUtil
import dk.cachet.carp.webservices.common.exception.email.EmailException
import dk.cachet.carp.webservices.common.notification.domain.SlackChannel
import dk.cachet.carp.webservices.common.notification.service.INotificationService
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.apache.commons.io.IOUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.angus.mail.smtp.SMTPSendFailedException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.ClassPathResource
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

/**
 * The Class [EmailServiceImpl].
 * The [EmailServiceImpl] implements the functionality logic for email sending.
 */
@Service
class EmailServiceImpl(
        @Qualifier("mailConfig")
        private val mailSender: JavaMailSender,
        private val environment: Environment,
        private val notificationService: INotificationService
)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
        private const val DEFAULT_SUBJECT = "Welcome to the CARP Research Platform"
    }

    /**
     * The function [invoke] sends a new email with a QR code.
     *
     * @param destinationEmail The [destinationEmail] of the recipient.
     * @param subject The [subject] of the study.
     * @param content The [content] of the email body.
     * @return The status code of the response.
     */
    @Async
    fun invoke(destinationEmail: String, subject: String, content: String): CompletableFuture<Int>
    {
        val response = send(destinationEmail, subject, content)
        return CompletableFuture.completedFuture(response)
    }

    /**
     * The function [send] builds a [MimeMessage] enabling sending an email.
     *
     * @param recipientEmailAddress The [recipientEmailAddress] email address of the recipient.
     * @param studyNameAsSubject The [studyNameAsSubject] subject of the email.
     * @param mailContent The [mailContent] of the email.
     * @return The status code of the response.
     */
    @Throws(EmailException::class)
    fun send(recipientEmailAddress: String, studyNameAsSubject: String, mailContent: String): Int
    {
        try
        {
            val mimeMessage: MimeMessage = mailSender.createMimeMessage()
            mimeMessage.setContent(mailContent, "text/html; charset=utf-8")

            val mimeMessageHelper = MimeMessageHelper(
                    mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name())
            mimeMessageHelper.setTo(recipientEmailAddress)
            mimeMessageHelper.setText(mailContent, true)
            mimeMessageHelper.setSubject(ifNullOrEmpty(studyNameAsSubject))
            mimeMessageHelper.setFrom(environment.getProperty("spring.mail.from")!!)
            this.addInlineLogosToMessage(mimeMessageHelper)

            this.mailSender.send(mimeMessage)
        }
        catch (ex: MailSendException)
        {
            LOGGER.warn("Email message not sent.", ex)
            notificationService.sendRandomOrAlertNotificationToSlack("Email message not sent. Exception: $ex.", SlackChannel.SERVER_ERRORS)
            return EmailSendResult.FAILURE.status
        }
        catch (ex: SMTPSendFailedException)
        {
            LOGGER.warn("Email message not sent.", ex)
            notificationService.sendRandomOrAlertNotificationToSlack("Email message not sent. Exception: $ex.", SlackChannel.SERVER_ERRORS)
            return EmailSendResult.FAILURE.status
        }
        catch (ex: MessagingException)
        {
            LOGGER.warn("Email message not sent.", ex)
            notificationService.sendRandomOrAlertNotificationToSlack("Email message not sent. Exception: $ex.", SlackChannel.SERVER_ERRORS)
            return EmailSendResult.FAILURE.status
        }
        catch (ex: NullPointerException)
        {
            LOGGER.warn("No invitation email was found.", ex)
            notificationService.sendRandomOrAlertNotificationToSlack("No invitation email was found. Exception: $ex.", SlackChannel.SERVER_ERRORS)
            return EmailSendResult.FAILURE.status
        }
        catch (ex: Exception)
        {
            LOGGER.warn("Failed to send mail, mailAddress= $recipientEmailAddress", ex)
            notificationService.sendRandomOrAlertNotificationToSlack(
                    "Failed to send mail, mailAddress= $recipientEmailAddress. Exception: $ex.", SlackChannel.SERVER_ERRORS)
            return EmailSendResult.FAILURE.status
        }
        return EmailSendResult.SUCCESS.status
    }

    private fun ifNullOrEmpty(value:String): String
    {
        return if (value.isEmpty() or value.isBlank()) DEFAULT_SUBJECT else value
    }

    private fun getCachetLogo(): ByteArray
    {
        return IOUtils.toByteArray(ClassPathResource("image/cachet.png").inputStream)
    }

    private fun getCPHLogo(): ByteArray
    {
        return IOUtils.toByteArray(ClassPathResource("image/footer_CPH.png").inputStream)
    }

    private fun getDTULogo(): ByteArray
    {
        return IOUtils.toByteArray(ClassPathResource("image/footer_CPH.png").inputStream)
    }

    private fun getHLogo(): ByteArray
    {
        return IOUtils.toByteArray(ClassPathResource("image/footer_H.png").inputStream)
    }

    private fun getKULogo(): ByteArray
    {
        return IOUtils.toByteArray(ClassPathResource("image/footer_KU.png").inputStream)
    }

    private fun addInlineLogosToMessage(mimeMessageHelper: MimeMessageHelper)
    {
        mimeMessageHelper.addInline(EmailTemplateUtil.INLINE_CACHET_LOGO_ID, ByteArrayResource(getCachetLogo()), EmailTemplateUtil.PNG_CONTENT_TYPE)
        mimeMessageHelper.addInline(EmailTemplateUtil.INLINE_CPH_LOGO_ID, ByteArrayResource(getCPHLogo()), EmailTemplateUtil.PNG_CONTENT_TYPE)
        mimeMessageHelper.addInline(EmailTemplateUtil.INLINE_DTU_LOGO_ID, ByteArrayResource(getDTULogo()), EmailTemplateUtil.PNG_CONTENT_TYPE)
        mimeMessageHelper.addInline(EmailTemplateUtil.INLINE_H_LOGO_ID, ByteArrayResource(getHLogo()), EmailTemplateUtil.PNG_CONTENT_TYPE)
        mimeMessageHelper.addInline(EmailTemplateUtil.INLINE_KU_LOGO_ID, ByteArrayResource(getKULogo()), EmailTemplateUtil.PNG_CONTENT_TYPE)
    }
}