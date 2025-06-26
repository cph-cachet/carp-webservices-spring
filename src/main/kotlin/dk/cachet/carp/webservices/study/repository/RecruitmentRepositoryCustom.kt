package dk.cachet.carp.webservices.study.repository

interface RecruitmentRepositoryCustom {
    @Suppress("LongParameterList")
    fun findRecruitmentParticipantsByStudyIdAndSearchAndLimitAndOffset(
        studyId: String,
        offset: Int?,
        limit: Int?,
        search: String?,
        isDescending: Boolean?,
    ): String?
}
