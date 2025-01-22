package dk.cachet.carp.webservices.account.serdes

import com.fasterxml.jackson.core.JsonGenerator
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test


class AccountIdentitySerializerTest {
    private val validationMessage: MessageBase = mockk<MessageBase>()
    private val jsonGenerator = mockk<JsonGenerator>()

    @Nested
    inner class Serialize {
        @Test
        fun `should serialize valid account identity`() {
            val accountIdentity = AccountIdentity.fromEmailAddress("test@dtu.dk")
            every { jsonGenerator.writeRawValue(any<String>()) } returns Unit
            every { validationMessage.get(any()) } returns "err"

            val sut = AccountIdentitySerializer(validationMessage);

            sut.serialize(accountIdentity, jsonGenerator, null)

            verify { jsonGenerator.writeRawValue("{\"__type\":\"dk.cachet.carp.common.application.users.EmailAccountIdentity\",\"emailAddress\":\"test@dtu.dk\"}") }
            verify(exactly = 0) { validationMessage.get(any()) }
        }

        @Test
        fun `should throw SerializationException if account identity is null`() {
            val accountIdentity = null
            every { validationMessage.get(any()) } returns "err"

            val sut = AccountIdentitySerializer(validationMessage)

            assertThrows<SerializationException> {
                sut.serialize(accountIdentity, jsonGenerator, null)
            }

            verify(exactly = 0) { jsonGenerator.writeRawValue(any<String>()) }
            verify(exactly = 1) { validationMessage.get("account.identity.request-object-null-value") }
        }

        @Test
        fun `should throw SerializationException if serialization fails`() {
            val accountIdentity = mockk<AccountIdentity>()
            every { accountIdentity.toString() } throws RuntimeException("Forced failure")

            every { validationMessage.get(any()) } returns "err"
            every { jsonGenerator.writeRawValue(any<String>()) } returns Unit

            val sut = AccountIdentitySerializer(validationMessage)

            assertThrows<SerializationException> {
                sut.serialize(accountIdentity, jsonGenerator, null)
            }

            verify(exactly = 1) { validationMessage.get("account.identity.request-serialization-not-valid") }
            verify(exactly = 0) { jsonGenerator.writeRawValue(any<String>()) }
        }
    }
}