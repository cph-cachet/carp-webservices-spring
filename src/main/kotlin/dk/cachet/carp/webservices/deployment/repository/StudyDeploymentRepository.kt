package dk.cachet.carp.webservices.deployment.repository

import dk.cachet.carp.webservices.deployment.domain.StudyDeployment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface StudyDeploymentRepository: JpaRepository<StudyDeployment, String>
{
    @Query(value = "SELECT * FROM deployments WHERE snapshot->>'id' = ?1", nativeQuery = true)
    fun findByDeploymentId(id: String): Optional<StudyDeployment>

    @Query(value = "SELECT * FROM deployments WHERE snapshot->>'id' in ?1", nativeQuery = true)
    fun findAllByStudyDeploymentIds(deploymentIds: Set<String>): List<StudyDeployment>

    fun findAllByDeployedFromStudyId(studyId: String): List<StudyDeployment>

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "DELETE FROM deployments WHERE snapshot->>'id' in ?1")
    fun deleteByDeploymentIds(ids: Collection<String>)
}