package dk.cachet.carp.webservices.common.actuator.config

import dk.cachet.carp.webservices.common.actuator.service.*
import dk.cachet.carp.webservices.common.notification.domain.SlackChannel
import dk.cachet.carp.webservices.common.notification.service.INotificationService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

/**
 * The Configuration Class [RegularStatusAlert].
 */
@Profile("!local & !staging & !testing & !development")
@Configuration
@EnableScheduling
class RegularStatusAlert(
    private val ping: IPingConnection,
    private val rabbit: IRabbitConnection,
    private val database: IDatabaseConnection,
    private val disk: IDiskSpaceStatus,
    private val mail: IMailConnection,
    private val notificationService: INotificationService
)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
        private const val NEW_LINE = "\n"
    }

    @Scheduled(cron = "0 30 8,12,15,17 * * *", zone = "Europe/Copenhagen")
    fun statusCheck()
    {
        val messageBuilder = StringBuilder()
        messageBuilder.append("== CARP Status ==")
        messageBuilder.append(NEW_LINE)

        if (ping.statusHealth().toString() == "UP") { messageBuilder.append("WS-Status: ${ping.statusHealth()} :white_check_mark: ") }
        else { messageBuilder.append("WS-Status: ${ping.statusHealth()} :no_entry: ") }
        messageBuilder.append(NEW_LINE)

        if (ping.statusHealth().toString() == "UP") { messageBuilder.append("Db-Status: ${database.statusHealth()} :white_check_mark: | ${database.statusDetails()}") }
        else { messageBuilder.append("DB-Status: ${database.statusHealth()} :no_entry: | ${database.statusDetails()}") }
        messageBuilder.append(NEW_LINE)

        if (disk.statusHealth().toString() == "UP") { messageBuilder.append("Disk-Status: ${disk.statusHealth()} :white_check_mark: | ${disk.statusDetails()} ") }
        else { messageBuilder.append("Disk-Status: ${disk.statusHealth()} :no_entry: | ${disk.statusDetails()} ") }
        messageBuilder.append(NEW_LINE)

        if (rabbit.statusHealth().toString() == "UP") {  messageBuilder.append("Rabbit-Status: ${rabbit.statusHealth()} :white_check_mark: | ${rabbit.statusDetails()}") }
        else {  messageBuilder.append("Rabbit-Status: ${rabbit.statusHealth()} :no_entry: | ${rabbit.statusDetails()}") }
        messageBuilder.append(NEW_LINE)

        if (mail.mailServerConnection().toString() == "UP") { messageBuilder.append("Mail-Server-Status: ${mail.mailServerConnection()} :white_check_mark: ") }
        else { messageBuilder.append("Mail-Server-Status: ${mail.mailServerConnection()} :no_entry: ") }

        LOGGER.info("Regular status check: $messageBuilder")
        notificationService.sendRandomOrAlertNotificationToSlack(messageBuilder.toString(), SlackChannel.HEARTBEAT)
    }
}