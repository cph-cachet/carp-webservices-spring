package dk.cachet.carp.webservices.deployment.repository

import dk.cachet.carp.webservices.deployment.domain.ParticipantGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface ParticipantGroupRepository: JpaRepository<ParticipantGroup, Int>
{
    @Query(nativeQuery = true,
            value = "SELECT * FROM participant_groups WHERE snapshot->>'studyDeploymentId' = ?1")
    fun findByStudyDeploymentId(studyDeploymentId: String): Optional<ParticipantGroup>

    @Query(nativeQuery = true,
            value = "SELECT * FROM participant_groups WHERE snapshot->>'studyDeploymentId' in ?1")
    fun findAllByStudyDeploymentIds(studyDeploymentIds: Collection<String>): List<ParticipantGroup>

    @Query(nativeQuery = true,
                value = "SELECT p.* FROM participant_groups p " +
                        "CROSS JOIN LATERAL jsonb_array_elements(p.snapshot -> 'participations') AS a WHERE a ->> 'accountId' = ?1")
    fun findAllByAccountId(accountId: String): List<ParticipantGroup>

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "DELETE FROM participant_groups WHERE snapshot->>'studyDeploymentId' in ?1")
    fun deleteByDeploymentIds(ids: Collection<String>)
}
