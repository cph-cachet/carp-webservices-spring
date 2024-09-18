package dk.cachet.carp.webservices.study.eventbus

import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

/**
 * The Class [StudyEventProcessorJob] listens to StudyService related events
 * and invokes handlers.
 * */
@Component
class StudyEventProcessorJob(private val coreEventBus: CoreEventBus) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * Listens to the study specific queue, deserializes the event and invokes the event handlers
     * registered for that event.
     */
    @RabbitListener(queues = ["\${rabbit.study.queue}"])
    fun process(message: Message) {
        LOGGER.info("New message received from the study event queue.")
        val event: StudyService.Event = JSON.decodeFromString(message.body.decodeToString())
        runBlocking {
            coreEventBus.invokeHandlers(event)
        }
    }
}
