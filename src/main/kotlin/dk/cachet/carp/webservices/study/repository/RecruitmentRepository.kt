package dk.cachet.carp.webservices.study.repository

import dk.cachet.carp.webservices.study.domain.Recruitment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface RecruitmentRepository : JpaRepository<Recruitment, Int> {
    @Query(value = "SELECT * FROM recruitments WHERE snapshot->>'studyId' = ?1", nativeQuery = true)
    fun findRecruitmentByStudyId(studyId: String): Recruitment?

    @Modifying
    @Transactional
    @Query(
        nativeQuery = true,
        value = "DELETE FROM recruitments WHERE snapshot->>'studyId' = ?1",
    )
    fun deleteByStudyId(studyId: String)

    @Query(
        value = """
            SELECT jsonb_agg(elem) AS participants_with_limit_and_offset
            FROM (
                SELECT elem
                FROM public.recruitments, 
                     jsonb_array_elements(snapshot->'participants') WITH ORDINALITY arr(elem, idx)
                WHERE snapshot->>'studyId' = :studyId
                AND (
                    :search IS NULL 
                    OR elem->'accountIdentity'->>'username' ILIKE '%' || :search || '%'
                    OR elem->'accountIdentity'->>'emailAddress' ILIKE '%' || :search || '%'
                )
                ORDER BY idx
                LIMIT :limit OFFSET :offset
            ) subquery
        """,
        nativeQuery = true,
    )
    fun findParticipantsByStudyIdWithPagination(
        studyId: String,
        offset: Int?,
        limit: Int?,
        search: String?,
    ): String?
}
