package dk.cachet.carp.webservices.data.repository

import dk.cachet.carp.webservices.data.domain.DataStreamConfiguration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface DataStreamConfigurationRepository : JpaRepository<DataStreamConfiguration, String> {
    @Query(
        nativeQuery = true,
        value = "SELECT * FROM data_stream_configurations WHERE study_deployment_id IN :ids",
    )
    fun getConfigurationsForIds(
        @Param("ids") ids: Collection<String>,
    ): List<DataStreamConfiguration>

    @Modifying
    @Transactional
    @Query(
        nativeQuery = true,
        value = "DELETE FROM data_stream_configurations WHERE study_deployment_id IN (:deploymentIds)",
    )
    fun deleteAllByDeploymentIds(
        @Param(value = "deploymentIds") deploymentIds: Collection<String>,
    )
}
