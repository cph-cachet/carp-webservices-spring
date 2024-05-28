package dk.cachet.carp.webservices.protocol.dto

import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import java.time.Instant

/**
 * A DTO representing the latest [StudyProtocolSnapshot] with auxiliary information.
 *
 * TODO: why are we using Java Instant here?
 */
data class ProtocolOverview(
    val ownerName: String?,
    val firstVersionCreatedDate: Instant?,
    val lastVersionCreatedDate: Instant?,
    val versionTag: String,
    val snapshot: StudyProtocolSnapshot,
)
