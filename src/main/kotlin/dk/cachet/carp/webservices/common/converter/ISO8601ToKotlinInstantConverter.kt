package dk.cachet.carp.webservices.common.converter

import kotlinx.datetime.Instant
import org.springframework.core.convert.converter.Converter

class ISO8601ToKotlinInstantConverter : Converter<String, Instant> {
    override fun convert(source: String): Instant {
        return Instant.parse(source)
    }
}
