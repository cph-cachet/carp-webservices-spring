package dk.cachet.carp.webservices.common.notification.service

import dk.cachet.carp.webservices.common.exception.advices.CarpErrorResponse
import dk.cachet.carp.webservices.common.notification.domain.TeamsChannel

/** The Interface [INotificationService]. */
interface INotificationService
{
    /** The [sendExceptionNotification] interface to notify exceptions notifications in slack channel. */
    fun sendExceptionNotification(errorResponse: CarpErrorResponse)

    /** The [sendAlertOrGeneralNotification] interface to notify specific alerts or notifications in slack channel. */
    fun sendAlertOrGeneralNotification(notification: String, channelToSendTo: TeamsChannel)
}