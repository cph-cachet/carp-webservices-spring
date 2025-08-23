package dk.cachet.carp.webservices.common.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.time.Instant
import java.time.Instant as JavaInstant
import kotlinx.datetime.Instant as KotlinInstant

@Converter
class InstantConverter : AttributeConverter<KotlinInstant, JavaInstant> {
    override fun convertToDatabaseColumn(p0: kotlinx.datetime.Instant?): Instant? {
        if (p0 == null) return null

        return p0.toJavaInstant()
    }

    override fun convertToEntityAttribute(p0: Instant?): kotlinx.datetime.Instant? {
        if (p0 == null) return null

        return p0.toKotlinInstant()
    }
}
