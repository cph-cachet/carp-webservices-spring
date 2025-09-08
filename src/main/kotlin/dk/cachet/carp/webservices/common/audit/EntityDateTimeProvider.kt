package dk.cachet.carp.webservices.common.audit

import org.springframework.data.auditing.DateTimeProvider
import java.time.temporal.TemporalAccessor
import java.util.*
import java.time.Instant as JavaInstant

/**
 * Overrides the default behaviour: java.time.LocalDateTime -> java.time.Instant
 */

class EntityDateTimeProvider : DateTimeProvider {
    override fun getNow(): Optional<TemporalAccessor> {
        return Optional.of(JavaInstant.now())
    }
}
