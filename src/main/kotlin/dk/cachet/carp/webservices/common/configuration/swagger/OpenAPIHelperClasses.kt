package dk.cachet.carp.webservices.common.configuration.swagger

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.ParticipantData
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.StudyStatus
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus

data class SetOfUUID(val uuids: Set<UUID>)

data class ListOfParticipant(val participants: List<Participant>)

data class ListOfParticipantGroupStatus(val participantGroupStatuses: List<ParticipantGroupStatus>)

data class ListStudyStatus(val studyStatuses: List<StudyStatus>)

data class ListStudyDeploymentStatus(val studyDeploymentStatuses: List<StudyDeploymentStatus>)

data class SetOfActiveParticipationInvitations(val activeParticipationInvitations: Set<ActiveParticipationInvitation>)

data class ListOfParticipantData(val participantData: List<ParticipantData>)

data class ListOfStudyProtocolSnapshot(val studyProtocolSnapshots: StudyProtocolSnapshot)

data class ListOfProtocolVersion(val protocolVersions: List<ProtocolVersion>)
