package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamSequence
import dk.cachet.carp.data.application.MutableDataStreamSequence

/**
 * A mutable collection of non-overlapping data stream [sequences].
 */
class MutableDataStreamBatch : DataStreamBatch
{

    private val sequenceMap: MutableMap<DataStreamId, MutableList<MutableDataStreamSequence<*>>> = mutableMapOf()

    /**
     * A list of sequences covering all sequences so far appended to this [MutableDataStreamBatch].
     * This may return less sequences than originally appended in case appended sequences were merged with prior ones.
     */
    override val sequences: Sequence<DataStreamSequence<*>>
        get() = sequenceMap.asSequence().flatMap { it.value }


    /**
     * Append a sequence to a non-existing or previously appended data stream in this batch.
     *
     * @throws IllegalArgumentException when:
     *  - the start of the [sequence] range precedes the end of a previously appended sequence to the same data stream
     *  - the sync point of [sequence] is older than that of previous sequences in this batch
     */
    @Suppress("UNCHECKED_CAST")
    fun appendSequence(sequence: DataStreamSequence<*>) {
        val sequenceList = sequenceMap[sequence.dataStream]

        // Early out if this is the first sequence added for this data stream.
        if (sequenceList == null) {
            sequenceMap[sequence.dataStream] = mutableListOf(sequence.toMutableDataStreamSequence())
            return
        }

        val last = sequenceList.last() as MutableDataStreamSequence<Data>

        // Merge sequence with last sequence if possible; add new sequence otherwise.
        if (last.isImmediatelyFollowedBy(sequence)) {
            last.appendSequence(sequence as DataStreamSequence<Data>)
        } else {
            sequenceList.add(sequence.toMutableDataStreamSequence())
        }
    }

    /**
     * Append all data stream sequences contained in [batch] to this batch.
     *
     * @throws IllegalArgumentException when the start of any of the sequences contained in [batch]
     *   precede the end of a previously appended sequence to the same data stream.
     */
    fun appendBatch(batch: DataStreamBatch) {
       when (batch) {
            is MutableDataStreamBatch ->
                // Preconditions for `MutableDataStreamBatch` can be verified much more easily.
                // This might seem like premature optimization, but currently it is the only concrete class.
                // We expect many sequences for one data type to be common, e.g., RR intervals have many sync points.
                batch.sequenceMap
                        .mapValues { it.value.last() }
                        .all { (dataStream, lastSequence) ->
                            val lastStoredSequence = sequenceMap[dataStream]?.last()
                            if (lastStoredSequence == null) true
                            else lastStoredSequence.range.last < lastSequence.range.first
                        }
            else ->
                batch.sequences.all { sequence ->
                    sequenceMap[sequence.dataStream]?.last().let { lastStoredSequence ->
                        if (lastStoredSequence == null) true
                        else lastStoredSequence.range.last < sequence.range.first
                    }
                }
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

