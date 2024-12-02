package dk.cachet.carp.webservices.datastream.repository

import dk.cachet.carp.webservices.datastream.domain.DataStreamSequence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
interface DataStreamSequenceRepository : JpaRepository<DataStreamSequence, Int> {
    @Query(
        nativeQuery = true,
        value =
            "SELECT * FROM data_stream_sequence " +
                "WHERE data_stream_id = :dataStreamId " +
                "AND ( (first_sequence_id <= :from AND last_sequence_id >= :from) " +
                "OR (last_sequence_id <= :to AND last_sequence_id >= :from) " +
                "OR (first_sequence_id <= :to AND first_sequence_id >= :from) )",
    )
    fun findAllBySequenceIdRange(
        @Param("dataStreamId") dataStreamId: Int,
        @Param("from") from: Long,
        @Param("to") to: Long,
    ): List<DataStreamSequence>

    @Query(
        nativeQuery = true,
        value =
            "SELECT * FROM data_stream_sequence " +
                "WHERE data_stream_id = :dataStreamId ",
    )
    fun findAllBySequenceId(
        @Param("dataStreamId") dataStreamId: Int,
    ): List<DataStreamSequence>

    @Modifying
    @Transactional
    @Query(
        nativeQuery = true,
        value = "DELETE FROM data_stream_sequence WHERE data_stream_id IN (:ids)",
    )
    fun deleteAllByDataStreamIds(
        @Param(value = "ids") ids: Collection<Int>,
    )

    @Query(
        nativeQuery = true,
        value = "SELECT * FROM data_stream_sequence WHERE data_stream_id IN (:dataStreamIds)",
    )
    fun findAllByDataStreamIds(
        @Param("dataStreamIds") dataStreamIds: Collection<Int>,
    ): List<DataStreamSequence>

    @Query(
        """
    SELECT MAX(dss.updatedAt) 
    FROM data_stream_sequence dss
    WHERE dss.dataStreamId IN :dataStreamIds
    """,
    )
    fun findMaxUpdatedAtByDataStreamIds(
        @Param("dataStreamIds") dataStreamIds: List<Int>,
    ): Instant?
}
