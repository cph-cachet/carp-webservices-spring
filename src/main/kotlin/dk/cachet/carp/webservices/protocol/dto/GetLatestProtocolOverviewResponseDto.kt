package dk.cachet.carp.webservices.protocol.dto

import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import java.time.Instant

data class GetLatestProtocolOverviewResponseDto(
    val ownerName: String?,
    val snapshot: StudyProtocolSnapshot,
    val lastVersionCreatedDate: Instant
) {
    /**
     * The [from] function converts
     * a data type [GetLatestProtocolResponseDto] to a [GetLatestProtocolOverviewResponseDto].
     *
     * @return A [GetLatestProtocolOverviewResponseDto] object containing the protocol from [snapshot].
     */
    companion object {
        fun from(dto: GetLatestProtocolResponseDto): GetLatestProtocolOverviewResponseDto {
            return GetLatestProtocolOverviewResponseDto(
                ownerName = dto.snapshot!!.name,
                snapshot = dto.snapshot,
                lastVersionCreatedDate = dto.lastVersionCreatedDate
            )
        }
    }
}