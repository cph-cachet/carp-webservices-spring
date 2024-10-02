package dk.cachet.carp.webservices.common.configuration.internationalisation.service

import dk.cachet.carp.webservices.common.configuration.internationalisation.AMessageBase
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.stereotype.Service

/**
 * The Class [MessageBase].
 * The [MessageBase] implements the [AMessageBase] interface for resolving messages, which supports the parameterization
 * and internationalization of the messages.
 */
@Service
class MessageBase(messageSource: MessageSource) : AMessageBase() {
    init {
        // Initialize the [accessor] with the injected [messageSource]
        accessor = MessageSourceAccessor(messageSource)
    }
}

@Configuration
class MessageConfig {
    /**
     * The function [messageSource] implements the [MessageSource] interface for resolving messages,
     * @return The [ResourceBundleMessageSource] with the given Map that is keyed with the message code.
     */
    @Bean
    fun messageSource(): MessageSource {
        val resourceBundleMessageSource = ResourceBundleMessageSource()
        resourceBundleMessageSource.setBasename("messages/messages")
        resourceBundleMessageSource.setDefaultEncoding("UTF-8")
        return resourceBundleMessageSource
    }
}
