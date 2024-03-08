package dk.cachet.carp.webservices.protocol.dto

import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import java.time.Instant

/*data class ProtocolOverviewDto(
    *//** ProtocolOwnerName *//*
    val ownerName: String?,

    *//** ProtocolStatus *//*
    val snapshot: StudyProtocolSnapshot,
    val lastVersionCreatedDate: Instant
)

*//**GetLatestVersionProtocolOverviewDto

 * The [GetLatestProtocolResponseDto.toProtocolOverview()] function converts
 * a data type [GetLatestProtocolResponseDto] to a [ProtocolOverviewDto].
 *
 * @return A [ProtocolOverviewDto] object containing the protocol.
 *//*

fun GetLatestProtocolResponseDto.toProtocolOverviewDto(): ProtocolOverviewDto {
    // Extract ownerName from the StudyProtocolSnapshot
    val snapshotOwnerName = snapshot.name // Replace with the actual property name in StudyProtocolSnapshot

    // Create a ProtocolOverview instance using the extracted data
    return ProtocolOverviewDto(
        ownerName = snapshotOwnerName,
        snapshot = this.snapshot,
        lastVersionCreatedDate = this.lastVersionCreatedDate
    )
}*/

data class ProtocolOverviewDto(
    val ownerName: String?,
    val snapshot: StudyProtocolSnapshot,
    val lastVersionCreatedDate: Instant
) {
    companion object {
        /**
         * The [GetLatestProtocolResponseDto.toProtocolOverviewDto()] function converts
         * a data type [GetLatestProtocolResponseDto] to a [ProtocolOverviewDto].
         *
         * @return A [ProtocolOverviewDto] object containing the protocol.
         */
        fun GetLatestProtocolResponseDto.toProtocolOverviewDto(): ProtocolOverviewDto {
            // Extract ownerName from the StudyProtocolSnapshot
            val snapshotOwnerName = snapshot.name // Replace with the actual property name in StudyProtocolSnapshot

            // Create a ProtocolOverview instance using the extracted data
            return ProtocolOverviewDto(
                ownerName = snapshotOwnerName,
                snapshot = this.snapshot,
                lastVersionCreatedDate = this.lastVersionCreatedDate
            )
        }
    }
}
