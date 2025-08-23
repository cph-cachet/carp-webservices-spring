package dk.cachet.carp.webservices.common.actuator.config

import dk.cachet.carp.webservices.common.actuator.service.*
import dk.cachet.carp.webservices.common.notification.domain.TeamsChannel
import dk.cachet.carp.webservices.common.notification.service.INotificationService
import io.mockk.*
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals

class RegularStatusAlertTest {
    private val ping: IPingConnection = mockk()
    private val rabbit: IRabbitConnection = mockk()
    private val database: IDatabaseConnection = mockk()
    private val disk: IDiskSpaceStatus = mockk()
    private val mail: IMailConnection = mockk()
    private val notificationService: INotificationService = mockk()

    @Nested
    inner class StatusCheck {
        @Test
        fun `statusCheck should send notification 1`() {
            every { ping.statusHealth().toString() } returns "UP"
            every { database.statusHealth().toString() } returns "UP"
            every { database.statusDetails().toString() } returns "UP"
            every { disk.statusHealth().toString() } returns "UP"
            every { disk.statusDetails().toString() } returns "UP"
            every { rabbit.statusHealth().toString() } returns "UP"
            every { rabbit.statusDetails().toString() } returns "UP"
            every { mail.mailServerConnection().toString() } returns "UP"
            every { notificationService.sendAlertOrGeneralNotification(any(), any()) } just Runs

            val sut = RegularStatusAlert(ping, rabbit, database, disk, mail, notificationService)
            sut.statusCheck()

            verify {
                notificationService.sendAlertOrGeneralNotification(
                    withArg { message ->
                        val checkmarkCount = message.count { it == '✅' }
                        assertEquals(5, checkmarkCount, "Should contain 5 checkmark emojis")
                    },
                    TeamsChannel.HEARTBEAT,
                )
            }
        }

        @Test
        fun `statusCheck should send notification 2`() {
            every { ping.statusHealth().toString() } returns "DOWN"
            every { database.statusHealth().toString() } returns "DOWN"
            every { database.statusDetails().toString() } returns "DOWN"
            every { disk.statusHealth().toString() } returns "DOWN"
            every { disk.statusDetails().toString() } returns "DOWN"
            every { rabbit.statusHealth().toString() } returns "DOWN"
            every { rabbit.statusDetails().toString() } returns "DOWN"
            every { mail.mailServerConnection().toString() } returns "DOWN"
            every { notificationService.sendAlertOrGeneralNotification(any(), any()) } just Runs

            val sut = RegularStatusAlert(ping, rabbit, database, disk, mail, notificationService)
            sut.statusCheck()

            verify {
                notificationService.sendAlertOrGeneralNotification(
                    withArg { message ->
                        val checkmarkCount = message.count { it == '❌' }
                        assertEquals(5, checkmarkCount, "Should contain 5 cross emojis")
                    },
                    TeamsChannel.HEARTBEAT,
                )
            }
        }
    }
}
