package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import kotlinx.datetime.Instant

interface DataStreamService {
    val core: DataStreamServiceDecorator
    fun getLatestUpdatedAt( deploymentId: UUID ): Instant?

}