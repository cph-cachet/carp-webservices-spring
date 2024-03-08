package dk.cachet.carp.webservices.study.domain

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant

data class StudyOverview
(
      /** StudyStatus */
      val studyId: UUID,
      val name: String,
      val createdOn: Instant,

      /** StudyCreatedBy */
      val createdBy: UUID?,

      val studyProtocolId: UUID?,
      val canSetInvitation: Boolean,
      val canSetStudyProtocol: Boolean,
      val canDeployToParticipants: Boolean,

      /** StudyDescription */
      val description: String?
)