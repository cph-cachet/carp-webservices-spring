package dk.cachet.carp.webservices.study.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceDecorator
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.study.domain.AnonymousParticipant
import dk.cachet.carp.webservices.study.domain.ParticipantGroupsStatus

interface RecruitmentService
{
    val core: RecruitmentServiceDecorator
    suspend fun inviteResearcher( studyId: UUID, email: String )
    suspend fun removeResearcher( studyId: UUID, email: String ): Boolean
    suspend fun getParticipants( studyId: UUID ) : List<Account>
    fun isParticipant( studyId: UUID, accountId: UUID ): Boolean
    suspend fun getParticipantGroupsStatus( studyId: UUID ) : ParticipantGroupsStatus
    suspend fun addAnonymousParticipants(
        studyId: UUID,
        amount: Int,
        expirationSeconds: Long,
        participantRoleName: String,
        redirectUri: String?
    ): List<AnonymousParticipant>
}