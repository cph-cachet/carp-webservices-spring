package dk.cachet.carp.webservices.common.eventbus

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.application.services.EventBus
import dk.cachet.carp.common.application.services.IntegrationEvent
import dk.cachet.carp.common.infrastructure.serialization.JSON
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.studies.application.StudyService
import kotlinx.serialization.encodeToString
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * The Class [CoreEventBus] implements the publisher/subscriber event bus declared in the core package.
 * */
@Component
class CoreEventBus(
    private val rabbitTemplate: RabbitTemplate,
    private val environment: Environment,
) : EventBus() {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun activateHandlers(
        subscriber: Any,
        handlers: List<Handler>,
    ) {
        // Nothing to do.
    }

    /**
     * Publish the specified [event] belonging to [publishingService].
     * It serializes the event and sends it to the application service specific queue.
     */
    override suspend fun <TService : ApplicationService<TService, TEvent>, TEvent : IntegrationEvent<TService>> publish(
        publishingService: KClass<TService>,
        event: TEvent,
    ) {
        if (StudyService::class == publishingService) {
            val castedEvent = event as StudyService.Event
            val serializedEvent = JSON.encodeToString(castedEvent)
            rabbitTemplate.convertAndSend(environment.getProperty("rabbit.study.queue")!!, serializedEvent)
            LOGGER.info("New StudyService event '${castedEvent::class.simpleName}' is published.")
        } else if (DeploymentService::class == publishingService) {
            val castedEvent = event as DeploymentService.Event
            val serializedEvent = JSON.encodeToString(castedEvent)
            rabbitTemplate.convertAndSend(environment.getProperty("rabbit.deployment.queue")!!, serializedEvent)
            LOGGER.info("New DeploymentService event '${castedEvent::class.simpleName}' is published.")
        }
    }

    suspend fun invokeHandlers(event: IntegrationEvent<*>) {
        // Find all active handlers listening to the published event type.
        val handlers =
            subscribers.values
                .filter { it.isActivated }
                .flatMap { it.eventHandlers.filter { handler -> handler.eventType.isInstance(event) } }

        // Publish.
        handlers.forEach {
            try {
                it.handler(event)
            } catch (ex: Exception) {
                LOGGER.info("An error is encountered while invoking handlers for event $event: ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}
