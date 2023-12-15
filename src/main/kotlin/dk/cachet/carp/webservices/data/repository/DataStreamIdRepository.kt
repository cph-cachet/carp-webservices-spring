package dk.cachet.carp.webservices.data.repository

import dk.cachet.carp.webservices.data.domain.DataStreamId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface DataStreamIdRepository: JpaRepository<DataStreamId, Int>
{
    fun findByStudyDeploymentIdAndDeviceRoleNameAndNameAndNameSpace(
        studyDeploymentId: String,
        deviceRoleName: String,
        name: String,
        nameSpace: String
    ): Optional<DataStreamId>

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "DELETE FROM data_stream_ids WHERE id IN (:ids)")
    fun deleteAllByDataStreamIds(@Param(value = "ids") ids: Collection<Int>)

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "DELETE FROM data_stream_ids WHERE study_deployment_id IN (:deploymentIds)")
    fun deleteAllByDeploymentIds(@Param(value = "deploymentIds") deploymentIds: Collection<String>)

    @Query(nativeQuery = true,
        value = "SELECT id FROM data_stream_ids WHERE study_deployment_id IN (:deploymentIds)")
    fun getAllByDeploymentIds(@Param("deploymentIds") ids: Collection<String>): List<Int>

    @Query(nativeQuery = true,
            value = "SELECT * FROM data_stream_ids WHERE study_deployment_id = :deploymentId ")
    /*fun getAllByDeploymentIds(@Param("deploymentIds") ids: String): Collection<String>*/
    fun getAllByDeploymentId(@Param("deploymentId") id: String, pageable: Pageable): Page<DataStreamId>
    
/*    fun findByDeploymentId(studyDeploymentId: String, pageable: Pageable): Page<DataStreamId>
    @Query(nativeQuery = true,
        value = "SELECT id FROM data_stream_ids where studyDeploymentId")*/

/*    @Query(nativeQuery = true,
        value = "SELECT up FROM data_stream_ids WHERE study_deployment_id IN (:deploymentIds)")
    fun getAllByDeploymentIds(@Param("deploymentIds") ids: Collection<String>): List<Int>*/

}