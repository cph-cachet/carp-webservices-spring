package dk.cachet.carp.webservices.deployment.eventbus

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

/**
 * The Class [DeploymentEventProcessorJob] listens to DeploymentService related events
 * and invokes handlers.
 * */
@Component
class DeploymentEventProcessorJob(private val coreEventBus: CoreEventBus) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * Listens to the deployment specific queue, deserializes the event and invokes the event handlers
     * registered for that event.
     */
    @RabbitListener(queues = ["\${rabbit.deployment.queue}"])
    fun process(message: Message) {
        LOGGER.info("New message received from the deployment event queue.")
        val event: DeploymentService.Event = JSON.decodeFromString(message.body.decodeToString())
        runBlocking {
            coreEventBus.invokeHandlers(event)
        }
    }
}
