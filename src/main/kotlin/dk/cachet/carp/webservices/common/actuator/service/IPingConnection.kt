package dk.cachet.carp.webservices.common.actuator.service

import org.springframework.boot.actuate.health.Status

/**
 * The Interface [IPingConnection].
 */
interface IPingConnection {
    /** The [statusHealth] interface. */
    fun statusHealth(): Status
}
