package dk.cachet.carp.webservices.data.service.impl

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.data.application.*

/** UNDER CONSTRUCTION --> CHECK create a function that checks sequence IDs before appending
 * + usable if we can get back last preceding upload of a sequence so the client know what to send
 * This a "trick" to make the class `DataStreamBatch` open for allowing appendDataStream without checking
 * all the sequence IDs in the append method. This is a temporary solution until we find a better way to handle this.
 */
class CawsMutableDataStreamBatchWrapper : Sequence<DataStreamPoint<*>>, DataStreamBatch {
    private val sequenceMap: MutableMap<DataStreamId, MutableList<MutableDataStreamSequence<*>>> = mutableMapOf()

    /**
     * A list of sequences covering all sequences so far appended to [CawsMutableDataStreamBatchWrapper].
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
     *
     * no catch to throw "IllegalArgumentException" when the start of any sequences contained in [batch]
     * precede the end of a previously appended sequence to the same data stream. --> This is a temporary solution
     */

    fun appendBatch(batch: DataStreamBatch) {
        val containsNoPrecedingSequence =
            when (batch) {
                is CawsMutableDataStreamBatchWrapper ->
                    // Preconditions for `MutableDataStreamBatch` can be verified much more easily.
                    // This might seem like premature optimization, but currently it is the only concrete class.
                    // We expect many sequences for one data type to be common,
                    // e.g., RR intervals have many sync points.
                    batch.sequenceMap
                        .mapValues { it.value.last() }
                        .all { (dataStream, lastSequence) ->
                            val lastStoredSequence = sequenceMap[ dataStream ]?.last()
                            if (lastStoredSequence == null) {
                                true
                            } else {
                                lastStoredSequence.range.last < lastSequence.range.first
                            }
                        }
                else ->
                    batch.sequences.all { sequence ->
                        sequenceMap[ sequence.dataStream ]?.last().let { lastStoredSequence ->
                            if (lastStoredSequence == null) {
                                true
                            } else {
                                lastStoredSequence.range.last < sequence.range.first
                            }
                        }
                    }
            }
        require(containsNoPrecedingSequence) {
            "The batch contains a sequence of which the start precedes a previously appended sequence"
        }

        batch.sequences.forEach(::appendSequence)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataStreamBatch) return false

        return toList() == other.toList()
    }

    override fun hashCode(): Int = sequences.hashCode()
}
