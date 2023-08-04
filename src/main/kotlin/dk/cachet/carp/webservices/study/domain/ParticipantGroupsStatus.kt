package dk.cachet.carp.webservices.study.domain

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.webservices.security.authentication.domain.Account

data class ParticipantGroupsStatus
(
    val groups: List<ParticipantGroupInfo>,
    val groupStatuses: List<ParticipantGroupStatus>
)

data class ParticipantGroupInfo
(
    val participantGroupId: UUID,
    val participants: List<ParticipantAccount>
)

data class ParticipantAccount
(
    val participantId: UUID,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null
) {
    companion object {

        fun fromParticipant( participant: Participant ): ParticipantAccount =
            ParticipantAccount(
                participantId = participant.id,
                email = (participant.accountIdentity as? EmailAccountIdentity)?.emailAddress?.address
            )
    }

    fun lateInitFrom( account: Account ) {
        firstName ?: account.firstName
        lastName ?: account.lastName
        email ?: account.email
    }
}
