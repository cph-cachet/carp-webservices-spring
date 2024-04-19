package dk.cachet.carp.webservices.study.domain

data class ParticipantInfoGeneral (
    val email: String?,
    val firstName: String?,
    val lastName: String?,
    val isRegistered: String?,
    val invitedOn: String?,
    val magicLink: String?
)

/*
email
firstName
lastName
isRegistered
invitedOn
participantId (anonymous links)
*/
