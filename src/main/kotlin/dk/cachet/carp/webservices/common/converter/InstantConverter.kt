package dk.cachet.carp.webservices.common.converter;

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import java.time.Instant
import kotlinx.datetime.Instant as KotlinInstant
import java.time.Instant as JavaInstant


@Converter()
class InstantConverter : AttributeConverter<KotlinInstant, JavaInstant> {
    override fun convertToDatabaseColumn(p0: kotlinx.datetime.Instant?): Instant {
        return p0?.toJavaInstant() ?: JavaInstant.EPOCH
    }

    override fun convertToEntityAttribute(p0: Instant?): kotlinx.datetime.Instant {
        return p0?.toKotlinInstant() ?: KotlinInstant.fromEpochMilliseconds(0)
    }
}
