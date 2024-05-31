package dk.cachet.carp.webservices.common.configuration.internationalisation.service

import dk.cachet.carp.webservices.common.configuration.internationalisation.AMessageBase
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.support.MessageSourceAccessor
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.stereotype.Service

/**
 * The Class [MessageBase].
 * The [MessageBase] implements the [AMessageBase] interface for resolving messages, which supports the parameterization
 * and internationalization of the messages.
 */
@Service
class MessageBase : AMessageBase() {
    init
    {
        // Initialize the [messageSource]
        accessor = MessageSourceAccessor(messageSource())
    }

    /**
     * The function [messageSource] implements the [MessageSource] interface for resolving messages,
     * @return The [ResourceBundleMessageSource] with the given Map that is keyed with the message code.
     */
    @Bean
    final fun messageSource(): MessageSource {
        val resourceBundleMessageSource = ResourceBundleMessageSource()
        resourceBundleMessageSource.setBasename("messages/messages")
        resourceBundleMessageSource.setDefaultEncoding("UTF-8")
        return resourceBundleMessageSource
    }
}
