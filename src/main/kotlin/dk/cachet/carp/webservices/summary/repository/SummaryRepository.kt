package dk.cachet.carp.webservices.summary.repository

import dk.cachet.carp.webservices.summary.domain.Summary
import dk.cachet.carp.webservices.summary.domain.SummaryStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface SummaryRepository: JpaRepository<Summary, String>
{
    fun findAllByCreatedBy(accountId: String): List<Summary>

    fun findAllByCreatedByAndStudyId(accountId: String, studyId: String): List<Summary>

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "DELETE FROM summary WHERE study_id = :studyId")
    fun deleteByStudyId(studyId: String)

    @Modifying
    @Transactional
    @Query("UPDATE summary SET status = :status WHERE id = :summaryId")
    fun updateSummaryStatus(status: SummaryStatus, summaryId: String)
}