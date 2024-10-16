package dk.cachet.carp.webservices.email.listener

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.email.EmailException
import dk.cachet.carp.webservices.email.domain.EmailRequest
import dk.cachet.carp.webservices.email.domain.EmailSendResult
import dk.cachet.carp.webservices.email.service.impl.javamail.EmailSenderImpl
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * The configuration class for sending and receiving email messages.
 */
@Component
class EmailSendingJob(
    private val rabbitTemplate: RabbitTemplate,
    private val emailSend: EmailSenderImpl,
    private val validationMessages: MessageBase,
    private val environment: Environment,
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * Email message producer.
     * It sends the request to the message queue for processing
     *
     * @param emailRequest The request to be sent to the queue.
     */
    fun send(emailRequest: EmailRequest) {
        val encodedEmailRequest = JSON.encodeToString(emailRequest)
        rabbitTemplate.convertAndSend(environment.getProperty("rabbit.email.sending.queue")!!, encodedEmailRequest)
    }

    /**
     * Email message consumer.
     * It sends the [emailRequest] using the [EmailSenderImpl] and waits
     * for the response.
     *
     * @param emailRequest The request to be sent to the queue.
     * @throws EmailException When there is a fail signal received.
     */
    @RabbitListener(queues = ["\${rabbit.email.sending.queue}"])
    fun receive(message: Message) {
        val emailRequest: EmailRequest = JSON.decodeFromString(message.body.decodeToString())
        val responseFuture =
            emailSend.invoke(
                emailRequest.destinationEmail,
                emailRequest.subject,
                emailRequest.content,
                emailRequest.cc,
            )

        val response = responseFuture.get()
        if (response == EmailSendResult.FAILURE.status) {
            LOGGER.info("Email sending to email: ${emailRequest.destinationEmail} failed.")
            throw EmailException(validationMessages.get("email.sending.job.failed", emailRequest.destinationEmail))
        }
    }
}
