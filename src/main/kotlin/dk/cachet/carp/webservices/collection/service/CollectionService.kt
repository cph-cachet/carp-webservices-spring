package dk.cachet.carp.webservices.collection.service

import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.collection.dto.CollectionCreateRequestDto
import dk.cachet.carp.webservices.collection.dto.CollectionUpdateRequestDto

/**
 * The Interface [CollectionService].
 * The [CollectionService] creates the interfaces for handling collections with the given parameters.
 */
interface CollectionService
{
    /** The [delete] interface for deleting a collection. */
    fun delete(studyId: String, id: Int)

    /** The [update] interface for updating an existing collection. */
    fun update(studyId: String, id: Int, updateRequest: CollectionUpdateRequestDto): Collection

    /** The [create] interface for creating a new collection. */
    fun create(request: CollectionCreateRequestDto, studyId: String, deploymentId: String?): Collection

    /** The [getCollectionByStudyIdAndId] interface for retrieving a collection by id. */
    fun getCollectionByStudyIdAndId(studyId: String, id: Int): Collection

    /** The [getCollectionByStudyIdAndByName] interface for retrieving a collection by name. */
    fun getCollectionByStudyIdAndByName(studyId: String, name: String): Collection

    /** The [getAll] interface for retrieving a collection by name. */
    fun getAll(studyId: String, query: String?): List<Collection>

    /** The [getAll] interface for retrieving all collection for a given study. */
    fun getAll(studyId: String): List<Collection>

    /** The [getAllByStudyIdAndDeploymentId] interface for retrieving all collection for a given study and deploymentId. */
    fun getAllByStudyIdAndDeploymentId(studyId: String, deploymentId: String): List<Collection>
}