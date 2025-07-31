package dk.cachet.carp.webservices.common.converter

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class UUIDConverterTest {
    private val converter = UUIDConverter()

    @Test
    fun `convert valid UUID string to UUID`() {
        val uuidString = "123e4567-e89b-12d3-a456-426614174000"
        val uuid = converter.convert(uuidString)
        assertNotNull(uuid)
        assertEquals(uuidString, uuid.toString())
    }

    @Test
    fun `convert invalid UUID string throws exception`() {
        val invalidUuidString = "invalid-uuid"
        assertThrows(IllegalArgumentException::class.java) {
            converter.convert(invalidUuidString)
        }
    }
}
