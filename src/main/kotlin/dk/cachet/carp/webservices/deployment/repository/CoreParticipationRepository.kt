package dk.cachet.carp.webservices.deployment.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.domain.users.AccountParticipation
import dk.cachet.carp.deployments.domain.users.ParticipantGroup
import dk.cachet.carp.deployments.domain.users.ParticipantGroupSnapshot
import dk.cachet.carp.deployments.domain.users.ParticipationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * The Class [CoreParticipationRepository].
 * [CoreParticipationRepository] provides implementation for the core [ParticipationRepository] interface.
 */
@Service
@Transactional
class CoreParticipationRepository
(
    private val participantGroupRepository: ParticipantGroupRepository,
    private val objectMapper: ObjectMapper
): ParticipationRepository
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * Returns the [ParticipantGroup] for the specified [studyDeploymentId], or null when it is not found.
     */
    override suspend fun getParticipantGroup(studyDeploymentId: UUID): ParticipantGroup? = withContext( Dispatchers.IO )
    {
        val optionalGroup = participantGroupRepository.findByStudyDeploymentId(studyDeploymentId.stringRepresentation)

        if (!optionalGroup.isPresent)
        {
            LOGGER.warn("Participant group was not found for deployment with id: ${studyDeploymentId.stringRepresentation}")
            return@withContext null
        }

        val group = optionalGroup.get()
        mapParticipantGroupSnapshotJsonNodeToParticipantGroup(group.snapshot!!)
    }

    /**
     * Return all [ParticipantGroup]s matching the specified [studyDeploymentIds].
     * Ids that are not found are ignored.
     */
    override suspend fun getParticipantGroupList(studyDeploymentIds: Set<UUID>): List<ParticipantGroup> =
        withContext( Dispatchers.IO )
        {
            val ids = studyDeploymentIds.map { id -> id.stringRepresentation }.toSet()
            val groups = participantGroupRepository.findAllByStudyDeploymentIds(ids)
            groups.map { group -> mapParticipantGroupSnapshotJsonNodeToParticipantGroup(group.snapshot!!) }
        }

    /**
     * Get all participation invitations for the account with the specified [accountId].
     */
    override suspend fun getParticipationInvitations(accountId: UUID): Set<AccountParticipation> =
        withContext(Dispatchers.IO)
        {
            participantGroupRepository.findAllByAccountId(accountId.stringRepresentation)
                .flatMap { mapParticipantGroupSnapshotJsonNodeToParticipantGroup(it.snapshot!!).participations }
                .toSet()
        }

    /**
     * Adds or updates the participant [group] in this repository.
     *
     * @return the previous [ParticipantGroup] stored in the repository, or null if it was not present before.
     */
    override suspend fun putParticipantGroup(group: ParticipantGroup): ParticipantGroup?=
        withContext( Dispatchers.IO )
        {
            val optionalGroup = participantGroupRepository.findByStudyDeploymentId(group.studyDeploymentId.stringRepresentation)
            val snapshotToSave = objectMapper.valueToTree<JsonNode>(group.getSnapshot())

            if (!optionalGroup.isPresent)
            {
                val newParticipantGroup = dk.cachet.carp.webservices.deployment.domain.ParticipantGroup()
                newParticipantGroup.snapshot = snapshotToSave
                val savedGroup = participantGroupRepository.save(newParticipantGroup)
                LOGGER.info("New participant group with id: ${savedGroup.id} saved for deployment with id: ${group.studyDeploymentId.stringRepresentation}")
                return@withContext null
            }

            val storedGroup = optionalGroup.get()
            val oldSnapshot = storedGroup.snapshot!!
            storedGroup.snapshot = snapshotToSave
            participantGroupRepository.save(storedGroup)
            LOGGER.info("Participant Group with id: ${storedGroup.id} is updated with a new snapshot.")

            mapParticipantGroupSnapshotJsonNodeToParticipantGroup(oldSnapshot)
        }

    /**
     * Remove the [ParticipantGroup]s matching the specified [studyDeploymentIds].
     *
     * @return The IDs of study deployments for which participant groups were removed. IDs for which no participant group exists are ignored.
     */
    override suspend fun removeParticipantGroups(studyDeploymentIds: Set<UUID>): Set<UUID> =
        withContext( Dispatchers.IO )
        {
            val ids = studyDeploymentIds.map { it.stringRepresentation }
            val idsPresent = participantGroupRepository.findAllByStudyDeploymentIds(ids)
                    .map { mapParticipantGroupSnapshotJsonNodeToParticipantGroup(it.snapshot!!).studyDeploymentId.stringRepresentation }
            participantGroupRepository.deleteByDeploymentIds(idsPresent)
            LOGGER.info("ParticipantGroups removed for deployments with ids: ${idsPresent.joinToString(", ")}")
            idsPresent.map { UUID(it) }.toSet()
        }

    /**
     * Maps [ParticipantGroupSnapshot] as a JsonNode to [ParticipantGroup]
     *
     * @param node The [JsonNode] that needs mapping.
     * @return [ParticipantGroup]
     */
    private fun mapParticipantGroupSnapshotJsonNodeToParticipantGroup(node: JsonNode): ParticipantGroup
    {
        val snapshot = objectMapper.treeToValue(node, ParticipantGroupSnapshot::class.java)
        return ParticipantGroup.fromSnapshot(snapshot)
    }

}