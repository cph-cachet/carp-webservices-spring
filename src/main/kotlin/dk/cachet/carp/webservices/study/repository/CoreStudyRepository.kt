package dk.cachet.carp.webservices.study.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.collection.repository.CollectionRepository
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.consent.repository.ConsentDocumentRepository
import dk.cachet.carp.webservices.dataPoint.repository.DataPointRepository
import dk.cachet.carp.webservices.document.repository.DocumentRepository
import dk.cachet.carp.webservices.file.repository.FileRepository
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.study.domain.StudyOverview
import dk.cachet.carp.webservices.summary.repository.SummaryRepository
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CoreStudyRepository
(
    private val studyRepository: dk.cachet.carp.webservices.study.repository.StudyRepository,
    private val participantRepository: CoreParticipantRepository,
    private val dataPointRepository: DataPointRepository,
    private val collectionRepository: CollectionRepository,
    private val consentDocumentRepository: ConsentDocumentRepository,
    private val documentRepository: DocumentRepository,
    private val summaryRepository: SummaryRepository,
    private val filesRepository: FileRepository,
    private val objectMapper: ObjectMapper,
    private val validationMessages: MessageBase,
    private val accountService: AccountService,
): StudyRepository
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override suspend fun add(study: Study) = runBlocking {
        if (studyRepository.getByStudyId(study.id.stringRepresentation).isPresent)
        {
            LOGGER.warn("Study already exists, id: ${study.id.stringRepresentation}")
            throw IllegalArgumentException(validationMessages.get("study.core.add.exists", study.id.stringRepresentation))
        }

        val studyToSave = dk.cachet.carp.webservices.study.domain.Study()

        val extendedInvitation = StudyInvitation(study.invitation.name, study.invitation.description, study.id.stringRepresentation)
        study.invitation = extendedInvitation

        val snapshot = study.getSnapshot()
        studyToSave.snapshot = objectMapper.valueToTree(snapshot)

        studyRepository.save(studyToSave)
        LOGGER.info("Study saved, id: ${study.id.stringRepresentation}")
    }

    override suspend fun getById(studyId: UUID): Study? = runBlocking {
        val optionalStudy = studyRepository.getByStudyId(studyId.stringRepresentation)
        if (!optionalStudy.isPresent)
        {
            LOGGER.info("Study is not found, id: ${studyId.stringRepresentation}")
            return@runBlocking null
        }

        val foundStudy = optionalStudy.get()
        return@runBlocking convertStudySnapshotNodeToStudy(foundStudy.snapshot!!)
    }

    override suspend fun getForOwner(ownerId: UUID): List<Study> = runBlocking {
        val studies = studyRepository.findAllByOwnerId(ownerId.stringRepresentation)
        return@runBlocking studies.map { convertStudySnapshotNodeToStudy(it.snapshot!!) }.toList()
    }

    suspend fun getStudiesOverview(accountId: String): List<StudyOverview> = runBlocking {
        val studiesAsOwner = studyRepository.findAllByOwnerId(accountId)
        val studiesAsGuestResearcher = studyRepository.getForGuestResearcher(accountId)
        val studies = studiesAsOwner + studiesAsGuestResearcher
        return@runBlocking studies.map {
            val study = convertStudySnapshotNodeToStudy(it.snapshot!!)
            val studyStatus = study.getStatus()
            StudyOverview(
                    studyStatus.studyId,
                    studyStatus.name,
                    studyStatus.createdOn,
                    studyStatus.studyProtocolId,
                    studyStatus.canSetInvitation,
                    studyStatus.canSetStudyProtocol,
                    studyStatus.canDeployToParticipants,
                    study.description)
        }.distinctBy { it.studyId }.toList()
    }

    /**
     * TODO: This should only remove the study from the study repository.
     * TODO: All associated data should be deleted by subscribing to `StudyService.Event.StudyRemoved` in the respective services.
     */
    @Transactional( rollbackFor = [Exception::class])
    override suspend fun remove(studyId: UUID): Boolean = runBlocking {
        val idsToRemove = getDeploymentIdsOrThrow( studyId )
        val collectionIds = collectionRepository.getCollectionIdsByStudyId( studyId.stringRepresentation )

        documentRepository.deleteAllByCollectionIds( collectionIds )

        collectionRepository.deleteAllByDeploymentIds( idsToRemove )
        consentDocumentRepository.deleteAllByDeploymentIds( idsToRemove )
        dataPointRepository.deleteAllByDeploymentIds( idsToRemove )

        filesRepository.deleteByStudyId( studyId.stringRepresentation )
        summaryRepository.deleteByStudyId( studyId.stringRepresentation )
        studyRepository.deleteByStudyId( studyId.stringRepresentation )

        LOGGER.info("Study with id ${studyId.stringRepresentation} and all associated data deleted.")
        return@runBlocking true
    }

    override suspend fun update(study: Study) = runBlocking {
        val optionalStudy = studyRepository.getByStudyId(study.id.stringRepresentation)
        if (!optionalStudy.isPresent)
        {
            LOGGER.warn("Study is not found, id: ${study.id.stringRepresentation}")
            throw IllegalArgumentException(validationMessages.get("study.core.update.study.not_found", study.id.stringRepresentation))
        }

        val storedStudy = optionalStudy.get()
        storedStudy.snapshot = objectMapper.valueToTree(study.getSnapshot())
        LOGGER.info("Study updated, id: ${study.id.stringRepresentation}")
    }

    fun inviteResearcherToStudy(studyId: String, email: String) = runBlocking {
        val study = getWSStudyById(UUID(studyId))
        val accountIdentity = AccountIdentity.fromEmailAddress(email)
        var account = accountService.findByAccountIdentity(accountIdentity)

        if (account == null)
        {
            LOGGER.info("Account with email $email is not found.")
            account = accountService.invite(accountIdentity, Role.RESEARCHER)
        }

        if (account.role!! < Role.RESEARCHER) {
            accountService.addRole(accountIdentity, Role.RESEARCHER)
            LOGGER.info("Account with email $email is granted the role RESEARCHER.")
        }

        if (study.researcherAccountIds.contains(account.id))
        {
            LOGGER.info("Study with id $studyId already contains the account with id ${account.id}")
            throw IllegalArgumentException(validationMessages.get("study.core.invite.researcher.exists", account.id!!, studyId))
        }

        study.researcherAccountIds.add(account.id!!)
        LOGGER.info("Account with email $email is added as a researcher to study with id $studyId.")
    }

    fun getWSStudyById(id: UUID): dk.cachet.carp.webservices.study.domain.Study
    {
        val optionalStudy = studyRepository.getByStudyId(id.stringRepresentation)
        if (!optionalStudy.isPresent)
        {
            LOGGER.warn("Study is not found, id: ${id.stringRepresentation}")
            throw IllegalArgumentException(validationMessages.get("study.core.study.not_found", id.stringRepresentation))
        }
        return optionalStudy.get()
    }

    suspend fun getDeploymentIdsOrThrow(studyId: UUID): List<String> = runBlocking {
        val recruitment = participantRepository.getRecruitment(studyId)
        return@runBlocking recruitment?.participantGroups?.keys?.map { it.stringRepresentation } ?: emptyList()
    }

    suspend fun getStudySnapshotById(id: UUID): StudySnapshot = runBlocking {
        val study = getWSStudyById(id)
        return@runBlocking objectMapper.treeToValue(study.snapshot, StudySnapshot::class.java)
    }

    suspend fun convertStudySnapshotNodeToStudy(node: JsonNode): Study
    {
        val snapshot = objectMapper.treeToValue(node, StudySnapshot::class.java)
        return Study.fromSnapshot(snapshot)
    }

    suspend fun getResearcherAccountsForStudy(studyId: String): List<Account> = runBlocking {
        val ownerId = convertStudySnapshotNodeToStudy(getWSStudyById(UUID(studyId)).snapshot!!).ownerId.stringRepresentation
        val accountIds = getWSStudyById(UUID(studyId)).researcherAccountIds.toMutableList()
        accountIds.add(ownerId)
        return@runBlocking accountIds.mapNotNull { accountService.findByUUID(UUID(it)) }
    }

    fun removeResearcherFromStudy(studyId: String, email: String): Boolean
    {
        val study = getWSStudyById(UUID(studyId))
        val account = runBlocking { accountService.findByAccountIdentity(AccountIdentity.fromEmailAddress(email))
            ?: throw IllegalArgumentException("Account with email $email is not found.") }
        val isDeleted = study.researcherAccountIds.remove(account.id)
        if (isDeleted) LOGGER.info("Researcher with id ${account.id} is removed from the study with id $studyId.")
        return isDeleted
    }
}