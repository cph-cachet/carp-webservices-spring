package dk.cachet.carp.webservices.common.converter

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class ISO8601ToKotlinInstantConverterTest {
    private val converter = ISO8601ToKotlinInstantConverter()

    @Test
    fun `convert valid ISO8601 string to Instant`() {
        val iso8601String = "2023-10-01T12:00:00Z"
        val instant = converter.convert(iso8601String)
        assertNotNull(instant)
        assertEquals("2023-10-01T12:00:00Z", instant.toString())
    }

    @Test
    fun `convert invalid ISO8601 string throws exception`() {
        val invalidIso8601String = "invalid-date"
        assertThrows(IllegalArgumentException::class.java) {
            converter.convert(invalidIso8601String)
        }
    }
}
