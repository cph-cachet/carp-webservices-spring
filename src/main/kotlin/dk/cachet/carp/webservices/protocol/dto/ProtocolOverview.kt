package dk.cachet.carp.webservices.protocol.dto

import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import java.time.Instant

data class LatestProtocolOverview
(
    val ownerName: String?,
    val snapshot: StudyProtocolSnapshot,
    val lastVersionCreatedDate: Instant
) {
    /**
     * The [GetLatestProtocolResponseDto.toLatestProtocolOverview()] function converts
     * a data type [GetLatestProtocolResponseDto] to a [LatestProtocolOverview].
     *
     * @return A [LatestProtocolOverview] object containing the protocol from [snapshot].
     */
    companion object {
        fun GetLatestProtocolResponseDto.toLatestProtocolOverview(): LatestProtocolOverview {
            val snapshotOwnerName = snapshot.name

            return LatestProtocolOverview(
                ownerName = snapshotOwnerName,
                snapshot = this.snapshot,
                lastVersionCreatedDate = this.lastVersionCreatedDate
            )
        }
    }
}
