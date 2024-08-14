package dk.cachet.carp.webservices.datastream.domain

import dk.cachet.carp.data.application.Measurement
import dk.cachet.carp.data.application.SyncPoint

data class DataStreamSnapshot(
    val measurements: List<Measurement<*>>,
    val triggerIds: List<Int>,
    val syncPoint: SyncPoint,
)
