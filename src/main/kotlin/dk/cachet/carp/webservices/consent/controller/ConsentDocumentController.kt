package dk.cachet.carp.webservices.consent.controller

import com.fasterxml.jackson.databind.JsonNode
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.consent.controller.ConsentDocumentController.Companion.CONSENT_DOCUMENT_BASE
import dk.cachet.carp.webservices.consent.domain.ConsentDocument
import dk.cachet.carp.webservices.consent.service.IConsentDocumentService
import io.swagger.v3.oas.annotations.Operation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = [CONSENT_DOCUMENT_BASE])
class ConsentDocumentController(private val consentDocumentService: IConsentDocumentService)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val CONSENT_DOCUMENT_BASE = "/api/deployments/{${PathVariableName.DEPLOYMENT_ID}}/consent-documents"
        const val CONSENT_BY_ID = "/{${PathVariableName.CONSENT_ID}}"
    }

    @GetMapping
    @PreAuthorize("@consentDocumentAuthorizationService.canViewAllConsentDocuments(#deploymentId)")
    @Operation(tags = ["consentDocument/getAll.json"])
    fun getAll(@PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: String): List<ConsentDocument>
    {
        LOGGER.info("Start GET: /api/deployments/$deploymentId/consent-documents")
        return consentDocumentService.getAll(deploymentId)
    }

    @GetMapping(CONSENT_BY_ID)
    @PreAuthorize("@consentDocumentAuthorizationService.canViewConsentDocument(#deploymentId, #consentId)")
    @Operation(tags = ["consentDocument/getOne.json"])
    fun getOne(
            @PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: String,
            @PathVariable(PathVariableName.CONSENT_ID) consentId: Int): ConsentDocument
    {
        LOGGER.info("Start GET: /api/deployments/$deploymentId/consent-documents/$consentId")
        return consentDocumentService.getOne(consentId)
    }

    @PostMapping
    @PreAuthorize("@consentDocumentAuthorizationService.canCreateConsentDocument(#deploymentId)")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(tags = ["consentDocument/create.json"])
    fun create(
            @PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: String,
            @RequestBody data: JsonNode?): ConsentDocument
    {
        LOGGER.info("Start POST: /api/deployments/$deploymentId/consent-documents")
        return consentDocumentService.create(deploymentId, data)
    }

    @DeleteMapping(CONSENT_BY_ID)
    @PreAuthorize("@consentDocumentAuthorizationService.canViewConsentDocument(#deploymentId, #consentId)")
    @Operation(tags = ["consentDocument/delete.json"])
    fun delete(
            @PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: String,
            @PathVariable(PathVariableName.CONSENT_ID) consentId: Int)
    {
        LOGGER.info("Start DELETE: /api/deployments/$deploymentId/consent-documents/$consentId")
        consentDocumentService.delete(consentId)
    }
}