package dk.cachet.carp.webservices.common.converter

import dk.cachet.carp.common.application.UUID
import org.springframework.core.convert.converter.Converter

class UUIDConverter: Converter<String, UUID> {
    override fun convert(source: String): UUID {
        return UUID.parse(source)
    }
}