package dk.cachet.carp.webservices.summary.controller

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.constants.RequestParamName
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.summary.controller.SummaryController.Companion.SUMMARY_BASE
import dk.cachet.carp.webservices.summary.domain.CreateSummaryRequest
import dk.cachet.carp.webservices.summary.domain.Summary
import dk.cachet.carp.webservices.summary.service.SummaryService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(SUMMARY_BASE)
class SummaryController(
    private val summaryService: SummaryService,
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
    @PreAuthorize("#{false}")
    fun create(@RequestBody request: CreateSummaryRequest): Summary
    {
        LOGGER.info("Start POST: $SUMMARY_BASE")
        return summaryService.createSummaryForStudy(request.studyId, request.deploymentIds)
    }

    @DeleteMapping(DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#{false}")
    fun delete(@PathVariable(PathVariableName.SUMMARY_ID) summaryId: UUID) {
        LOGGER.info("Start DELETE: $DELETE")
        summaryService.deleteSummaryById(summaryId)
    }

    @GetMapping(DOWNLOAD)
    @PreAuthorize("#{false}")
    fun download(@PathVariable(PathVariableName.SUMMARY_ID) summaryId: UUID): ResponseEntity<Resource> {
        LOGGER.info("Start GET: $DOWNLOAD")
        val file = summaryService.downloadSummary(summaryId)
        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"${file.filename}\""
        ).body(file)
    }

    @GetMapping(LIST)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("#{false}")
    fun list(@RequestParam(RequestParamName.STUDY_ID) studyId: UUID?): List<Summary> {
        LOGGER.info("Start GET: $LIST")
        val account = authenticationService.getAuthentication()
        return summaryService.listSummaries(UUID(account.id!!), studyId)
    }
}