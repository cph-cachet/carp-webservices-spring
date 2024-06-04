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

// Define DataStreamSequenceA that implements IDataStreamSequence]


/*@Serializable
data class DataStreamSequenceA(
    override val dataStream: DataStreamId,
    override val firstSequenceId: Long,
    override val measurements: List<Measurement<*>>,
    override val triggerIds: List<Int>,
    override val syncPoint: SyncPoint
) : IDataStreamSequence<DataTypeA> {
    override fun toMutableDataStreamSequence(): MutableDataStreamSequence<DataTypeA> {
        TODO("Not yet implemented")
    }

    override fun iterator(): Iterator<DataStreamPoint<DataTypeA>> =
        measurements.asSequence().mapIndexed { index, measurement ->
            DataStreamPoint(
                firstSequenceId + index,
                dataStream.studyDeploymentId,
                dataStream.deviceRoleName,
                measurement,
                triggerIds,
                syncPoint
            )
        }.iterator()


}*/
