package dk.cachet.carp.webservices.study.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.application.users.StudyInvitation
import dk.cachet.carp.studies.domain.Study
import dk.cachet.carp.studies.domain.StudyRepository
import dk.cachet.carp.studies.domain.StudySnapshot
import dk.cachet.carp.webservices.collection.repository.CollectionRepository
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.consent.repository.ConsentDocumentRepository
import dk.cachet.carp.webservices.dataPoint.repository.DataPointRepository
import dk.cachet.carp.webservices.document.repository.DocumentRepository
import dk.cachet.carp.webservices.file.repository.FileRepository
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
): StudyRepository
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override suspend fun add(study: Study) = runBlocking {
        if ( studyRepository.getByStudyId(study.id.stringRepresentation) != null )
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
        val study = studyRepository.getByStudyId(studyId.stringRepresentation)

        if ( study == null )
        {
            LOGGER.info("Study is not found, id: ${studyId.stringRepresentation}")
            return@runBlocking null
        }

        return@runBlocking convertStudySnapshotNodeToStudy( study.snapshot!! )
    }

    override suspend fun getForOwner(ownerId: UUID): List<Study> = runBlocking {
        val studies = studyRepository.findAllByOwnerId(ownerId.stringRepresentation)
        return@runBlocking studies.map { convertStudySnapshotNodeToStudy(it.snapshot!!) }.toList()
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
        val existingStudy = studyRepository.getByStudyId(study.id.stringRepresentation)

        if (existingStudy == null)
        {
            LOGGER.warn("Study is not found, id: ${study.id.stringRepresentation}")
            throw IllegalArgumentException(validationMessages.get("study.core.update.study.not_found", study.id.stringRepresentation))
        }

        existingStudy.snapshot = objectMapper.valueToTree(study.getSnapshot())
        LOGGER.info("Study updated, id: ${study.id.stringRepresentation}")
    }


    fun getWSStudyById( id: UUID ): dk.cachet.carp.webservices.study.domain.Study
    {
        val study = studyRepository.getByStudyId( id.stringRepresentation )
        if ( study == null )
        {
            LOGGER.warn( "Study is not found, id: ${id.stringRepresentation}" )
            throw IllegalArgumentException( validationMessages.get("study.core.study.not_found", id.stringRepresentation ) )
        }

        return study
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
}