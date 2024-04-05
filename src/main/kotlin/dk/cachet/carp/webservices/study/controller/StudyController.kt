package dk.cachet.carp.webservices.study.controller

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.constants.RequestParamName
import dk.cachet.carp.webservices.common.exception.responses.BadRequestException
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.study.authorization.StudyAuthorizationService
import dk.cachet.carp.webservices.study.domain.AnonymousLinkRequest
import dk.cachet.carp.webservices.study.domain.ParticipantGroupsStatus
import dk.cachet.carp.webservices.study.domain.StudyOverview
import dk.cachet.carp.webservices.study.dto.AddParticipantsRequestDto
import dk.cachet.carp.webservices.study.repository.CoreParticipantRepository
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import dk.cachet.carp.webservices.study.service.CoreRecruitmentService
import dk.cachet.carp.webservices.study.service.CoreStudyService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
class StudyController
    (
    private val coreParticipantRepository: CoreParticipantRepository,
    private val coreDeploymentRepository: CoreDeploymentRepository,
    private val coreStudyRepository: CoreStudyRepository,
    private val studyAuthorizationService: StudyAuthorizationService,
    private val validationMessages: MessageBase,
    private val authenticationService: AuthenticationService,
    coreStudyService: CoreStudyService,
    /**
     * This is a hack to resolve the circular dependencies. I hate to do this, but the webservices
     * is in a very sorry state and I cannot emphasize enough how much I want to refactor it.
     * I just want to have the surrounding infrastructure ready.
     */
    private val coreRecruitmentService: CoreRecruitmentService
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
        const val GENERATE_ANONYMOUS_PARTICIPANTS = "/api/studies/{${PathVariableName.STUDY_ID}}/generate"
    }

    private val studyService = coreStudyService.instance
    private val recruitmentService = coreRecruitmentService.instance

    @PostMapping(value = [ADD_RESEARCHER])
    @PreAuthorize("@studyAuthorizationService.canAccessStudy(#studyId)")
    @Operation(tags = ["study/addResearcher.json"])
    fun addResearcher(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String,
        @RequestParam(RequestParamName.EMAIL) email: String
    ) {
        LOGGER.info("Start POST: /api/studies/$studyId/researchers")
        return coreStudyRepository.inviteResearcherToStudy(studyId, email)
    }

    @GetMapping(value = [GET_PARTICIPANTS_ACCOUNTS])
    @PreAuthorize("@studyAuthorizationService.canAccessStudy(#studyId)")
    @Operation(tags = ["study/getParticipantsAccounts.json"])
    fun getParticipantAccounts(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String
    ): List<Account> {
        LOGGER.info("Start GET: /api/studies/$studyId/participants/accounts")
        return runBlocking { coreRecruitmentService.getParticipantAccounts(UUID(studyId)) }
    }

    @GetMapping(value = [RESEARCHERS])
    @PreAuthorize("@studyAuthorizationService.canAccessStudy(#studyId)")
    @Operation(tags = ["study/getResearchers.json"])
    fun getResearchers(@PathVariable(PathVariableName.STUDY_ID) studyId: String): List<Account> {
        LOGGER.info("Start GET: /api/studies/$studyId/researchers")
        return runBlocking { coreStudyRepository.getResearcherAccountsForStudy(studyId) }
    }

    @GetMapping(value = [GET_PARTICIPANT_GROUP_STATUS])
    @PreAuthorize("@studyAuthorizationService.canAccessStudy(#studyId)")
    @Operation(tags = ["study/getParticipantGroupStatus.json"])
    fun getParticipantGroupStatus(@PathVariable(PathVariableName.STUDY_ID) studyId: String): ParticipantGroupsStatus {
        LOGGER.info("Start GET: /api/studies/$studyId/participantGroup/status")
        return runBlocking { coreRecruitmentService.getParticipantGroupStatus(UUID(studyId)) }
    }

    @DeleteMapping(value = [RESEARCHERS])
    @PreAuthorize("@studyAuthorizationService.canAccessStudy(#studyId)")
    @Operation(tags = ["study/removeResearchers.json"])
    fun removeResearcher(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String,
        @RequestParam(RequestParamName.EMAIL) email: String
    ): Boolean {
        return coreStudyRepository.removeResearcherFromStudy(studyId, email)
    }

    @GetMapping(value = [GET_PARTICIPANT_INFO])
    @PreAuthorize("@studyAuthorizationService.canAccessStudy(#studyId)")
    @Operation(tags = ["study/getParticipantAccountInfo.json"])
    fun getParticipantAccountInfo(@PathVariable(PathVariableName.STUDY_ID) studyId: String): List<Account> {
        LOGGER.info("Start POST: /api/studies/$studyId/participants")
        return runBlocking { coreParticipantRepository.getParticipantAccountDetailsForStudy(studyId) }
    }

    @GetMapping(value = [GET_STUDIES_OVERVIEW])
    @PreAuthorize("@studyAuthorizationService.canCreateStudy()")
    fun getStudiesOverview(): List<StudyOverview> {
        LOGGER.info("Start POST: /api/studies/studies-overview")
        return runBlocking { coreStudyRepository.getStudiesOverview(authenticationService.getCurrentPrincipal().id!!) }
    }

    @PostMapping(value = [STUDY_SERVICE])
    @Operation(tags = ["study/studies.json"])
    fun studies(@RequestBody request: StudyServiceRequest<*>): ResponseEntity<*> = runBlocking {
        return@runBlocking when (request) {
            is StudyServiceRequest.CreateStudy -> {
                if (!studyAuthorizationService.canCreateStudy()) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $STUDY_SERVICE -> CreateStudy")
                val status =
                    studyService.createStudy(request.ownerId, request.name, request.description, request.invitation)
                ResponseEntity.status(HttpStatus.CREATED).body(status)
            }

            is StudyServiceRequest.GetStudyStatus -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $STUDY_SERVICE -> GetStudyStatus")
                val status = studyService.getStudyStatus(request.studyId)
                ResponseEntity.ok(status)
            }

            is StudyServiceRequest.GetStudiesOverview -> {
                if (!studyAuthorizationService.canCreateStudy()) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $STUDY_SERVICE -> GetStudiesOverview")
                val result = studyService.getStudiesOverview(request.ownerId)
                ResponseEntity.ok(result)
            }

            is StudyServiceRequest.SetProtocol -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $STUDY_SERVICE -> SetProtocol")
                val result = studyService.setProtocol(request.studyId, request.protocol)
                ResponseEntity.ok(result)
            }

            is StudyServiceRequest.GoLive -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $STUDY_SERVICE -> GoLive")
                val result = studyService.goLive(request.studyId)
                ResponseEntity.ok(result)
            }

            is StudyServiceRequest.SetInternalDescription -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $STUDY_SERVICE -> SetInternalDescription")
                val result = studyService.setInternalDescription(request.studyId, request.name, request.description)
                ResponseEntity.ok(result)
            }

            is StudyServiceRequest.SetInvitation -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $STUDY_SERVICE -> SetInvitation")
                val result = studyService.setInvitation(request.studyId, request.invitation)
                ResponseEntity.ok(result)
            }

            is StudyServiceRequest.GetStudyDetails -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $STUDY_SERVICE -> GetStudyDetails")
                val result = studyService.getStudyDetails(request.studyId)
                ResponseEntity.ok(result)
            }

            is StudyServiceRequest.Remove -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $STUDY_SERVICE -> Remove")
                val result = studyService.remove(request.studyId)
                ResponseEntity.ok(result)
            }

            else -> throw BadRequestException(validationMessages.get("study.service.invalid_request", request))
        }
    }


    @PostMapping(value = [RECRUITMENT_SERVICE])
    @Operation(tags = ["study/recruitments.json"])
    fun recruitments(@RequestBody request: RecruitmentServiceRequest<*>): ResponseEntity<*> = runBlocking {
        return@runBlocking when (request) {
            is RecruitmentServiceRequest.AddParticipantByEmailAddress -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $RECRUITMENT_SERVICE -> AddParticipant")
                val result = recruitmentService.addParticipant(request.studyId, request.email)
                ResponseEntity.ok(result)
            }

            is RecruitmentServiceRequest.AddParticipantByUsername -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $RECRUITMENT_SERVICE -> AddParticipant")
                val result = recruitmentService.addParticipant(request.studyId, request.username)
                ResponseEntity.ok(result)
            }

            is RecruitmentServiceRequest.GetParticipant -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $RECRUITMENT_SERVICE -> GetParticipant")
                val result = recruitmentService.getParticipant(request.studyId, request.participantId)
                ResponseEntity.ok(result)
            }

            is RecruitmentServiceRequest.GetParticipants -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $RECRUITMENT_SERVICE -> GetParticipants")
                val result = recruitmentService.getParticipants(request.studyId)
                ResponseEntity.ok(result)
            }

            is RecruitmentServiceRequest.InviteNewParticipantGroup -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $RECRUITMENT_SERVICE -> DeployParticipantGroup")
                val result = recruitmentService.inviteNewParticipantGroup(request.studyId, request.group)
                setStudyIdForNewDeployment(result, request.studyId)
                ResponseEntity.ok(result)
            }

            is RecruitmentServiceRequest.GetParticipantGroupStatusList -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $RECRUITMENT_SERVICE -> GetParticipantGroupStatusList")
                val result = recruitmentService.getParticipantGroupStatusList(request.studyId)
                ResponseEntity.ok(result)
            }

            is RecruitmentServiceRequest.StopParticipantGroup -> {
                if (!studyAuthorizationService.canAccessStudy(request.studyId.stringRepresentation)) {
                    return@runBlocking ResponseEntity(HttpStatus.FORBIDDEN)
                }
                LOGGER.info("Start POST: $RECRUITMENT_SERVICE -> StopParticipantGroup")
                val result = recruitmentService.stopParticipantGroup(request.studyId, request.groupId)
                ResponseEntity.ok(result)
            }

            else -> throw BadRequestException(
                validationMessages.get(
                    "study.service.participant.invalid_request",
                    request
                )
            )
        }
    }

    private suspend fun setStudyIdForNewDeployment(participantGroupStatus: ParticipantGroupStatus, studyId: UUID) {
        val deploymentId = participantGroupStatus.id
        val newDeployment = coreDeploymentRepository.getWSDeploymentById(deploymentId)
        newDeployment!!.deployedFromStudyId = studyId.stringRepresentation
        coreDeploymentRepository.updateWSDeployment(newDeployment)
    }

    @PostMapping(value = [ADD_PARTICIPANTS])
    @PreAuthorize("@studyAuthorizationService.canAccessStudy(#studyId)")
    suspend fun addParticipants(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String,
        @Valid @RequestBody request: AddParticipantsRequestDto
    ) {
        runBlocking {
            LOGGER.info("Start POST: /api/studies/$studyId/participants/add")
            request.emails.forEach { e -> recruitmentService.addParticipant(UUID(studyId), EmailAddress(e)) }
        }
    }

    @PostMapping(GENERATE_ANONYMOUS_PARTICIPANTS)
    @PreAuthorize("@accountAuthorizationService.isResearcherPartOfTheStudy(#studyId)")
    suspend fun generateAnonymousParticipants(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @Valid @RequestBody request: AnonymousLinkRequest
    ): ResponseEntity<ByteArray> {
        LOGGER.info("Start POST: /api/studies/$studyId/generate")

        val anonymousParticipants = coreRecruitmentService.createAnonymousParticipants(
            studyId,
            request.amountOfAccounts,
            request.expirationSeconds,
            request.participantRoleName,
            request.redirectUri
        )

        val responseHeaders = HttpHeaders()
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, "text/csv")
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=${Clock.System.now()}_${studyId}.csv")

        val header = "account_id,study_deployment_id,access_link,expiry_date\n"
        val body = anonymousParticipants.joinToString("") { participant ->
            "${participant.accountId},${participant.studyDeploymentId},\"${participant.magicLink}\",${participant.expiryDate}\n"
        }

        return ResponseEntity((header + body).toByteArray(Charsets.ISO_8859_1), responseHeaders, HttpStatus.OK)
    }
}
