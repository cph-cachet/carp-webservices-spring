package dk.cachet.carp.webservices.common.notification.service

import dk.cachet.carp.webservices.common.exception.advices.CarpErrorResponse
import dk.cachet.carp.webservices.common.notification.domain.SlackChannel

/** The Interface [INotificationService]. */
interface INotificationService
{
    /** The [sendExceptionNotificationToSlack] interface to notify exceptions notifications in slack channel. */
    fun sendExceptionNotificationToSlack(errorResponse: CarpErrorResponse)

    /** The [sendRandomOrAlertNotificationToSlack] interface to notify specific alerts or notifications in slack channel. */
    fun sendRandomOrAlertNotificationToSlack(notification: String, channelToSendTo: SlackChannel)
}