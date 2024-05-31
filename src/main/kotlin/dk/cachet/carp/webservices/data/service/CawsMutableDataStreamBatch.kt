package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamId
import dk.cachet.carp.data.application.DataStreamSequence
import dk.cachet.carp.data.application.MutableDataStreamSequence

/**
 * This a "trick" to make the class `DataStreamBatch` open for allowing appendDataStream without checking
 * all the sequence IDs in the append method. This is a temporary solution until we find a better way to handle this.
 */
class CawsMutableDataStreamBatch : DataStreamBatch
{
    private val sequenceMap: MutableMap<DataStreamId, MutableList<MutableDataStreamSequence<*>>> = mutableMapOf()

    /**
     * A list of sequences covering all sequences so far appended to this [CawsMutableDataStreamBatch].
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
    @Suppress( "UNCHECKED_CAST" )
    fun appendSequence( sequence: DataStreamSequence<*>)
    {
        val sequenceList = sequenceMap[ sequence.dataStream ]

        // Early out if this is the first sequence added for this data stream.
        if ( sequenceList == null )
        {
            sequenceMap[ sequence.dataStream ] = mutableListOf( sequence.toMutableDataStreamSequence() )
            return
        }

        val last = sequenceList.last() as MutableDataStreamSequence<Data>

        // Merge sequence with last sequence if possible; add new sequence otherwise.
        if ( last.isImmediatelyFollowedBy( sequence ) )
        {
            last.appendSequence( sequence as DataStreamSequence<Data>)
        }
        else { sequenceList.add( sequence.toMutableDataStreamSequence() ) }
    }

    /**
     * Append all data stream sequences contained in [batch] to this batch.
     *
     * @throws IllegalArgumentException when the start of any sequences contained in [batch]
     *   precede the end of a previously appended sequence to the same data stream.
     */
    fun appendBatch(batch: DataStreamBatch) {
        // Map and get the last sequence from each data stream in the batch
        when (batch) {
            is CawsMutableDataStreamBatch -> {
                batch.sequenceMap.mapValues { it.value.last() }
            }
            else -> {
                batch.sequences.map { sequence ->
                    sequence.dataStream to sequence
                }
            }
        }
        // Append each sequence from the batch
        batch.sequences.forEach(::appendSequence)
    }

    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( other !is DataStreamBatch) return false

        return toList() == other.toList()
    }

    override fun hashCode(): Int = sequences.hashCode()
}