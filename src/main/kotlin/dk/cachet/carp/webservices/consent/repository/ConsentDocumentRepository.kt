package dk.cachet.carp.webservices.consent.repository

import dk.cachet.carp.webservices.consent.domain.ConsentDocument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface ConsentDocumentRepository : JpaRepository<ConsentDocument, Int> {
    fun findByDeploymentId(deploymentId: String): List<ConsentDocument>

    @Query(value = "SELECT cd FROM consent_documents cd WHERE cd.deploymentId IN :deploymentIds")
    fun findAllByDeploymentIds(
        @Param("deploymentIds") deploymentIds: Collection<String>,
    ): List<ConsentDocument>

    @Query(
        value = "SELECT * FROM consent_documents cd WHERE cd.deploymentId IN :deployumentIds AND data->>'participantId' = ?1",
        nativeQuery = true,
    )
    fun findAllByDeploymentIdsAndParticipantId(
        @Param("deploymentIds") deploymentIds: Collection<String>,
        @Param("participantId")participantId: String,
    ): List<ConsentDocument>

    @Modifying
    @Transactional
    @Query(
        nativeQuery = true,
        value = "DELETE FROM consent_documents WHERE deployment_id IN (:deploymentIds)",
    )
    fun deleteAllByDeploymentIds(
        @Param(value = "deploymentIds") deploymentIds: Collection<String>,
    )
}
