package dk.cachet.carp.webservices.common.actuator.service.impl

import org.springframework.mail.javamail.JavaMailSenderImpl
import io.mockk.*
import org.junit.jupiter.api.Nested
import org.springframework.messaging.MessagingException
import kotlin.test.Test
import kotlin.test.assertEquals


class EmailConnectionImplTest {
    private val mailSender: JavaMailSenderImpl = mockk()

    @Nested
    inner class MailServerConnection {
        @Test
        fun `should return UP when mail server is available`() {
            every { mailSender.testConnection() } just Runs
            val sut = EmailConnectionImpl(mailSender)

            val emailConnection = sut.mailServerConnection()
            assertEquals("UP", emailConnection)
        }

        @Test
        fun `should return DOWN when mail server is unavailable`() {
            every { mailSender.testConnection() } throws MessagingException("Connection failed")
            every { mailSender.host } returns "smtp.example.com"
            every { mailSender.port } returns 587

            val sut = EmailConnectionImpl(mailSender)

            val emailConnection = sut.mailServerConnection()
            assertEquals("DOWN", emailConnection)
        }
    }
}