package dk.cachet.carp.webservices.collection.service

import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.collection.dto.CollectionCreateRequestDto
import dk.cachet.carp.webservices.collection.dto.CollectionUpdateRequestDto

interface CollectionService {
    fun delete(
        studyId: String,
        id: Int,
    )

    fun update(
        studyId: String,
        id: Int,
        updateRequest: CollectionUpdateRequestDto,
    ): Collection

    fun create(
        request: CollectionCreateRequestDto,
        studyId: String,
        deploymentId: String?,
    ): Collection

    fun getCollectionByStudyIdAndId(
        studyId: String,
        id: Int,
    ): Collection

    fun getCollectionByStudyIdAndByName(
        studyId: String,
        name: String,
    ): Collection

    fun getAll(
        studyId: String,
        query: String?,
    ): List<Collection>

    fun getAll(studyId: String): List<Collection>

    fun getAllByStudyIdAndDeploymentId(
        studyId: String,
        deploymentId: String,
    ): List<Collection>
}
