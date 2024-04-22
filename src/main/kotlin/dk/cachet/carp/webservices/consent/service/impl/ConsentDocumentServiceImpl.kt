package dk.cachet.carp.webservices.consent.service.impl

import com.fasterxml.jackson.databind.JsonNode
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.consent.domain.ConsentDocument
import dk.cachet.carp.webservices.consent.repository.ConsentDocumentRepository
import dk.cachet.carp.webservices.consent.service.ConsentDocumentService
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Claim
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * The Class [ConsentDocumentServiceImpl].
 * The [ConsentDocumentServiceImpl] provides implementation to create, retrieve, and delete a consent documents.
 */
@Service
@Transactional
class ConsentDocumentServiceImpl(
    private val consentDocumentRepository: ConsentDocumentRepository,
    private val accountService: AccountService,
    private val authenticationService: AuthenticationService,
    private val validationMessages: MessageBase
): ConsentDocumentService
{
    private val backgroundWorker = CoroutineScope(Dispatchers.IO)
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * The function [getAll] retrieves all consent documents for the given [deploymentId].
     *
     * @param deploymentId The [deploymentId] of the study.
     * @return [ConsentDocument] consent document list.
     */
    override fun getAll(deploymentId: String): List<ConsentDocument>
    {
        return consentDocumentRepository.findByDeploymentId(deploymentId)
    }

    /**
     * The function [getAll] retrieves all consent documents for several [deploymentIds].
     *
     * @param deploymentIds ID's of deployments;
     * @return [ConsentDocument] consent document list.
     */
    override fun getAll(deploymentIds: List<String>): List<ConsentDocument>
    {
        return consentDocumentRepository.findAllByDeploymentIds(deploymentIds)
    }

    /**
     * The function [getOne] retrieves one consent document for the given consent document [id].
     *
     * @param id The [id] of the consent document.
     * @return [ConsentDocument] consent document.
     */
    override fun getOne(id: Int): ConsentDocument
    {
        val optionalConsent = consentDocumentRepository.findById(id)
        if (!optionalConsent.isPresent)
        {
            LOGGER.info("Consent document is not found, id: $id")
            throw ResourceNotFoundException(validationMessages.get("consent.document.id.not_found", id))
        }
        return optionalConsent.get()
    }

    /**
     * The function [delete] deletes the consent document with the given consent [id].
     *
     * @param id The [id] of the consent document to be deleted.
     */
    override fun delete(id: Int)
    {
        val consent = getOne(id)
        consentDocumentRepository.delete(consent)
        LOGGER.info("Consent document deleted, id: ${consent.id}")

        backgroundWorker.launch {
            val identity = authenticationService.getCarpIdentity()
            accountService.revoke(identity, setOf( Claim.ConsentOwner( consent.id ) ) )
        }
    }

    /**
     * The function [create] creates a new consent document.
     *
     * @param deploymentId The [deploymentId] of the study.
     * @param data The [data] object containing the consent information.
     * @return [ConsentDocument] consent document.
     */
    override fun create(deploymentId: String, data: JsonNode?): ConsentDocument
    {
        val saved = consentDocumentRepository.save(ConsentDocument(deploymentId = deploymentId, data = data))
        LOGGER.info("Consent document created, id: ${saved.id}")

        backgroundWorker.launch {
            val identity = authenticationService.getCarpIdentity()
            accountService.grant( identity, setOf( Claim.ConsentOwner( saved.id ) ) )
        }
        return saved
    }
}