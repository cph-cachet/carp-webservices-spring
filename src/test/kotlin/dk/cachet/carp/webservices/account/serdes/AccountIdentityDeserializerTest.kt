package dk.cachet.carp.webservices.account.serdes

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.serialization.SerializationException
import io.mockk.*
import org.junit.jupiter.api.Nested
import kotlin.test.*

class AccountIdentityDeserializerTest {
    private val validationMessage: MessageBase = mockk<MessageBase>()
    private val jsonParser: JsonParser = mockk<JsonParser>()

    @Nested
    inner class Deserialize {
        @Test
        fun `should deserialize valid JSON`() {
            val validJsonString =
                "{\"__type\":\"dk.cachet.carp.common.application.users.EmailAccountIdentity\"," +
                    "\"emailAddress\":\"test@dtu.dk\"}"
            every { jsonParser.codec.readTree<TreeNode>(jsonParser).toString() } returns validJsonString
            val sut = AccountIdentityDeserializer(validationMessage)

            val result = sut.deserialize(jsonParser, null)

            val expectedAccountIdentity = AccountIdentity.fromEmailAddress("test@dtu.dk")
            assertEquals(expectedAccountIdentity, result)
        }

        @Test
        fun `should throw SerializationException if json string is empty`() {
            val emptyJsonString = ""
            every { jsonParser.codec.readTree<TreeNode>(jsonParser).toString() } returns emptyJsonString
            every { validationMessage.get(any()) } returns "err"
            val sut = AccountIdentityDeserializer(validationMessage)

            assertFailsWith<SerializationException> {
                sut.deserialize(jsonParser, null)
            }

            verify(exactly = 1) { validationMessage.get("account.identity.request-blank-or-empty") }
        }

        @Test
        fun `should throw SerializationException if failed to retrieve json string`() {
            every { jsonParser.codec.readTree<TreeNode>(jsonParser).toString() } throws
                RuntimeException("Forced failure")
            every { validationMessage.get(any()) } returns "err"
            val sut = AccountIdentityDeserializer(validationMessage)

            assertFailsWith<SerializationException> {
                sut.deserialize(jsonParser, null)
            }

            verify(exactly = 1) { validationMessage.get("account.identity.request-bad-format") }
        }

        @Test
        fun `should throw SerializationException if failed to `() {
            val invalidJsonString =
                "{\"__type\":\"dk.cachet!.carp.common.application.users.EmailAccountIdentity\"," +
                    "\"emailAddress\":\"test@dtu.dk\"}"

            every { jsonParser.codec.readTree<TreeNode>(jsonParser).toString() } returns invalidJsonString
            every { validationMessage.get(any()) } returns "err"
            val sut = AccountIdentityDeserializer(validationMessage)

            assertFailsWith<SerializationException> {
                sut.deserialize(jsonParser, null)
            }

            verify(exactly = 1) { validationMessage.get("account.identity.request-deserialization-not-valid") }
        }
    }
}
