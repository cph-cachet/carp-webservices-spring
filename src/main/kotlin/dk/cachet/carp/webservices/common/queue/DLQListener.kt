package dk.cachet.carp.webservices.common.queue

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.amqp.core.Message

/**
 * A common base class for Dead-Letter-Queue listeners.
 */
abstract class DLQListener
{
    companion object
    {
        val LOGGER: Logger = LogManager.getLogger()
        const val HEADER_X_RETRIES_COUNT = "x-retries-count"
        const val MAX_RETRIES_COUNT = 2
    }

    /**
     * It is used to assert the HEADER_X_RETRIES_COUNT header in the queue message.
     *
     * @param failedMessage The message to be examined.
     * @return A boolean value based on whether the message is still available for requeues.
     */
    protected fun assertAndIncrementRetriesHeader(failedMessage: Message): Boolean
    {
        var retriesCount = failedMessage.messageProperties.headers[HEADER_X_RETRIES_COUNT] as Int?
        if (retriesCount == null) retriesCount = 1

        if (retriesCount > MAX_RETRIES_COUNT)
        {
            return false
        }

        failedMessage.messageProperties.headers[HEADER_X_RETRIES_COUNT] = ++retriesCount
        return true
    }
}