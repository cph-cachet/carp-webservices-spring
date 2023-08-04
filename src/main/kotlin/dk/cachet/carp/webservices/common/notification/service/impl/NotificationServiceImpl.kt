package dk.cachet.carp.webservices.common.notification.service.impl

import com.github.seratch.jslack.Slack
import com.github.seratch.jslack.api.webhook.Payload
import com.github.seratch.jslack.api.webhook.WebhookResponse
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.environment.EnvironmentProfile
import dk.cachet.carp.webservices.common.environment.EnvironmentUtil
import dk.cachet.carp.webservices.common.exception.advices.CarpErrorResponse
import dk.cachet.carp.webservices.common.exception.responses.BadRequestException
import dk.cachet.carp.webservices.common.notification.domain.SlackChannel
import dk.cachet.carp.webservices.common.notification.service.INotificationService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.IOException

/**
 * The Class [NotificationServiceImpl].
 * The [NotificationServiceImpl] enables exception notifications [CarpErrorResponse] in slack channel.
 */
@Service
class NotificationServiceImpl
(
    private val environment: EnvironmentUtil,
    private val validationMessages: MessageBase,
    @Value("\${slack.channel.name}") private val slackChannel: String,
    @Value("\${slack.channel.server}") private val slackServerChannel: String,
    @Value("\${slack.channel.heartbeat}") private val slackHeartbeatChannel: String,
    @Value("\${slack.webhook}") private val slackWebHook: String
): INotificationService
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
        private const val NEW_LINE = "\n"
    }

    /**
     * The [sendExceptionNotificationToSlack] function sends a notification message with the given message.
     * @param notification The [notification] containing the message to send.
     * @param channelToSendTo The value of the slack channel the message needs to be sent to.
     */
    override fun sendRandomOrAlertNotificationToSlack(notification: String, channelToSendTo: SlackChannel)
    {
        val messageBuilder = StringBuilder()
        messageBuilder.append(notification)
        messageBuilder.append(NEW_LINE)
        messageBuilder.append(NEW_LINE)
        messageBuilder.append("Environment: ${environment.profile}")
        when (channelToSendTo)
        {
            SlackChannel.CLIENT_ERRORS -> processException(messageBuilder.toString(), slackChannel)
            SlackChannel.SERVER_ERRORS -> processException(messageBuilder.toString(), slackServerChannel)
            SlackChannel.HEARTBEAT -> processException(messageBuilder.toString(), slackHeartbeatChannel)
        }
    }

    /**
     * The [sendExceptionNotificationToSlack] function sends a notification message with the given message.
     * @param errorResponse The errorResponse includes the error code, error message, and the error response.
     */
    override fun sendExceptionNotificationToSlack(errorResponse: CarpErrorResponse)
    {
        if(environment.profile == EnvironmentProfile.PRODUCTION)
        {
            val messageBuilder = StringBuilder()
            messageBuilder.append("Exception Code: ${errorResponse.statusCode}")
            messageBuilder.append(NEW_LINE)
            messageBuilder.append("Exception: ${errorResponse.exception}")
            messageBuilder.append(NEW_LINE)
            messageBuilder.append("Message: ${errorResponse.message}")
            messageBuilder.append(NEW_LINE)
            messageBuilder.append("Path: ${errorResponse.path}")
            messageBuilder.append(NEW_LINE)
            messageBuilder.append("Environment: ${environment.profile}")
            messageBuilder.append(NEW_LINE)

            if (errorResponse.statusCode in 400..499)
            {
                processException(messageBuilder.toString(), slackChannel)
            }
            else if (errorResponse.statusCode in 500..599)
            {
                processException(messageBuilder.toString(), slackServerChannel)
            }
        }
    }

    /**
     * The [processException] function processes the exception and sends the message to the slack channel.
     * @param message The message to send on slack channel.
     * @throws IOException when the webhook cannot be reached.
     */
    private fun processException(message: String, slackChannelToSend: String?)
    {
        val payload: Payload = Payload.builder()
                .channel(slackChannelToSend)
                .username("CARP-Webservices")
                .iconEmoji(":rocket:")
                .text(message)
                .build()
        try
        {
            val webhookResponse: WebhookResponse = Slack.getInstance().send(slackWebHook, payload)
            LOGGER.info("Slack response code -> {}, body -> {}", webhookResponse.code, "body -> " + webhookResponse.body)
        }
        catch (ex: IOException)
        {
            LOGGER.error("Unexpected Error! WebHook: $ex")
            throw BadRequestException(validationMessages.get("notification.slack.exception", ex.message.toString()))
        }
    }
}