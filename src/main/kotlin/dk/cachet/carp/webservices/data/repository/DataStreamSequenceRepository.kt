package dk.cachet.carp.webservices.data.repository

import dk.cachet.carp.webservices.data.domain.DataStreamSequence
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

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
        @Param("from") from: Int,
        @Param("to") to: Int,
    ): List<DataStreamSequence>

    @Query(
        nativeQuery = true,
        value =
            "SELECT dsi.id, ds.data_stream_id, ds.snapshot, ds.first_sequence_id, " +
                "ds.last_sequence_id, ds.updated_at, ds.created_at, ds.created_by, ds.updated_by \n" +
                "FROM data_stream_sequence ds\n" +
                "LEFT JOIN data_stream_ids dsi ON ds.data_stream_id = dsi.id\n" +
                "WHERE dsi.study_deployment_id IN (:deploymentIds)",
    )
    fun findAllByDeploymentIds(
        @Param("deploymentIds") deploymentIds: List<String>,
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
        value = "SELECT * FROM data_stream_sequence WHERE data_stream_id = :dataStreamId",
    )
    fun findAllByDataStreamId(
        @Param("dataStreamId") dataStreamId: Int,
    ): List<DataStreamSequence>
}
