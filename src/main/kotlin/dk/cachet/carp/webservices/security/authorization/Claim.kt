package dk.cachet.carp.webservices.security.authorization

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.extensions.toSnakeCase
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

sealed class Claim
{
    protected abstract val value: Set<String>

    fun toTokenClaimObject(): Pair<String, Any> = getKey( this::class ) to value

    companion object
    {
        fun getKey( klass: KClass<*> ) = klass.simpleName!!.toSnakeCase()

        fun fromTokenClaimObject( pair: Pair<String, Any> ): Claim?
        {
            val claimKlass = Claim::class.sealedSubclasses.first { getKey( it ) == pair.first }

            val uuids = (pair.second as? Set<*>)
                ?.mapNotNull {
                    runCatching { UUID(it.toString()) }.getOrNull()
                } ?: emptyList()

            return claimKlass.primaryConstructor?.call(uuids)
        }
    }

    data class ManageDeployment( val deploymentIds: Set<UUID> ) : Claim()
    {
        constructor( deploymentId: UUID ) : this( setOf( deploymentId ) )

        override val value = deploymentIds.map { it.toString() }.toSet()
    }

    data class ManageStudy( val studyIds: Set<UUID> ) : Claim()
    {
        constructor( studyId: UUID ) : this( setOf( studyId ) )

        override val value = studyIds.map { it.toString() }.toSet()
    }

    data class ProtocolOwner( val ownerIds: Set<UUID> ) : Claim()
    {
        constructor( ownerId: UUID ) : this( setOf( ownerId ) )

        override val value = ownerIds.map { it.toString() }.toSet()
    }

    data class InDeployment( val deploymentIds: Set<UUID> ) : Claim()
    {
        constructor( deploymentId: UUID ) : this( setOf( deploymentId ) )

        override val value = deploymentIds.map { it.toString() }.toSet()
    }
}