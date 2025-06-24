package dk.cachet.carp.webservices.study.repository

import dk.cachet.carp.webservices.study.domain.ParticipantOrderBy
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

class RecruitmentRepositoryImpl(
    @PersistenceContext private val entityManager: EntityManager,
) : RecruitmentRepositoryCustom {
    override fun findRecruitmentParticipantsByStudyIdAndSearchAndLimitAndOffset(
        studyId: String,
        offset: Int?,
        limit: Int?,
        search: String?,
        isDescending: Boolean?,
        orderBy: ParticipantOrderBy?,
    ): String? {
        val direction = if (isDescending == true) "DESC" else "ASC"

        val searchCondition =
            if (search != null) {
                """
            AND (
                elem->'accountIdentity'->>'username' ILIKE CONCAT('%', :search, '%')
                OR elem->'accountIdentity'->>'emailAddress' ILIKE CONCAT('%', :search, '%')
            )
        """
            } else {
                ""
            }

        val orderByClause =
            if (orderBy == null) {
                "idx"
            } else if (orderBy == ParticipantOrderBy.Username) {
                "elem->'accountIdentity'->>'username'"
            } else {
                "elem->'accountIdentity'->>'emailAddress'"
            }

        val sql = """
            SELECT jsonb_agg(elem) AS participants_
            FROM (
                SELECT elem
                FROM public.recruitments,
                     jsonb_array_elements(snapshot->'participants') WITH ORDINALITY arr(elem, idx)
                WHERE snapshot->>'studyId' = :studyId
                $searchCondition
                ORDER BY $orderByClause $direction
                LIMIT :limit OFFSET :offset
            ) subquery
        """

        val query = entityManager.createNativeQuery(sql)
        query.setParameter("studyId", studyId)
        query.setParameter("limit", limit)
        query.setParameter("offset", offset)
        if (search != null) query.setParameter("search", search)

        return query.singleResult as? String
    }
}
