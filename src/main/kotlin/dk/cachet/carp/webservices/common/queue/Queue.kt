package dk.cachet.carp.webservices.common.queue

import org.springframework.amqp.core.*
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

/**
 * The Class [Queue].
 * It contains the AMPQ Queue, Exchange and Binding configurations.
 * */
@Configuration
class Queue(environment: Environment) {
    /** DataPoint queues */
    val DATA_POINT_PROCESSING_QUEUE = environment.getProperty("rabbit.data-point.processing.queue")
    val DATA_POINT_DIRECT_EXCHANGE = environment.getProperty("rabbit.data-point.processing.direct-ex")
    val DATA_POINT_PROCESSING_DLQ = environment.getProperty("rabbit.data-point.processing.dlq")
    val DATA_POINT_PROCESSING_DLX = environment.getProperty("rabbit.data-point.processing.dlx")
    val DATA_POINT_PROCESSING_PLQ = environment.getProperty("rabbit.data-point.processing.plq")
    val DATA_POINT_PROCESSING_PLX = environment.getProperty("rabbit.data-point.processing.plx")

    /** Email queues */
    val EMAIL_SENDING_QUEUE = environment.getProperty("rabbit.email.sending.queue")
    val EMAIL_SENDING_DIRECT_EXCHANGE = environment.getProperty("rabbit.email.sending.direct-ex")
    val EMAIL_SENDING_DLQ = environment.getProperty("rabbit.email.sending.dlq")
    val EMAIL_SENDING_DLX = environment.getProperty("rabbit.email.sending.dlx")
    val EMAIL_SENDING_PLQ = environment.getProperty("rabbit.email.sending.plq")
    val EMAIL_SENDING_PLX = environment.getProperty("rabbit.email.sending.plx")

    /** 3rd party queues */
    val THIRD_PARTY_QUEUE = environment.getProperty("rabbit.third-party.processing.queue")
    val THIRD_PARTY_DIRECT_EXCHANGE = environment.getProperty("rabbit.third-party.processing.direct-ex")
    val THIRD_PARTY_DLQ = environment.getProperty("rabbit.third-party.processing.dlq")
    val THIRD_PARTY_DLX = environment.getProperty("rabbit.third-party.processing.dlx")
    val THIRD_PARTY_PLQ = environment.getProperty("rabbit.third-party.processing.plq")
    val THIRD_PARTY_PLX = environment.getProperty("rabbit.third-party.processing.plx")

    /** Study queues */
    val STUDIES_QUEUE = environment.getProperty("rabbit.study.queue")
    val STUDIES_DIRECT_EXCHANGE = environment.getProperty("rabbit.study.direct-ex")
    val STUDIES_DLQ = environment.getProperty("rabbit.study.dlq")
    val STUDIES_DLX = environment.getProperty("rabbit.study.dlx")

    /** Deployment queues */
    val DEPLOYMENTS_QUEUE = environment.getProperty("rabbit.deployment.queue")
    val DEPLOYMENTS_DIRECT_EXCHANGE = environment.getProperty("rabbit.deployment.direct-ex")
    val DEPLOYMENTS_DLQ = environment.getProperty("rabbit.deployment.dlq")
    val DEPLOYMENTS_DLX = environment.getProperty("rabbit.deployment.dlx")

    /**
     * Exchange declarations
     */
    @Bean
    fun dataPointProcessingDirectExchange(): DirectExchange {
        return DirectExchange(DATA_POINT_DIRECT_EXCHANGE)
    }

    @Bean
    fun dataPointProcessingDLX(): FanoutExchange {
        return FanoutExchange(DATA_POINT_PROCESSING_DLX)
    }

    @Bean
    fun dataPointProcessingPLX(): FanoutExchange {
        return FanoutExchange(DATA_POINT_PROCESSING_PLX)
    }

    @Bean
    fun emailSendingDirectExchange(): DirectExchange {
        return DirectExchange(EMAIL_SENDING_DIRECT_EXCHANGE)
    }

    @Bean
    fun emailSendingDLX(): FanoutExchange {
        return FanoutExchange(EMAIL_SENDING_DLX)
    }

    @Bean
    fun emailSendingPLX(): FanoutExchange {
        return FanoutExchange(EMAIL_SENDING_PLX)
    }

    @Bean
    fun thirdPartyDirectExchange(): DirectExchange {
        return DirectExchange(THIRD_PARTY_DIRECT_EXCHANGE)
    }

    @Bean
    fun thirdPartyDLX(): FanoutExchange {
        return FanoutExchange(THIRD_PARTY_DLX)
    }

    @Bean
    fun thirdPartyPLX(): FanoutExchange {
        return FanoutExchange(THIRD_PARTY_PLX)
    }

    @Bean
    fun studyDirectExchange(): DirectExchange {
        return DirectExchange(STUDIES_DIRECT_EXCHANGE)
    }

    @Bean
    fun studyDLX(): FanoutExchange {
        return FanoutExchange(STUDIES_DLX)
    }

    @Bean
    fun deploymentDirectExchange(): DirectExchange {
        return DirectExchange(DEPLOYMENTS_DIRECT_EXCHANGE)
    }

    @Bean
    fun deploymentDLX(): FanoutExchange {
        return FanoutExchange(DEPLOYMENTS_DLX)
    }

    /**
     * Queue declarations
     */
    @Bean
    fun dataPointProcessingQueue(): Queue {
        return QueueBuilder
            .durable(DATA_POINT_PROCESSING_QUEUE)
            .withArgument("x-dead-letter-exchange", DATA_POINT_PROCESSING_DLX)
            .build()
    }

    @Bean
    fun dataPointProcessingDLQ(): Queue {
        return QueueBuilder.durable(DATA_POINT_PROCESSING_DLQ).build()
    }

    @Bean
    fun dataPointProcessingPLQ(): Queue {
        return QueueBuilder.durable(DATA_POINT_PROCESSING_PLQ).build()
    }

    @Bean
    fun emailSendingQueue(): Queue {
        return QueueBuilder
            .durable(EMAIL_SENDING_QUEUE)
            .withArgument("x-dead-letter-exchange", EMAIL_SENDING_DLX)
            .build()
    }

    @Bean
    fun emailSendingDLQ(): Queue {
        return QueueBuilder.durable(EMAIL_SENDING_DLQ).build()
    }

    @Bean
    fun emailSendingPLQ(): Queue {
        return QueueBuilder.durable(EMAIL_SENDING_PLQ).build()
    }

    @Bean
    fun thirdPartyQueue(): Queue {
        return QueueBuilder
            .durable(THIRD_PARTY_QUEUE)
            .withArgument("x-dead-letter-exchange", THIRD_PARTY_DLX)
            .build()
    }

    @Bean
    fun thirdPartyDLQ(): Queue {
        return QueueBuilder.durable(THIRD_PARTY_DLQ).build()
    }

    @Bean
    fun thirdPartyPLQ(): Queue {
        return QueueBuilder.durable(THIRD_PARTY_PLQ).build()
    }

    @Bean
    fun studyQueue(): Queue {
        return QueueBuilder
            .durable(STUDIES_QUEUE)
            .withArgument("x-dead-letter-exchange", STUDIES_DLX)
            .build()
    }

    @Bean
    fun studyDLQ(): Queue {
        return QueueBuilder.durable(STUDIES_DLQ).build()
    }

    @Bean
    fun deploymentQueue(): Queue {
        return QueueBuilder
            .durable(DEPLOYMENTS_QUEUE)
            .withArgument("x-dead-letter-exchange", DEPLOYMENTS_DLX)
            .build()
    }

    @Bean
    fun deploymentDLQ(): Queue {
        return QueueBuilder.durable(DEPLOYMENTS_DLQ).build()
    }

    /**
     * Binding declarations
     */
    @Bean
    fun dataPointProcessingBinding(): Binding {
        return BindingBuilder.bind(
            dataPointProcessingQueue(),
        ).to(dataPointProcessingDirectExchange()).with(DATA_POINT_PROCESSING_QUEUE)
    }

    @Bean
    fun dataPointProcessingQueueDeadLetterBinding(): Binding {
        return BindingBuilder.bind(dataPointProcessingDLQ()).to(dataPointProcessingDLX())
    }

    @Bean
    fun dataPointProcessingParkingLotBinding(): Binding {
        return BindingBuilder.bind(dataPointProcessingPLQ()).to(dataPointProcessingPLX())
    }

    @Bean
    fun emailSendingBinding(): Binding {
        return BindingBuilder.bind(emailSendingQueue()).to(emailSendingDirectExchange()).with(EMAIL_SENDING_QUEUE)
    }

    @Bean
    fun emailSendingQueueDeadLetterBinding(): Binding {
        return BindingBuilder.bind(emailSendingDLQ()).to(emailSendingDLX())
    }

    @Bean
    fun emailSendingParkingLotBinding(): Binding {
        return BindingBuilder.bind(emailSendingPLQ()).to(emailSendingPLX())
    }

    @Bean
    fun thirdPartyBinding(): Binding {
        return BindingBuilder.bind(thirdPartyQueue()).to(thirdPartyDirectExchange()).with(THIRD_PARTY_QUEUE)
    }

    @Bean
    fun thirdPartyQueueDeadLetterBinding(): Binding {
        return BindingBuilder.bind(thirdPartyDLQ()).to(thirdPartyDLX())
    }

    @Bean
    fun thirdPartyParkingLotBinding(): Binding {
        return BindingBuilder.bind(thirdPartyPLQ()).to(thirdPartyPLX())
    }

    @Bean
    fun studyBinding(): Binding {
        return BindingBuilder.bind(studyQueue()).to(studyDirectExchange()).with(STUDIES_QUEUE)
    }

    @Bean
    fun studyQueueDeadLetterBinding(): Binding {
        return BindingBuilder.bind(studyDLQ()).to(studyDLX())
    }

    @Bean
    fun deploymentBinding(): Binding {
        return BindingBuilder.bind(deploymentQueue()).to(deploymentDirectExchange()).with(DEPLOYMENTS_QUEUE)
    }

    @Bean
    fun deploymentQueueDeadLetterBinding(): Binding {
        return BindingBuilder.bind(deploymentDLQ()).to(deploymentDLX())
    }
}
