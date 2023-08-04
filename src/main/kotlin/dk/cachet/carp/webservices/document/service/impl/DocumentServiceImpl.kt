package dk.cachet.carp.webservices.document.service.impl

import com.fasterxml.jackson.databind.JsonNode
import cz.jirutka.rsql.parser.RSQLParser
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.responses.AlreadyExistsException
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.common.query.QueryUtil
import dk.cachet.carp.webservices.common.query.QueryVisitor
import dk.cachet.carp.webservices.document.authorizer.DocumentAuthorizationService
import dk.cachet.carp.webservices.document.domain.Document
import dk.cachet.carp.webservices.document.dto.CreateDocumentRequestDto
import dk.cachet.carp.webservices.document.dto.UpdateDocumentRequestDto
import dk.cachet.carp.webservices.document.filter.DocumentSpecification
import dk.cachet.carp.webservices.document.repository.DocumentRepository
import dk.cachet.carp.webservices.document.service.IDocumentService
import dk.cachet.carp.webservices.security.authentication.service.impl.AuthenticationServiceImpl
import jakarta.servlet.http.HttpServletRequest
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DocumentServiceImpl(
    private val documentRepository: DocumentRepository,
    private val documentTraverser: DocumentTraverser,
    private val validationMessages: MessageBase,
    private val authenticationService: AuthenticationServiceImpl,
    private val authorizationService: DocumentAuthorizationService
): IDocumentService
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun getAll(pageRequest: PageRequest, query: String?, studyId: String): List<Document>
    {
        try {
            val isAccountResearcher = authorizationService.isAccountResearcher()
            val belongsToStudySpec = DocumentSpecification.belongsToStudyId(studyId)
            val accountId = authenticationService.getCurrentPrincipal().id!!
            val belongsToUserSpec = DocumentSpecification.belongsToUserAccountId(accountId)

            val validatedQuery = query?.let { QueryUtil.validateQuery(it) }

            validatedQuery?.let {
                val queryForRole = if (!isAccountResearcher) {
                    // Return data relevant to this user only.
                    "$validatedQuery;created_by==${accountId}"
                } else validatedQuery

                val specification = RSQLParser()
                    .parse(queryForRole)
                    .accept(QueryVisitor<Document>())

                return documentRepository.findAll(specification.and(belongsToStudySpec), pageRequest).content
            }

            if (isAccountResearcher) {
                return documentRepository.findAll(belongsToStudySpec, pageRequest).content
            }

            return documentRepository.findAll(belongsToStudySpec.and(belongsToUserSpec), pageRequest).content
        } catch (e :Exception) {
            LOGGER.error("Error occurred when invoking get all documents for study id: $studyId", e)
            throw e
        }
    }

    override fun getAll(collectionIds: List<Int>): List<Document> {
        return documentRepository.findAllByCollectionsIdsWithDocuments(collectionIds)
    }

    override fun getByDocumentPath(studyId: String, request: HttpServletRequest): String?
    {
        return documentTraverser.getAllFromDocumentPath(request, studyId, null)
    }

    override fun createByDocumentPath(studyId: String, data: JsonNode?, request: HttpServletRequest): String?
    {
        val saved = documentTraverser.createAllFromDocumentPath(request, studyId, data, null)
        LOGGER.info("Document saved, path: $saved")
        return saved
    }

    override fun getOne(id: Int): Document
    {
        try {
            val optionalDocument = documentRepository.findById(id)
            if (!optionalDocument.isPresent)
            {
                LOGGER.warn("Document is not found, id: $id")
                throw ResourceNotFoundException(validationMessages.get("document.id.not_found", id))
            }
            return optionalDocument.get()
        } catch (e: Exception) {
            LOGGER.error("Error occurred when invoking get one document with id: $id", e)
            throw e
        }
    }

    // FIXME: saving document without validating its relationships? e.g. collection
    /**
     *  The function [create] creates a new document with the given [request] object.
     *  @param request The [request] containing the document object request.
     *  @return The saved [Document] object.
     */
    override fun create(request: CreateDocumentRequestDto): Document
    {
        val document = mapCreateDocumentRequestToDocument(request)
        val documentByName = documentRepository.findByNameAndCollectionId(request.name, document.collectionId!!)
        if (documentByName.isPresent) {
            throw AlreadyExistsException(validationMessages.get("document.already_exists", request.collectionId!!, request.name))
        }
        val saved = documentRepository.save(document)
        LOGGER.info("Document saved, id: ${saved.id}")
        return saved
    }

    override fun delete(id: Int)
    {
        val document = getOne(id)
        documentRepository.delete(document)
        LOGGER.info("Document deleted, id: $id")
    }

    override fun update(id: Int, updateRequest: UpdateDocumentRequestDto): Document
    {
        val updated = documentRepository.update(id, updateRequest)
        if (!updated.isPresent)
        {
            LOGGER.warn("Document is not found, id: $id")
            throw ResourceNotFoundException(validationMessages.get("document.id.not_found", id))
        }
        return updated.get()
    }

    override fun append(id: Int, updateRequest: UpdateDocumentRequestDto): Document
    {
        val updated = documentRepository.appendDocument(id, updateRequest)
        if (!updated.isPresent)
        {
            LOGGER.warn("Document is not found, id: $id")
            throw ResourceNotFoundException(validationMessages.get("document.id.not_found", id))
        }
        return updated.get()
    }

    private fun mapCreateDocumentRequestToDocument(request: CreateDocumentRequestDto) = Document().apply {
        name = request.name
        collectionId = request.collectionId
        collections = request.collections
        data = request.data
    }
}