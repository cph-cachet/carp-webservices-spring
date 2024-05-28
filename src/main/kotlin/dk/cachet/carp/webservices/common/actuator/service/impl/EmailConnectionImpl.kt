package dk.cachet.carp.webservices.common.actuator.service.impl

import dk.cachet.carp.webservices.common.actuator.service.IMailConnection
import dk.cachet.carp.webservices.common.exception.email.EmailException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.context.annotation.Lazy
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.messaging.MessagingException
import org.springframework.stereotype.Service

@Service
class EmailConnectionImpl(
    @Lazy private val mailSender: JavaMailSenderImpl,
) : IMailConnection {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The function [mailServerConnection] tests the smtp server connection.
     */
    override fun mailServerConnection(): String? {
        var status = "DOWN"
        try {
            if (testConnection()) {
                status = "UP"
                LOGGER.info("Mail server is available!")
            }
        } catch (ex: MessagingException) {
            LOGGER.warn("Mail server is not available!")
            throw EmailException("Mail server is not available. Exception: $ex")
        }
        return status
    }

    /**
     * The function [testConnection] to test the connection.
     */
    fun testConnection(): Boolean {
        try {
            mailSender.testConnection()
        } catch (ex: MessagingException) {
            LOGGER.error("SMTP server {}:{} is not responding", mailSender.host, mailSender.port, ex)
            return false
        }
        return true
    }
}
