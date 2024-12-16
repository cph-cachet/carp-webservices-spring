package dk.cachet.carp.webservices.datastream.repository

import dk.cachet.carp.webservices.datastream.domain.DataStreamId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface DataStreamIdRepository : JpaRepository<DataStreamId, Int> {
    fun findByStudyDeploymentIdAndDeviceRoleNameAndNameAndNameSpace(
        studyDeploymentId: String,
        deviceRoleName: String,
        name: String,
        nameSpace: String,
    ): Optional<DataStreamId>

    @Modifying
    @Transactional
    @Query(
        nativeQuery = true,
        value = "DELETE FROM data_stream_ids WHERE id IN (:ids)",
    )
    fun deleteAllByDataStreamIds(
        @Param(value = "ids") ids: Collection<Int>,
    )

    @Modifying
    @Transactional
    @Query(
        nativeQuery = true,
        value = "DELETE FROM data_stream_ids WHERE study_deployment_id IN (:deploymentIds)",
    )
    fun deleteAllByDeploymentIds(
        @Param(value = "deploymentIds") deploymentIds: Collection<String>,
    )

    @Query(
        nativeQuery = true,
        value = "SELECT id FROM data_stream_ids WHERE study_deployment_id IN (:deploymentIds)",
    )
    fun getAllByDeploymentIds(
        @Param("deploymentIds") ids: Collection<String>,
    ): List<Int>

    @Query(
        nativeQuery = true,
        value = "SELECT * FROM data_stream_ids WHERE study_deployment_id = :deploymentId ",
    )
    fun getAllByDeploymentId(
        @Param("deploymentId") id: String,
    ): List<DataStreamId>

    @Query(
        nativeQuery = true,
        value = "SELECT * FROM data_stream_ids WHERE id = :ids ",
    )
    fun findByDataStreamId(
        @Param("ids") ids: Int,
    ): DataStreamId?
}
