package dk.cachet.carp.webservices.data.service.impl

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.data.application.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/** UNDER CONSTRUCTION --> CHECK create a function that checks sequence IDs before appending
 * + usable if we can get back last preceding upload of a sequence so the client know what to send
 * This a "trick" to make the class `DataStreamBatch` open for allowing appendDataStream without checking
 * all the sequence IDs in the append method. This is a temporary solution until we find a better way to handle this.
 */
@Suppress("UNUSED_EXPRESSION")
class CawsMutableDataStreamBatchWrapper : Sequence<DataStreamPoint<*>>, DataStreamBatch {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    private val sequenceMap: MutableMap<DataStreamId, MutableList<MutableDataStreamSequence<*>>> = mutableMapOf()

    /**
     * A list of sequences covering all sequences so far appended to [CawsMutableDataStreamBatchWrapper].
     * This may return less sequences than originally appended in case appended sequences were merged with prior ones.
     */
    override val sequences: Sequence<DataStreamSequence<*>>
        get() = sequenceMap.asSequence().flatMap { it.value }.map { it }

    /**
     * Consider val for sequence.toMutableDataStreamSequence(), but not nece
     */
    @Suppress("UNCHECKED_CAST")
    private fun appendSequence(sequence: DataStreamSequence<*>) {
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
     * Append a sequence to a non-existing or previously appended data stream in this batch.
     *
     * no catch to throw "IllegalArgumentException" when the start of any sequences
     * contained in [batch] -> This is a temporary solution
     *
     * - the start of the [sequence] range precedes the end of a previously appended sequence to the same data stream
     * - the sync point of [sequence] is older than that of previous sequences in this batch
     */

    fun sequenceTypeCheck(sequence: DataStreamSequence<*>): Boolean {
        LOGGER.info("Appending sequence: ${sequence.dataStream}...")
        return when (sequence) {
            else -> {
                appendSequence(sequence)
                LOGGER.info("Sequence appended successfully.")
                true
            }
        }
    }

    /**
     * Append all data stream sequences contained in [batch] to this batch.
     *
     * no catch to throw "IllegalArgumentException" when the start of any sequences contained in [batch]
     * precede the end of a previously appended sequence to the same data stream. --> This is a temporary solution
     */

    fun appendBatch(batch: DataStreamBatch) {
        batch.sequences.forEach { sequence ->
            LOGGER.info("Attempting to append sequence: ${sequence.dataStream}")
            require(sequenceTypeCheck(sequence)) {
                LOGGER.error("Failed to append sequence: ${sequence.dataStream}")
            }
            LOGGER.info("Sequence appended successfully: ${sequence.dataStream}")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataStreamBatch) return false

        return toList() == other.toList()
    }

    override fun hashCode(): Int = sequences.hashCode()
}
