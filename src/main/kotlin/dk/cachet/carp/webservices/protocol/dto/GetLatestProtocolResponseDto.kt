package dk.cachet.carp.webservices.protocol.dto

import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import java.time.Instant


data class GetLatestProtocolResponseDto (
    val versionTag: String,
    val snapshot: StudyProtocolSnapshot,
    val firstVersionCreatedDate: Instant,
    val lastVersionCreatedDate: Instant
)