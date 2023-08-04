package dk.cachet.carp.webservices.common.exception.async

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import java.lang.reflect.Method

/**
 * The Class [AsyncExceptionHandler].
 * The [AsyncExceptionHandler] handle the uncaught exceptions thrown from asynchronous methods.
 */
class AsyncExceptionHandler: AsyncUncaughtExceptionHandler
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The [handleUncaughtException] function for handling uncaught exceptions thrown from asynchronous methods.
     * @link [AsyncExceptionHandler]
     */
    override fun handleUncaughtException(ex: Throwable, method: Method, vararg params: Any?)
    {
        LOGGER.error("Exception message: ${ex.message}")
        LOGGER.error("Method name: ${method.name}")
        for (param in params)
            LOGGER.error("Parameter value: $param")
    }
}