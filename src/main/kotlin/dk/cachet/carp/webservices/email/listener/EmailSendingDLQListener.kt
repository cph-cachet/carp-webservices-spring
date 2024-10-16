package dk.cachet.carp.webservices.email.listener

import dk.cachet.carp.webservices.common.queue.DLQListener
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * The Dead-Letter-Queue listener for email sending.
 * It requeues the messages when the messages did not surpass the retry threshold
 * or redirects them to the Parking-Lot-Queue.
 */
@Component
class EmailSendingDLQListener(
    private val rabbitTemplate: RabbitTemplate,
    private val environment: Environment,
) : DLQListener() {
    @RabbitListener(queues = ["\${rabbit.email.sending.dlq}"])
    fun receive(failedMessage: Message) {
        if (!assertAndIncrementRetriesHeader(failedMessage)) {
            rabbitTemplate.send(
                environment.getProperty("rabbit.email.sending.plx")!!,
                failedMessage.messageProperties.receivedRoutingKey,
                failedMessage,
            )
            return
        }

        rabbitTemplate.send(
            environment.getProperty("rabbit.email.sending.direct-ex")!!,
            failedMessage.messageProperties.receivedRoutingKey,
            failedMessage,
        )
    }
}
