package dk.cachet.carp.webservices.common.configuration.internationalisation

import org.springframework.context.support.MessageSourceAccessor

/**
 * The Abstract Class [AMessageBase].
 * The [AMessageBase] enables easy access to messages from a MessageSource, providing various overloaded getMessage methods.
 */
abstract class AMessageBase {
    // [MessageSourceAccessor] enables easy access to messages from a MessageSource, providing various overloaded getMessage methods
    protected var accessor: MessageSourceAccessor? = null

    /**
     * The function [get] provides the message for the given code and the default Locale.
     *
     * @param code The [code] of the message.
     * @returns The [MessageSourceAccessor] message with the given code and the default Locale.
     */
    fun get(code: String): String {
        return accessor!!.getMessage(code)
    }

    /**
     * The function [get] provides the message for the given code, argument for the message, and the default Locale.
     *
     * @param code The [code] of the message.
     * @returns The [MessageSourceAccessor] message with the given code and the default Locale.
     */
    fun get(
        code: String,
        vararg params: Any,
    ): String {
        return accessor!!.getMessage(code, params)
    }
}
