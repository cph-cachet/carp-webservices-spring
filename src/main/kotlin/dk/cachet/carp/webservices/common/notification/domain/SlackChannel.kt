package dk.cachet.carp.webservices.common.notification.domain

/**
 * The enum [SlackChannel].
 * It represents the available slack channels that the application can send notifications to.
 */
enum class SlackChannel(val id: Int)
{
    CLIENT_ERRORS(0),
    SERVER_ERRORS(1),
    HEARTBEAT(2)
}