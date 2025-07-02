package dk.cachet.carp.webservices.security.authorization

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.extensions.toSnakeCase
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * A CAWS specific claim which can be granted to a user.
 *
 * It is assumed that these claims are in the form of user attributes in the identity provider, and they follow the
 * following naming scheme:
 * - Name: "Some Name"
 * - User Attribute: "someName"
 * - Token Claim: "some_name"
 */
sealed class Claim {
    abstract val value: Any

    companion object Factory {
        fun fromUserAttribute(pair: Pair<String, Any>): List<Claim>? {
            val claimKlass = Claim::class.sealedSubclasses.firstOrNull { userAttributeName(it) == pair.first }

            if (claimKlass == null) return null

            return (pair.second as? List<*>)?.mapNotNull { instantiate(claimKlass, it.toString()) }
        }

        // Each authority is mapped to "token_claim_{}" where {} is the value of the claim (ID).
        // you can see how the mapping takes place in the spring boot application properties file
        fun fromGrantedAuthority(authority: String): Claim? {
            val claimName = authority.substringBeforeLast('_')
            val value = authority.substringAfterLast('_')

            val claimKlass = Claim::class.sealedSubclasses.firstOrNull { tokenClaimName(it) == claimName }

            if (claimKlass == null) return null

            return instantiate(claimKlass, value)
        }

        private fun instantiate(
            klass: KClass<out Claim>,
            valueAsString: String,
        ): Claim? {
            // Try to parse the value as an integer or UUID.
            val value = valueAsString.toIntOrNull() ?: runCatching { UUID(valueAsString) }.getOrNull()

            if (value == null) return null

            return runCatching { klass.primaryConstructor?.call(value) }.getOrNull()
        }

        fun tokenClaimName(klass: KClass<*>) = klass.simpleName!!.toSnakeCase()

        fun userAttributeName(klass: KClass<*>) = klass.simpleName!!.replaceFirstChar { it.lowercase() }
    }

    data class ManageStudy(val studyId: UUID) : Claim() {
        override val value = studyId.toString()
    }

    data class LimitedManageStudy(val studyId: UUID) : Claim() {
        override val value = studyId.toString()
    }

    data class ProtocolOwner(val protocolId: UUID) : Claim() {
        override val value = protocolId.toString()
    }

    data class InDeployment(val deploymentId: UUID) : Claim() {
        override val value = deploymentId.toString()
    }

    data class CollectionOwner(val collectionId: Int) : Claim() {
        override val value = collectionId.toString()
    }

    data class FileOwner(val fileId: Int) : Claim() {
        override val value = fileId.toString()
    }

    /**
     * A virtual claim is a claim which is not directly present as a user attribute,
     * but can be derived from other claims.
     */
    sealed class VirtualClaim : Claim() {
        override val value get() =
            throw UnsupportedOperationException("The value of virtual claims cannot be requested.")
    }

    data class ManageDeployment(val deploymentId: UUID) : VirtualClaim()
}
