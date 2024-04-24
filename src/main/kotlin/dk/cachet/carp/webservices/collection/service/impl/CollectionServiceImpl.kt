package dk.cachet.carp.webservices.collection.service.impl

import com.fasterxml.jackson.databind.ObjectMapper
import cz.jirutka.rsql.parser.RSQLParser
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.collection.dto.CollectionCreateRequestDto
import dk.cachet.carp.webservices.collection.dto.CollectionUpdateRequestDto
import dk.cachet.carp.webservices.collection.repository.CollectionRepository
import dk.cachet.carp.webservices.collection.service.CollectionService
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.responses.AlreadyExistsException
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.common.query.QueryUtil
import dk.cachet.carp.webservices.common.query.QueryVisitor
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Claim
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * The Class [CollectionServiceImpl].
 * The [CollectionServiceImpl] provides implementation for [CollectionService] to create, invite,
 * delete and find collection for the given parameter requests.
 */
@Service
@Transactional
class CollectionServiceImpl
(
    private val collectionRepository: CollectionRepository,
    private val accountService: AccountService,
    private val authenticationService: AuthenticationService,
    private val validationMessages: MessageBase,
    private val objectMapper: ObjectMapper
): CollectionService
{
    private val backgroundWorker = CoroutineScope(Dispatchers.IO)

    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The function [delete] deletes the collection.
     *
     * @param studyId The [studyId] of the study.
     * @param id The [id] of the collection.
     */
    override fun delete(studyId: String, id: Int)
    {
        val collection = this.getCollectionByStudyIdAndId(studyId, id)
        collectionRepository.delete(collection)
        LOGGER.info("Collection deleted, id: $id")

        backgroundWorker.launch {
            val identity = authenticationService.getCarpIdentity()
            accountService.revoke( identity, setOf( Claim.CollectionOwner( collection.id ) ) )
        }
    }

    /**
     * The function [update] updates a collection [name] with the given parameters.
     *
     * @param studyId The [studyId] of the study.
     * @param id The [id] of the collection.
     * @return The updated [Collection] by its [studyId] and collection [id].
     */
    override fun update(studyId: String, id: Int, updateRequest: CollectionUpdateRequestDto): Collection
    {
        val collection = this.getCollectionByStudyIdAndId(studyId, id)
        collection.name = updateRequest.name
        LOGGER.info("Collection updated, studyId: $studyId and collectionId: $id")
        return collection
    }

    /**
     * The function [create] creates a new collection.
     *
     * @param request The [request] object containing the information needed to create a new collection.
     * @param studyId The [studyId] of the study.
     * @return The [Collection] created with the given [studyId] and [request] object.
     */
    override fun create(request: CollectionCreateRequestDto, studyId: String, deploymentId: String?): Collection
    {
        val collection = Collection()
        collection.name = request.name
        collection.studyId = studyId
        collection.studyDeploymentId = deploymentId
        val collectionByName = collectionRepository.findByStudyDeploymentIdAndName(collection.studyDeploymentId!!, collection.name)
        if (collectionByName.isPresent) {
            throw AlreadyExistsException(validationMessages.get("collection.already-exists", request.deploymentId!!, request.name))
        }
        val saved = collectionRepository.save(collection)
        LOGGER.info("Collection saved, id: ${saved.id}")

        backgroundWorker.launch {
            val identity = authenticationService.getCarpIdentity()
            accountService.grant( identity, setOf( Claim.CollectionOwner( saved.id ) ) )
        }

        return saved
    }

    /**
     * The function [getCollectionByStudyIdAndId] retrieves the collection with the given parameters.
     *
     * @param studyId The [studyId] of the study.
     * @param id The collection [id].
     * @throws ResourceNotFoundException when the [studyId] and  collection [id] are not found.
     * @return The [Collection] with the given [studyId] and collection [id].
     */
    override fun getCollectionByStudyIdAndId(studyId: String, id: Int): Collection
    {
        val collectionOp = collectionRepository.findCollectionByStudyIdAndId(studyId, id)
        if (!collectionOp.isPresent)
        {
            LOGGER.info("Collection not found, studyId: $studyId, collectionId: $id")
            throw ResourceNotFoundException(validationMessages.get("collection.studyId-and-collectionId.not_found", studyId, id))
        }
        val collection = collectionOp.get()
        objectMapper.writeValueAsString(collection)
        return collection
    }

    /**
     * The function [getCollectionByStudyIdAndByName] retrieves the collection with the given parameters.
     *
     * @param studyId The [studyId] of the study.
     * @param name The [name] of collection.
     * @throws ResourceNotFoundException when the [studyId] and collection [name] are not found.
     * @return The [Collection] with the given [studyId] and collection [name].
     */
    override fun getCollectionByStudyIdAndByName(studyId: String, name: String): Collection
    {
        val collectionOp = collectionRepository.findCollectionByStudyIdAndName(studyId, name)
        if (!collectionOp.isPresent)
        {
            LOGGER.warn("Collection not found, studyId: $studyId, collection name: $name")
            throw ResourceNotFoundException(validationMessages.get("collection.studyId-and-collectionName.not_found", studyId, name))
        }
        val collection = collectionOp.get()
        objectMapper.writeValueAsString(collection)
        return collection
    }

    /**
     * The function [getAll] retrieves all collections with the given [query] parameters.
     *
     * @param studyId The [studyId] of the study.
     * @param query The [query] parameter of the request.
     * @return The [Collection] with the given [studyId] and [query] parameters.
     */
    override fun getAll(studyId: String, query: String?): List<Collection>
    {
        val validatedQuery = query?.let { QueryUtil.validateQuery(it) }
        val nestedQuery = "$validatedQuery;study_id==$studyId"
        val specification = RSQLParser().parse(nestedQuery).accept(QueryVisitor<Collection>())
        return collectionRepository.findAll(specification)
    }

    /**
     * The function [getAll] retrieves all collections with the given [studyId] parameter.
     *
     * @param studyId The [studyId] of the study.
     * @return The [Collection] with the given [studyId]
     */
    override fun getAll(studyId: String): List<Collection> {
        return collectionRepository.findAllByStudyId(studyId)
    }

    /**
     * The function [getAllByStudyIdAndDeploymentId] retrieves all collections with the given [studyId] and [deploymentId] parameters.
     *
     * @param studyId The [studyId] of the study.
     * @param deploymentId The [deploymentId] of the study.
     * @return The [Collection] with the given [studyId] and [deploymentId] parameters.
     */
    override fun getAllByStudyIdAndDeploymentId(studyId: String, deploymentId: String): List<Collection>
    {
        return collectionRepository.findAllByStudyIdAndDeploymentId(studyId, deploymentId)
    }
}