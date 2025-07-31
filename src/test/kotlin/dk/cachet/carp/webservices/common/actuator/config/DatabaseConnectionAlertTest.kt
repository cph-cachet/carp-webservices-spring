package dk.cachet.carp.webservices.common.actuator.config

import dk.cachet.carp.webservices.common.actuator.service.IDatabaseConnection
import dk.cachet.carp.webservices.common.notification.domain.TeamsChannel
import dk.cachet.carp.webservices.common.notification.service.INotificationService
import dk.cachet.carp.webservices.email.service.EmailService
import io.mockk.*
import org.junit.jupiter.api.Nested
import kotlin.test.*

class DatabaseConnectionAlertTest {
    private val database: IDatabaseConnection = mockk()
    private val notificationService: INotificationService = mockk()
    private val emailNotificationService: EmailService = mockk()
    private val alertWarning = "alert"
    private val alertEmail = "test@test.com"

    @Nested
    inner class CheckRegularDatabaseConnection {
        @Test
        fun `nothing should happen if database ok`() {
            every { database.statusHealth().code } returns "UP"

            val sut =
                DatabaseConnectionAlert(
                    database,
                    notificationService,
                    emailNotificationService,
                    alertEmail,
                    alertWarning,
                )

            sut.checkRegularDatabaseConnection()

            verify(exactly = 0) { notificationService.sendAlertOrGeneralNotification(any(), any()) }
            verify(exactly = 0) { emailNotificationService.sendNotificationEmail(any(), any(), any()) }
        }

        @Test
        fun `should take action if database down`() {
            every { database.statusHealth().code } returns "DOWN"
            every { database.statusDetails() } returns mutableMapOf()
            every { notificationService.sendAlertOrGeneralNotification(any(), any()) } just Runs
            every { emailNotificationService.sendNotificationEmail(any(), any(), any()) } just Runs

            val sut =
                DatabaseConnectionAlert(
                    database,
                    notificationService,
                    emailNotificationService,
                    alertEmail,
                    alertWarning,
                )

            sut.checkRegularDatabaseConnection()

            verify(exactly = 1) {
                notificationService.sendAlertOrGeneralNotification(any(), TeamsChannel.HEARTBEAT)
            }

            verify(exactly = 1) {
                emailNotificationService.sendNotificationEmail(alertEmail, alertWarning, any())
            }
        }
    }
}
