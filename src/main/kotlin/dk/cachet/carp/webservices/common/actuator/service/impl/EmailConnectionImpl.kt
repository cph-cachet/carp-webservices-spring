package dk.cachet.carp.webservices.common.actuator.service.impl

import dk.cachet.carp.webservices.common.actuator.service.IMailConnection
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
        if (testConnection()) {
            status = "UP"
            LOGGER.info("Mail server is available!")
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
