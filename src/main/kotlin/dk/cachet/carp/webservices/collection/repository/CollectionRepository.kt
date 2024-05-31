package dk.cachet.carp.webservices.collection.repository

import dk.cachet.carp.webservices.collection.domain.Collection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface CollectionRepository : JpaSpecificationExecutor<Collection>, JpaRepository<Collection, Int> {
    fun findByNameAndStudyIdAndDocumentId(
        name: String,
        studyId: String,
        documentId: Int?,
    ): Optional<Collection>

    fun findCollectionByStudyIdAndId(
        studyId: String,
        id: Int,
    ): Optional<Collection>

    fun findByStudyDeploymentIdAndName(
        studyDeploymentId: String,
        name: String,
    ): Optional<Collection>

    fun findCollectionByStudyIdAndName(
        studyId: String,
        name: String,
    ): Optional<Collection>

    fun findCollectionByName(name: String): Optional<Collection>

    fun findAllByStudyId(studyId: String): List<Collection>

    @Query(
        nativeQuery = true,
        value = "SELECT id FROM collections WHERE study_id = :studyId",
    )
    fun getCollectionIdsByStudyId(
        @Param("studyId") studyId: String,
    ): List<Int>

    @Query(
        nativeQuery = true,
        value = "SELECT * FROM collections WHERE study_id = :studyId and study_deployment_id = :deploymentId",
    )
    fun findAllByStudyIdAndDeploymentId(
        @Param(value = "studyId") studyId: String,
        @Param(value = "deploymentId") deploymentId: String,
    ): List<Collection>

    @Modifying
    @Transactional
    @Query(
        nativeQuery = true,
        value = "DELETE FROM collections WHERE study_deployment_id IN (:deploymentIds)",
    )
    fun deleteAllByDeploymentIds(
        @Param(value = "deploymentIds") deploymentIds: kotlin.collections.Collection<String>,
    )
}
