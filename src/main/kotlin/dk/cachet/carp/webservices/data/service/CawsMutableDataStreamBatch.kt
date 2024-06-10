package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.data.application.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/** TODO create a function that checks sequence IDs before appending
 * + usable if we can get back last preceding upload of a sequence so the client know what to send
 *
 * */

/**
 * This a "trick" to make the class `DataStreamBatch` open for allowing appendDataStream without checking
 * all the sequence IDs in the append method. This is a temporary solution until we find a better way to handle this.
 */
class CawsMutableDataStreamBatch : Sequence<DataStreamPoint<*>>, DataStreamBatch

{
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }
    private val sequenceMap: MutableMap<DataStreamId, MutableList<MutableDataStreamSequence<*>>> = mutableMapOf()

    /**
     * A list of sequences covering all sequences so far appended to [CawsMutableDataStreamBatch].
     * This may return less sequences than originally appended in case appended sequences were merged with prior ones.
     */
    override val sequences: Sequence<DataStreamSequence<*>>
        get() = sequenceMap.asSequence().flatMap { it.value }.map { it as DataStreamSequence<*> }

    @Suppress("UNCHECKED_CAST")
    private fun <TData : Data> appendSequenceInternal(sequence: DataStreamSequence<TData>) {
        val mutableSequence = sequence.toMutableDataStreamSequence()
        val sequenceList = sequenceMap[sequence.dataStream]

        if (sequenceList == null) {
            sequenceMap[sequence.dataStream] = mutableListOf(mutableSequence)
            return
        }

        val last = sequenceList.last() as MutableDataStreamSequence<TData>

        if (last.isImmediatelyFollowedBy(sequence)) {
            last.appendSequence(mutableSequence)
        } else {
            sequenceList.add(mutableSequence)
        }
    }

    /**
     * Append a sequence to a non-existing or previously appended data stream in this batch.
     *
     * no catch to throw "IllegalArgumentException" when the start of any sequences
     * contained in [batch] -> This is a temporary solution
     *
     * - the start of the [sequence] range precedes the end of a previously appended sequence to the same data stream
     * - the sync point of [sequence] is older than that of previous sequences in this batch
     */

    @Suppress("UNCHECKED_CAST")
    fun appendSequence(sequence: DataStreamSequence<*>) {
        LOGGER.info("Appending sequence: ${sequence.dataStream}")
        when (sequence) {
            is DataStreamSequence<*> -> {
                appendSequenceInternal(sequence as DataStreamSequence<*>)
                LOGGER.info("Sequence appended successfully.")
            }
            else -> {
                LOGGER.error("Unsupported sequence type.")
                throw IllegalArgumentException("Unsupported sequence type")
            }
        }
    }

    /**
     * Append all data stream sequences contained in [batch] to this batch.
     *
     * no catch to throw "IllegalArgumentException" when the start of any sequences contained in [batch]
     * precede the end of a previously appended sequence to the same data stream. --> This is a temporary solution
     */

    // assigning same logic based on Core, however sequence<> should work then >?<
    fun appendBatch(batch: DataStreamBatch, ) {
        batch.sequences.forEach { sequence ->
            appendSequence(sequence)
        }
    }

    override fun equals( other: Any? ): Boolean
    {
        if ( this === other ) return true
        if ( other !is DataStreamBatch) return false

        return toList() == other.toList()
    }

    override fun hashCode(): Int = sequences.hashCode()
}
