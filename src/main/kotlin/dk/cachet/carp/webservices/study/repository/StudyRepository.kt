package dk.cachet.carp.webservices.study.repository

import dk.cachet.carp.webservices.study.domain.Study
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface StudyRepository: JpaRepository<Study, Int>
{
    @Query(nativeQuery = true,
            value = "SELECT * FROM studies WHERE snapshot->>'id' = ?1")
    fun getByStudyId(id: String): Optional<Study>

    @Query(nativeQuery = true,
            value = "SELECT * FROM studies WHERE snapshot->>'ownerId' = ?1")
    fun findAllByOwnerId(ownerId: String): List<Study>

    @Query(value = "SELECT s FROM studies s JOIN s.researcherAccountIds a WHERE a = :accountId")
    fun getForGuestResearcher(@Param("accountId") accountId: String): List<Study>

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "DELETE FROM studies WHERE snapshot->>'id' = ?1")
    fun deleteByStudyId(studyId: String)
}