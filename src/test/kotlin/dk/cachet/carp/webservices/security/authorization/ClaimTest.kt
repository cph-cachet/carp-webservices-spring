package dk.cachet.carp.webservices.security.authorization

import dk.cachet.carp.common.application.UUID
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class ClaimTest {

    @Nested
    inner class FromUserAttribute
    {
        @Test
        fun `should return null if the attribute is not recognized`() {
            val attribute = "unknown" to "value"
            val claims = Claim.fromUserAttribute( attribute )
            assertNull( claims )
        }

        @Test
        fun `should return null if the attribute value is not a list`() {
            val attribute = "manageDeployment" to "value"
            val claims = Claim.fromUserAttribute( attribute )
            assertNull( claims )
        }

        @Test
        fun `should create claims`() {
            val uuid1 = UUID.randomUUID()
            val uuid2 = UUID.randomUUID()
            val attribute = "manageDeployment" to listOf( uuid1.toString(), uuid2.toString() )
            val claims = Claim.fromUserAttribute( attribute )

            assertEquals( claims, listOf( Claim.ManageDeployment( uuid1 ), Claim.ManageDeployment( uuid2 ) ) )
        }

        @Test
        fun `should ignore unrecognized values`() {
            val attribute = "manageDeployment" to listOf( UUID.randomUUID(), "randomValue" )
            val claims = Claim.fromUserAttribute( attribute )
            assertEquals( claims?.size, 1)
        }
    }

    @Nested
    inner class FromGrantedAuthority
    {
        @Test
        fun `should return null if the authority is not recognized`() {
            val authority = "unknown_claim_123"
            val claim = Claim.fromGrantedAuthority( authority )
            assertNull( claim )
        }

        @Test
        fun `should create claim`() {
            val uuid = UUID.randomUUID()
            val authority = "manage_deployment_$uuid"
            val claim = Claim.fromGrantedAuthority( authority )

            assertEquals( claim, Claim.ManageDeployment( uuid ) )
        }
    }
}