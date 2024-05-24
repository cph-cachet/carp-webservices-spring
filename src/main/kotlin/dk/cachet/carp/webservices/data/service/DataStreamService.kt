package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.webservices.data.domain.DataStreamSequence
import kotlinx.datetime.Instant

interface DataStreamService
{
    val core: DataStreamServiceDecorator
    fun getLatestUpdatedAt( deploymentId: UUID ): Instant?
}