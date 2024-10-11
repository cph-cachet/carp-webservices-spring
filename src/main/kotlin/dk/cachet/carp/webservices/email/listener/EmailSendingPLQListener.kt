package dk.cachet.carp.webservices.email.listener

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.webservices.email.domain.EmailRequest
import dk.cachet.carp.webservices.common.notification.domain.TeamsChannel
import dk.cachet.carp.webservices.common.notification.service.INotificationService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * The Parking-Lot-Queue listener for email sending.
 * It sends a notification on a failed email sending attempt.
 *
 */
@Component
class EmailSendingPLQListener {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    @Autowired
    lateinit var notificationService: INotificationService

    @RabbitListener(queues = ["\${rabbit.email.sending.plq}"])
    fun receive(message: Message) {
        val emailRequest: EmailRequest = JSON.decodeFromString(message.body.decodeToString())
        LOGGER.info(
            "New Email message for ${emailRequest.destinationEmail} " +
                "with id ${emailRequest.id} has arrived in the Email Parking Lot.",
        )
        notificationService.sendAlertOrGeneralNotification(
            "New Email message for ${emailRequest.destinationEmail} " +
                "with id ${emailRequest.id} has arrived in the Parking Lot.",
            TeamsChannel.SERVER_ERRORS,
        )
    }
}
