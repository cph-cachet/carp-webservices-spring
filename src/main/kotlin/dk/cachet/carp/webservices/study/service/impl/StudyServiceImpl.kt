package dk.cachet.carp.webservices.study.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.config.SecurityCoroutineContext
import dk.cachet.carp.webservices.study.domain.StudyOverview
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import dk.cachet.carp.webservices.study.service.StudyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class StudyServiceImpl(
    private val accountService: AccountService,
    private val repository: CoreStudyRepository,
    services: CoreServiceContainer
) : StudyService
{
    final override val core = services.studyService

    override suspend fun getStudiesOverview( accountId: UUID ): List<StudyOverview> =
        withContext( Dispatchers.IO + SecurityCoroutineContext() )
        {
            val account = accountService.findByUUID( accountId ) ?:
                throw IllegalArgumentException("Account with id $accountId is not found.")

            val studyStatuses = account.carpClaims
                ?.filterIsInstance<Claim.ManageStudy>()
                ?.map { it.studyId }
                ?.let {
                    repository.findAllByStudyIds( it ).map { study -> study.getStatus() }
                } ?: emptyList()

            studyStatuses.map {
                val details = core.getStudyDetails( it.studyId )

                StudyOverview(
                    it.studyId,
                    it.name,
                    it.createdOn,
                    it.studyProtocolId,
                    it.canSetInvitation,
                    it.canSetStudyProtocol,
                    it.canDeployToParticipants,
                    details.description
                )
            }
        }
}
