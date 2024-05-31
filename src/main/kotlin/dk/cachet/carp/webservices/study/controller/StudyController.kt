package dk.cachet.carp.webservices.study.controller

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.constants.RequestParamName
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.study.domain.ParticipantGroupsStatus
import dk.cachet.carp.webservices.study.domain.StudyOverview
import dk.cachet.carp.webservices.study.dto.AddParticipantsRequestDto
import dk.cachet.carp.webservices.study.service.RecruitmentService
import dk.cachet.carp.webservices.study.service.StudyService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
class StudyController(
    private val authenticationService: AuthenticationService,
    private val accountService: AccountService,
    private val studyService: StudyService,
    private val recruitmentService: RecruitmentService,
) {
    companion object {
        val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val STUDY_SERVICE = "/api/study-service"
        const val RECRUITMENT_SERVICE = "/api/recruitment-service"
        const val RESEARCHERS = "/api/studies/{${PathVariableName.STUDY_ID}}/researchers"
        const val ADD_RESEARCHER = "/api/studies/{${PathVariableName.STUDY_ID}}/researchers/add"
        const val GET_PARTICIPANT_INFO = "/api/studies/{${PathVariableName.STUDY_ID}}/participants"
        const val GET_STUDIES_OVERVIEW = "/api/studies/studies-overview"
        const val GET_PARTICIPANTS_ACCOUNTS = "/api/studies/{${PathVariableName.STUDY_ID}}/participants/accounts"
        const val GET_PARTICIPANT_GROUP_STATUS = "/api/studies/{${PathVariableName.STUDY_ID}}/participantGroup/status"
        const val ADD_PARTICIPANTS = "/api/studies/{${PathVariableName.STUDY_ID}}/participants/add"
    }

    @PostMapping(value = [ADD_RESEARCHER])
    @PreAuthorize("canManageStudy(#studyId)")
    @Operation(tags = ["study/addResearcher.json"])
    suspend fun addResearcher(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @RequestParam(RequestParamName.EMAIL) email: String,
    ) {
        LOGGER.info("Start POST: /api/studies/$studyId/researchers")
        return recruitmentService.inviteResearcher(studyId, email)
    }

    @GetMapping(value = [GET_PARTICIPANTS_ACCOUNTS])
    @PreAuthorize("canManageStudy(#studyId)")
    @Operation(tags = ["study/getParticipantsAccounts.json"])
    suspend fun getParticipantAccounts(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @RequestParam(name = RequestParamName.OFFSET, required = false, defaultValue = "0") offset: Int,
        @RequestParam(name = RequestParamName.LIMIT, required = false, defaultValue = "-1") limit: Int,
    ): List<Account> {
        LOGGER.info("Start GET: /api/studies/$studyId/participants/accounts")
        return recruitmentService.getParticipants(studyId, offset, limit)
    }

    @GetMapping(value = [RESEARCHERS])
    @PreAuthorize("canManageStudy(#studyId)")
    @Operation(tags = ["study/getResearchers.json"])
    suspend fun getResearchers(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
    ): List<Account> {
        LOGGER.info("Start GET: /api/studies/$studyId/researchers")
        return accountService.findAllByClaim(Claim.ManageStudy(studyId))
    }

    @GetMapping(value = [GET_PARTICIPANT_GROUP_STATUS])
    @PreAuthorize("canManageStudy(#studyId)")
    @Operation(tags = ["study/getParticipantGroupStatus.json"])
    fun getParticipantGroupStatus(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
    ): ParticipantGroupsStatus {
        LOGGER.info("Start GET: /api/studies/$studyId/participantGroup/status")
        return runBlocking { recruitmentService.getParticipantGroupsStatus(studyId) }
    }

    @DeleteMapping(value = [RESEARCHERS])
    @PreAuthorize("canManageStudy(#studyId)")
    @Operation(tags = ["study/removeResearchers.json"])
    suspend fun removeResearcher(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @RequestParam(RequestParamName.EMAIL) email: String,
    ): Boolean = recruitmentService.removeResearcher(studyId, email)

    // TODO: duplicated endpoint, mark for removal
    @GetMapping(value = [GET_PARTICIPANT_INFO])
    @PreAuthorize("canManageStudy(#studyId)")
    @Operation(tags = ["study/getParticipantAccountInfo.json"])
    suspend fun getParticipantAccountInfo(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @RequestParam(name = RequestParamName.OFFSET, required = false, defaultValue = "0") offset: Int,
        @RequestParam(name = RequestParamName.LIMIT, required = false, defaultValue = "-1") limit: Int,
    ): List<Account> {
        LOGGER.info("Start POST: /api/studies/$studyId/participants")
        return recruitmentService.getParticipants(studyId, offset, limit)
    }

    @GetMapping(value = [GET_STUDIES_OVERVIEW])
    suspend fun getStudiesOverview(): List<StudyOverview> {
        LOGGER.info("Start POST: /api/studies/studies-overview")
        return studyService.getStudiesOverview(authenticationService.getId())
    }

    @PostMapping(value = [STUDY_SERVICE])
    @Operation(tags = ["study/studies.json"])
    suspend fun studies(
        @RequestBody request: StudyServiceRequest<*>,
    ): ResponseEntity<*> {
        LOGGER.info("Start POST: $STUDY_SERVICE -> ${ request::class.simpleName }")
        return studyService.core.invoke(request).let { ResponseEntity.ok(it) }
    }

    @PostMapping(value = [RECRUITMENT_SERVICE])
    @Operation(tags = ["study/recruitments.json"])
    suspend fun recruitments(
        @RequestBody request: RecruitmentServiceRequest<*>,
    ): ResponseEntity<*> {
        LOGGER.info("Start POST: $RECRUITMENT_SERVICE -> ${ request::class.simpleName }")
        return recruitmentService.core.invoke(request).let { ResponseEntity.ok(it) }
    }

    @PostMapping(value = [ADD_PARTICIPANTS])
    @PreAuthorize("canManageStudy(#studyId)")
    suspend fun addParticipants(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @Valid @RequestBody request: AddParticipantsRequestDto,
    ) {
        LOGGER.info("Start POST: /api/studies/$studyId/participants/add")
        request.emails.forEach { e -> recruitmentService.core.addParticipant(studyId, EmailAddress(e)) }
    }
}
