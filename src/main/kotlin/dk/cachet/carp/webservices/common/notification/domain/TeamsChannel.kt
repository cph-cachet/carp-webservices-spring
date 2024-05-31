package dk.cachet.carp.webservices.common.notification.domain

/**
 * The enum [TeamsChannel].
 * It represents the available MS Teams channels that the application can send notifications to.
 */
enum class TeamsChannel(val id: Int) {
    CLIENT_ERRORS(0),
    SERVER_ERRORS(1),
    HEARTBEAT(2),
}
