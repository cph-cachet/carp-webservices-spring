package dk.cachet.carp.webservices.study.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.export.service.ResourceExporter
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.config.SecurityCoroutineContext
import dk.cachet.carp.webservices.study.domain.StudyOverview
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import dk.cachet.carp.webservices.study.service.StudyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import java.nio.file.Path

@Service
class StudyServiceWrapper(
    private val accountService: AccountService,
    private val repository: CoreStudyRepository,
    services: CoreServiceContainer,
) : StudyService, ResourceExporter<StudySnapshot> {
    final override val core = services.studyService

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override suspend fun getStudiesOverview(accountId: UUID): List<StudyOverview> =
        withContext(Dispatchers.IO + SecurityCoroutineContext()) {
            val account =
                accountService.findByUUID(accountId)
                    ?: throw IllegalArgumentException("Account with id $accountId is not found.")

            account.carpClaims
                ?.filterIsInstance<Claim.ManageStudy>()
                ?.map { it.studyId }
                ?.let { repository.findAllByStudyIds(it) }
                ?.map {
                    val status = it.getStatus()
//                    val details = core.getStudyDetails(status.studyId)
//                    val owner = accountService.findByUUID(details.ownerId)
                    // TODO: Do we still need this?
                    StudyOverview(
                        status.studyId,
                        status.name,
                        status.createdOn,
                        status.studyProtocolId,
                        status.canSetInvitation,
                        status.canSetStudyProtocol,
                        status.canDeployToParticipants,
//                        details.description,
                        "",
                        "",
//                        owner?.fullName,
                    )
                }
                ?: emptyList()
        }

    final override val dataFileName = "study.json"

    override suspend fun studyExists(studyId: UUID): Boolean =
        try {
            repository.getWSStudyById(studyId)
            true
        } catch (e: IllegalStateException) {
            LOGGER.error("Error checking if study exists for studyId: $studyId", e)
            false
        }

    override suspend fun exportDataOrThrow(
        studyId: UUID,
        deploymentIds: Set<UUID>,
        target: Path,
    ): Collection<StudySnapshot> = setOf(repository.getStudySnapshotById(studyId))
}
