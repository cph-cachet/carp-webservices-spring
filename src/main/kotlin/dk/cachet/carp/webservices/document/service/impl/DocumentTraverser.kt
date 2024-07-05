package dk.cachet.carp.webservices.document.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.collection.repository.CollectionRepository
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.responses.AlreadyExistsException
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.document.domain.Document
import dk.cachet.carp.webservices.document.repository.DocumentRepository
import jakarta.servlet.http.HttpServletRequest
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.web.servlet.HandlerMapping
import java.util.*

/** The Data Class [CollectionWrapper]. */
data class CollectionWrapper(
    val id: Int?,
    val name: String,
    val type: CollectionWrapperType,
    val scopeToUserAccountId: String?,
)

/** The Data Class [DocumentPath]. */
data class DocumentPath(val collections: List<CollectionWrapper>, val documents: List<CollectionWrapper>)

/** The Enum Class [DocumentPathException]. */
enum class CollectionWrapperType { Document, Collection }

/**
 * The Class [DocumentPathException].
 * The [DocumentPathException] handles exceptions thrown when the document cannot create path from request to URL path.
 */
class DocumentPathException(message: String) : Exception(message)

/**
 * The Service Class [DocumentTraverser].
 * The [DocumentTraverser] implements the [DocumentRepository] and [CollectionRepository] interfaces
 * to create, retrieve, and delete documents.
 */
@Service
class DocumentTraverser(
    private val documentRepository: DocumentRepository,
    private val collectionRepository: CollectionRepository,
    private val validationMessages: MessageBase,
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * Takes a request object with a path structure: collections/{collectionName}/{documentName}/.../
     * and builds the entire representation in the database, passing back either a
     * Collection or Document object with the corresponding data payload.
     *
     * @param request The request to split into collection/document parts.
     * @param studyId The studyId to store the objects against.
     * @param documentData The json payload (if supplied) to populate the final document in the path.
     * @param scopeToUserId (Optional) Scope the request to the author of the document.
     * @throws DocumentPathException when the document cannot create path from [request] to URL path.
     * @return The created Json object (Collection/Document), or null if the path was invalid.
     */
    @Suppress("LongMethod")
    fun createAllFromDocumentPath(
        request: HttpServletRequest,
        studyId: String,
        documentData: JsonNode?,
        scopeToUserId: String?,
    ): String? {
        val pathList = requestToPathList(request, scopeToUserId)

        var collectionWrapper: CollectionWrapper? = null

        if (pathList.collections.isEmpty()) {
            LOGGER.warn("Cannot create path from: ${requestToUrlPath(request)}")
            throw DocumentPathException(validationMessages.get("document.create_all.empty", requestToUrlPath(request)))
        }

        pathList.collections.forEachIndexed { index, wrappedCollection ->

            val collectionQuery =
                collectionRepository.findByNameAndStudyIdAndDocumentId(
                    wrappedCollection.name,
                    studyId,
                    collectionWrapper?.id,
                )

            // If there's no collection to store this document in, create it, referencing a parent document if required.
            val collection =
                collectionQuery.orElseGet {
                    collectionRepository.save(
                        Collection(
                            name = wrappedCollection.name,
                            studyId = studyId,
                            documentId = collectionWrapper?.id,
                        ),
                    )
                }

            // If we're not creating a document, return the Collection object.
            if (index >= pathList.documents.size) {
                collectionWrapper =
                    CollectionWrapper(
                        collection.id,
                        collection.name,
                        CollectionWrapperType.Collection,
                        scopeToUserId,
                    )
            } else {
                val documentQuery =
                    documentRepository.findByNameAndCollectionId(
                        pathList.documents[index].name,
                        collection.id,
                    )

                val data = if (pathList.collections.lastIndex == index) documentData else null

                if (documentQuery.isPresent) {
                    throw AlreadyExistsException(
                        validationMessages.get(
                            "document.already_exists",
                            collection.id,
                            pathList.documents[index].name,
                        ),
                    )
                }

                val document =
                    documentRepository.save(
                        Document(
                            name = pathList.documents[index].name,
                            collectionId = collection.id,
                            data = data,
                        ),
                    )

                collectionWrapper =
                    CollectionWrapper(document.id, document.name, CollectionWrapperType.Document, scopeToUserId)
            }
        }

        return toJson(collectionWrapper)
    }

    /**
     * Takes a request object with a path structure: collections/{collectionName}/{documentName}/.../
     * and returns the Document or Collection if it exists within that path.
     *
     * @param request The request to explode into collection/document parts.
     * @param studyId The studyId to store the objects against.
     * @param scopeToUserId (Optional) Scope the request to the author of the document.
     *
     * @return The Collection or Document if it exists.
     */
    fun getAllFromDocumentPath(
        request: HttpServletRequest,
        studyId: String,
        scopeToUserId: String?,
    ): String? {
        val pathList = requestToPathList(request, scopeToUserId)

        var collectionWrapper: CollectionWrapper? = null

        pathList.collections.forEachIndexed { index, wrappedCollection ->

            val collection =
                collectionRepository.findByNameAndStudyIdAndDocumentId(
                    wrappedCollection.name,
                    studyId,
                    collectionWrapper?.id,
                )
                    .orElseThrow {
                        ResourceNotFoundException(
                            validationMessages.get(
                                "document.get_with_all_params.not_found",
                                studyId,
                                wrappedCollection.name,
                                collectionWrapper?.id!!,
                            ),
                        )
                    }

            if (index >= pathList.documents.size) {
                collectionWrapper =
                    CollectionWrapper(
                        collection.id,
                        collection.name,
                        CollectionWrapperType.Collection,
                        scopeToUserId,
                    )
            } else {
                val document =
                    documentRepository.findByNameAndCollectionId(
                        pathList.documents[index].name,
                        collection.id,
                    ).orElseThrow {
                        ResourceNotFoundException(
                            validationMessages.get(
                                "document.get_params.not_found",
                                pathList.documents[index].name,
                                collection.id,
                            ),
                        )
                    }

                collectionWrapper =
                    CollectionWrapper(document.id, document.name, CollectionWrapperType.Document, scopeToUserId)
            }
        }

        return toJson(collectionWrapper)
    }

    /**
     * The [requestToUrlPath] function converts a request object to a URL path.
     *
     * @param request The [request] object.
     * @return The converted request object to URL path.
     */
    fun requestToUrlPath(request: HttpServletRequest): String {
        return request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE) as String
    }

    private fun requestToPathList(
        request: HttpServletRequest,
        scopeToUserId: String?,
    ): DocumentPath {
        val urlPath = requestToUrlPath(request)

        val documentPath = "(?<=collections\\/)(.*)".toRegex().find(urlPath)?.value?.split("/")!!

        val collections =
            documentPath
                .filterIndexed { index, _ -> index % 2 == 0 }
                .filter { value -> value.isNotBlank() }
                .map { collectionName ->
                    CollectionWrapper(
                        null,
                        collectionName.lowercase(Locale.getDefault()),
                        CollectionWrapperType.Collection,
                        scopeToUserId,
                    )
                }

        val documents =
            documentPath
                .filterIndexed { index, _ -> index % 2 != 0 }
                .filter { value -> value.isNotBlank() }
                .map { documentName ->
                    CollectionWrapper(
                        null,
                        documentName.lowercase(Locale.getDefault()),
                        CollectionWrapperType.Document,
                        scopeToUserId,
                    )
                }

        return DocumentPath(collections = collections, documents = documents)
    }

    private fun toJson(collectionWrapper: CollectionWrapper?): String? {
        if (collectionWrapper === null) return null

        val mapper = ObjectMapper()

        mapper.registerModule(JavaTimeModule())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

        return if (collectionWrapper.type === CollectionWrapperType.Collection) {
            val outerCollection = collectionRepository.findById(collectionWrapper.id!!)

            mapper.writeValueAsString(
                outerCollection
                    .map { collection: Collection ->
                        if (collectionWrapper.scopeToUserAccountId != null) {
                            // Scope documents to the specified user.
                            collection.documents =
                                collection.documents?.filter { document ->
                                    document.createdBy == collectionWrapper.scopeToUserAccountId
                                }

                            collection
                        } else {
                            collection
                        }
                    }.get(),
            )
        } else {
            val document =
                documentRepository
                    .findById(collectionWrapper.id!!)
                    .get()
                    .takeIf { document ->
                        if (collectionWrapper.scopeToUserAccountId != null) {
                            // Scope document to the specified user.
                            document.createdBy == collectionWrapper.scopeToUserAccountId
                        } else {
                            true
                        }
                    }

            if (document != null) {
                mapper.writeValueAsString(document)
            } else {
                // If the user's role doesn't grant them access to the document.
                LOGGER.warn("Current account is not granted to access the document = $document.")
                throw ResourceNotFoundException(validationMessages.get("document.access.not_granted", document?.name!!))
            }
        }
    }
}
