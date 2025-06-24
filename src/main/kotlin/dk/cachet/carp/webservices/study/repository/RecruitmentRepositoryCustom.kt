package dk.cachet.carp.webservices.study.repository

import dk.cachet.carp.webservices.study.domain.ParticipantOrderBy

interface RecruitmentRepositoryCustom {
    @Suppress("LongParameterList")
    fun findRecruitmentParticipantsByStudyIdAndSearchAndLimitAndOffset(
        studyId: String,
        offset: Int?,
        limit: Int?,
        search: String?,
        isDescending: Boolean?,
        orderBy: ParticipantOrderBy?,
    ): String?
}
