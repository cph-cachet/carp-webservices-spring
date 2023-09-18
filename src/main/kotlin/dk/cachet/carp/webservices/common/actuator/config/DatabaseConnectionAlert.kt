package dk.cachet.carp.webservices.common.actuator.config

import dk.cachet.carp.webservices.common.actuator.service.IDatabaseConnection
import dk.cachet.carp.webservices.common.email.service.EmailInvitationService
import dk.cachet.carp.webservices.common.notification.domain.SlackChannel
import dk.cachet.carp.webservices.common.notification.service.INotificationService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

/**
 * The Configuration Class [DatabaseConnectionAlert].
 * The [DatabaseConnectionAlert] sends a notification to the System Admins if the database connection is down.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(value = ["database.connection.alert.enabled"], havingValue = "true")
class DatabaseConnectionAlert
(
    private var database: IDatabaseConnection,
    private val notificationService: INotificationService,
    private val emailNotificationService: EmailInvitationService,
    @Value("\${alert.admin-email}") private val alertEmail: String,
    @Value("\${alert.subject}") private val alertWarning: String
)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    @Scheduled(fixedDelay = 30 * 60 * 1000)
    fun checkRegularDatabaseConnection()
    {
        if ("DOWN" == database.statusHealth().code)
        {
            LOGGER.warn("Database connection is DOWN!")
            val databaseConnectionStatusNotification =
                "Database connection is ${database.statusHealth().code}!." +
                        " More details...: ${database.statusDetails()}"

            notificationService.sendRandomOrAlertNotificationToSlack(
                databaseConnectionStatusNotification,
                SlackChannel.HEARTBEAT
            )

            emailNotificationService.sendEmail(
                alertEmail,
                alertWarning,
                databaseConnectionStatusNotification
            )
        }
    }
}