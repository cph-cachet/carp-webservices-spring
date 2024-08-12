package dk.cachet.carp.webservices.datastream.service.impl

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.data.application.*
import org.springframework.stereotype.Service

/** UNDER CONSTRUCTION --> CHECK create a function that checks sequence IDs before appending
 * + usable if we can get back last preceding upload of a sequence so the client know what to send
 * This a "trick" to make the class `DataStreamBatch` open for allowing appendDataStream without checking
 * all the sequence IDs in the append method. This is a temporary solution until we find a better way to handle this.
 */
@Service
class MutableDataStreamBatchDecorator : Sequence<DataStreamPoint<*>>, DataStreamBatch {
    private val sequenceMap: MutableMap<DataStreamId, MutableList<MutableDataStreamSequence<*>>> = mutableMapOf()

    /**
     * A list of sequences covering all sequences so far appended to [MutableDataStreamBatchDecorator].
     * This may return less sequences than originally appended in case appended sequences were merged with prior ones.
     */
    override val sequences: Sequence<DataStreamSequence<*>>
        get() = sequenceMap.asSequence().flatMap { it.value }.map { it }

    /**
     * Consider val for sequence.toMutableDataStreamSequence(), but not necessary
     */
    @Suppress("UNCHECKED_CAST")
    fun appendSequence(sequence: DataStreamSequence<*>) {
        val sequenceList = sequenceMap[sequence.dataStream]

        if (sequenceList == null) {
            sequenceMap[sequence.dataStream] = mutableListOf(sequence.toMutableDataStreamSequence())
            return
        }

        val last = sequenceList.last() as MutableDataStreamSequence<Data>

        if (last.isImmediatelyFollowedBy(sequence)) {
            last.appendSequence(sequence as MutableDataStreamSequence<Data>)
        } else {
            sequenceList.add(sequence.toMutableDataStreamSequence())
        }
    }

    /**
     * Append all data stream sequences contained in [batch] to this batch.
     * rid of premature optimization and sequential checks. <-- let fly in anything and check later.
     * --> This is a temporary solution
     */

    fun appendBatch(batch: DataStreamBatch) {
        batch.sequences.forEach(::appendSequence)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataStreamBatch) return false

        return toList() == other.toList()
    }

    override fun hashCode(): Int = sequences.hashCode()
}
