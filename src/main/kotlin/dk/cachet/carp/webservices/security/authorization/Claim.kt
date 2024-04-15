package dk.cachet.carp.webservices.security.authorization

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.extensions.toSnakeCase
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

sealed class Claim
{
    abstract val value: Any

    companion object
    {
        fun tokenClaimName( klass: KClass<*> ) = klass.simpleName!!.toSnakeCase()

        fun fromTokenClaimObject( pair: Pair<String, Any> ): List<Claim>?
        {
            val claimKlass =
                Claim::class.sealedSubclasses.firstOrNull { tokenClaimName( it ) == pair.first }
                ?: return null

            return (pair.second as? List<*>)
                ?.mapNotNull {
                    claimKlass.primaryConstructor?.call(
                        runCatching { UUID(it.toString()) }.getOrNull()
                    )
                }
        }
    }

    fun userAttributeName() = this::class.simpleName!!.replaceFirstChar { it.lowercase() }

    data class ManageDeployment( val deploymentId: UUID ) : Claim()
    {
        override val value = deploymentId.toString()
    }

    data class ManageStudy( val studyId: UUID ) : Claim()
    {
        override val value = studyId.toString()
    }

    data class ProtocolOwner( val ownerId: UUID ) : Claim()
    {
        override val value = ownerId.toString()
    }

    data class InDeployment( val deploymentId: UUID ) : Claim()
    {
        override val value = deploymentId.toString()
    }
}