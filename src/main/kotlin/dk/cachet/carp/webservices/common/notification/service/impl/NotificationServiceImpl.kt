package dk.cachet.carp.webservices.common.notification.service.impl

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
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.io.IOException
import java.net.ConnectException

/**
 * The Class [NotificationServiceImpl].
 * The [NotificationServiceImpl] enables exception notifications [CarpErrorResponse] in Slack channel.
 */
@Service
class NotificationServiceImpl
(
    private val environment: EnvironmentUtil,
    private val validationMessages: MessageBase,
    @Value("\${slack.channel.name}") private val slackChannel: String,
    @Value("\${slack.channel.server}") private val slackServerChannel: String,
    @Value("\${slack.channel.heartbeat}") private val slackHeartbeatChannel: String,
    @Value("\${slack.webhook}") private val slackWebHook: String,
    @Value("\${teams.webhook}") private val teamsWebHook: String
): INotificationService
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
        private const val NEW_LINE = "\n\n"
    }

    /**
     * The [sendExceptionNotificationToSlack] function sends a notification message with the given message.
     * @param notification The [notification] containing the message to send.
     * @param channelToSendTo The value of the Slack channel the message needs to be sent to.
     */
    override fun sendRandomOrAlertNotificationToSlack(notification: String, channelToSendTo: SlackChannel)
    {
        val messageBuilder = StringBuilder()
        messageBuilder.append(NEW_LINE)
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
            messageBuilder.append("----- **Notification** ----- ")
            messageBuilder.append(NEW_LINE)
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
     * The [processException] function processes the exception and sends the message to the Slack channel.
     * @param message The message to send on Slack channel.
     * @throws IOException when the webhook cannot be reached.
     */
    private fun processException(message: String, slackChannelToSend: String?)
    {
        val payload = mapOf(
            "text" to message,
            "title" to "CARP-Webservices &#x1f381;",
            "themeColor" to "0078D7")

        try
        {
            val webClient = WebClient.create()
            webClient.post()
                    .uri(teamsWebHook)
                    .body(BodyInserters.fromValue(payload))
                    .retrieve()
                    .bodyToMono(String::class.java)
                    .subscribe(
                        { response -> LOGGER.info("Teams response -> {}", response) },
                        { error -> LOGGER.error("Error sending message to Teams: ${error.message}") }
                    )

        }
        catch (ex: WebClientResponseException)
        {
            LOGGER.error("Unexpected Error! WebHook: $ex")
            throw BadRequestException(validationMessages.get("notification.slack.exception", ex.message.toString()))
        }
        catch (ex: ConnectException)
        {
            LOGGER.error("Connection Error! WebHook: $ex")
            throw BadRequestException(validationMessages.get("notification.slack.exception", ex.message.toString()))
        }
        catch (ex: Exception)
        {
            LOGGER.error("Unexpected Error! WebHook: $ex")
            throw BadRequestException(validationMessages.get("notification.slack.exception", ex.message.toString()))
        }
    }
}