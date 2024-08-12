package dk.cachet.carp.webservices.common.actuator.service

import org.springframework.boot.actuate.health.Status

/**
 * The [IRabbitConnection] Interface.
 */
interface IRabbitConnection {
    /** The [statusHealth] interface. */
    fun statusHealth(): Status

    /** The [statusDetails] interface. */
    fun statusDetails(): MutableMap<String, Any>?
}
