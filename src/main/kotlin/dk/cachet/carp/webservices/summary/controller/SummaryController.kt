package dk.cachet.carp.webservices.summary.controller

import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.constants.RequestParamName
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.summary.controller.SummaryController.Companion.SUMMARY_BASE
import dk.cachet.carp.webservices.summary.domain.CreateSummaryRequest
import dk.cachet.carp.webservices.summary.domain.Summary
import dk.cachet.carp.webservices.summary.service.ISummaryService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(SUMMARY_BASE)
class SummaryController(
    private val summaryService: ISummaryService,
    private val authenticationService: AuthenticationService
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val SUMMARY_BASE = "/api/summary"
        const val DELETE = "/{${PathVariableName.SUMMARY_ID}}"
        const val DOWNLOAD = "/{${PathVariableName.SUMMARY_ID}}"
        const val LIST = "/list"
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("@summaryAuthorizationService.canCreateSummary(#request.studyId)")
    fun create(@RequestBody request: CreateSummaryRequest): Summary
    {
        LOGGER.info("Start POST: $SUMMARY_BASE")
        return summaryService.createSummaryForStudy(request.studyId, request.deploymentIds)
    }

    @DeleteMapping(DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@summaryAuthorizationService.canDownloadSummary(#summaryId)")
    fun delete(@PathVariable(PathVariableName.SUMMARY_ID) summaryId: String) {
        LOGGER.info("Start DELETE: $DELETE")
        summaryService.deleteSummaryById(summaryId)
    }

    @GetMapping(DOWNLOAD)
    @PreAuthorize("@summaryAuthorizationService.canDownloadSummary(#summaryId)")
    fun download(@PathVariable(PathVariableName.SUMMARY_ID) summaryId: String): ResponseEntity<Resource> {
        LOGGER.info("Start GET: $DOWNLOAD")
        val file = summaryService.downloadSummary(summaryId)
        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"$summaryId.zip\""
        ).body(file)
    }

    @GetMapping(LIST)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("@summaryAuthorizationService.canListSummaries(#studyId)")
    fun list(@RequestParam(RequestParamName.STUDY_ID) studyId: String?): List<Summary> {
        LOGGER.info("Start GET: $LIST")
        return summaryService.listSummaries(authenticationService.getCurrentPrincipal().id!!, studyId)
    }
}