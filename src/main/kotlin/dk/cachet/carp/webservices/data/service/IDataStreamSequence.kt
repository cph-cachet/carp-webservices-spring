package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.data.application.*

// Define a common interface for DataStreamSequence
interface IDataStreamSequence<TData : Data> : Sequence<DataStreamPoint<TData>> {
    val dataStream: DataStreamId
    val firstSequenceId: Long
    val measurements: List<Measurement<TData>>
    val triggerIds: List<Int>
    val syncPoint: SyncPoint

    // This method converts the sequence to a mutable version
    fun toMutableDataStreamSequence(): MutableDataStreamSequence<TData>

    val range: LongRange get() =
        if (measurements.isEmpty()) LongRange.EMPTY
        else firstSequenceId until firstSequenceId + measurements.size

    fun isImmediatelyFollowedBy(sequence: IDataStreamSequence<TData>): Boolean =
        dataStream == sequence.dataStream &&
                triggerIds == sequence.triggerIds &&
                syncPoint == sequence.syncPoint &&
                if (range == LongRange.EMPTY) firstSequenceId == sequence.firstSequenceId
                else range.last + 1 == sequence.firstSequenceId
}
// Compare this snippet from src/main/kotlin/dk/cachet/carp/webservices/data/service/impl/DataStreamServiceWrapper.kt:
