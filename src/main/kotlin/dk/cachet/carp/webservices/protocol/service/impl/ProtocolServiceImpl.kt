package dk.cachet.carp.webservices.protocol.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.protocol.domain.Protocol
import dk.cachet.carp.webservices.protocol.dto.ProtocolOverview
import dk.cachet.carp.webservices.protocol.repository.ProtocolRepository
import dk.cachet.carp.webservices.protocol.service.ProtocolService
import dk.cachet.carp.webservices.security.authentication.domain.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class ProtocolServiceImpl(
    private val accountService: AccountService,
    private val protocolRepository: ProtocolRepository,
    private val objectMapper: ObjectMapper,
    services: CoreServiceContainer
): ProtocolService
{
    final override val core = services.protocolService

    override suspend fun getSingleProtocolOverview( protocolId: String ): ProtocolOverview? =
        withContext( Dispatchers.IO )
        {
            val versions = protocolRepository.findAllByIdSortByCreatedAt( protocolId )
            if ( versions.isEmpty() ) return@withContext null

            createProtocolOverview( versions )
        }

    override suspend fun getProtocolsOverview( accountId: UUID ): List<ProtocolOverview> =
        withContext( Dispatchers.IO )
        {
            val account = accountService.findByUUID( accountId ) ?:
                throw IllegalArgumentException("Account with id $accountId is not found.")

            protocolRepository.findAllByOwnerId( account.id!! )
                .filter { it.snapshot != null }
                .groupBy { it.snapshot?.get("id").toString() }
                .map {(_, versions) ->
                    val sorted = versions.sortedBy { it.createdAt }
                    createProtocolOverview( sorted, account )
                }
        }

    /**
     * Get the [ProtocolOverview] from a sorted list of all the versions of a protocol.
     *
     * @param versions A list of all the versions of a protocol sorted by creation date.
     * @param account The account to use in the protocol overview. Will be looked up if not provided.
     */
    private suspend fun createProtocolOverview( versions: List<Protocol>, account: Account? = null ): ProtocolOverview
    {
        val snapshot = objectMapper.treeToValue( versions.last().snapshot, StudyProtocolSnapshot::class.java )

        val owner = account ?: accountService.findByUUID( snapshot.ownerId )
        val ownerName = owner?.let { "${it.firstName} ${it.lastName}" }

        return ProtocolOverview(
            ownerName,
            versions.first().createdAt,
            versions.last().createdAt,
            versions.last().versionTag,
            snapshot
        )
    }
}