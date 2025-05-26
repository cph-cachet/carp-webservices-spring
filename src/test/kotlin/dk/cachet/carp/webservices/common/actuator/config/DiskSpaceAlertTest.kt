package dk.cachet.carp.webservices.common.actuator.config

import dk.cachet.carp.webservices.common.actuator.service.IDiskSpaceStatus
import dk.cachet.carp.webservices.common.notification.service.INotificationService
import dk.cachet.carp.webservices.email.service.EmailService
import io.mockk.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DiskSpaceAlertTest {
    private val diskSpace: IDiskSpaceStatus = mockk()
    private val notificationService: INotificationService = mockk()
    private val emailNotificationService: EmailService = mockk()
    private val alertEmail = "test@mail.dk"
    private val alertWarning = "alert"

    @Nested
    inner class CheckRegularDiskSpace {

        @Test
        fun `should not do anything if disk not UP`() {
            every { diskSpace.statusHealth().code } returns "DOWN"

            val mockStatusDetails = mutableMapOf<String, Any>(
                "free" to "1000",
                "total" to "5000",
            )

            every { diskSpace.statusDetails() } returns mockStatusDetails

            val sut = DiskSpaceAlert(
                diskSpace, notificationService, emailNotificationService, alertEmail, alertWarning
            )

            sut.checkRegularDiskSpace()

            verify(exactly = 0) { notificationService.sendAlertOrGeneralNotification(any(), any()) }
            verify(exactly = 0) { emailNotificationService.sendNotificationEmail(any(), any(), any()) }
        }

        @Test
        fun `should not do anything if diskSpaceHealth has no info`() {
            every { diskSpace.statusHealth().code } returns "UP"

            val mockStatusDetails = mutableMapOf<String, Any>(
            )

            every { diskSpace.statusDetails() } returns mockStatusDetails

            val sut = DiskSpaceAlert(
                diskSpace, notificationService, emailNotificationService, alertEmail, alertWarning
            )

            sut.checkRegularDiskSpace()

            verify(exactly = 0) { notificationService.sendAlertOrGeneralNotification(any(), any()) }
            verify(exactly = 0) { emailNotificationService.sendNotificationEmail(any(), any(), any()) }
        }

        @Test
        fun `should not take action if usable percentage is less than limit`() {
            every { diskSpace.statusHealth().code } returns "UP"

            val mockStatusDetails = mutableMapOf<String, Any>(
                "free" to "1000",
                "total" to "5000",
            )

            every { diskSpace.statusDetails() } returns mockStatusDetails

            val sut = DiskSpaceAlert(
                diskSpace, notificationService, emailNotificationService, alertEmail, alertWarning
            )

            sut.checkRegularDiskSpace()

            verify(exactly = 0) { notificationService.sendAlertOrGeneralNotification(any(), any()) }
            verify(exactly = 0) { emailNotificationService.sendNotificationEmail(any(), any(), any()) }
        }

        @Test
        fun `should take action if usable percentage is more than limit`() {
            every { diskSpace.statusHealth().code } returns "UP"
            every { notificationService.sendAlertOrGeneralNotification(any(), any()) } just Runs
            every { emailNotificationService.sendNotificationEmail(any(), any(), any()) } just Runs

            val mockStatusDetails = mutableMapOf<String, Any>(
                "free" to "1000",
                "total" to "15000",
            )

            every { diskSpace.statusDetails() } returns mockStatusDetails

            val sut = DiskSpaceAlert(
                diskSpace, notificationService, emailNotificationService, alertEmail, alertWarning
            )

            sut.checkRegularDiskSpace()

            verify(exactly = 1) { notificationService.sendAlertOrGeneralNotification(any(), any()) }
            verify(exactly = 1) { emailNotificationService.sendNotificationEmail(any(), any(), any()) }
        }
    }
}