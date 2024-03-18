package dk.cachet.carp.webservices.study.repository

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.studies.domain.users.ParticipantRepository
import dk.cachet.carp.studies.domain.users.Recruitment
import dk.cachet.carp.studies.domain.users.RecruitmentSnapshot
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.security.authentication.domain.Account
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CoreParticipantRepository
(
    private val objectMapper: ObjectMapper,
    private val recruitmentRepository: RecruitmentRepository,
    private val accountService: AccountService,
): ParticipantRepository
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    suspend fun getParticipantAccountDetailsForStudy(studyId: String): List<Account> = runBlocking {
        val recruitment = recruitmentRepository.findRecruitmentByStudyId(studyId)
            ?: throw IllegalArgumentException("Recruitment for study with id $studyId not found")
        val recruitmentSnapshot =  objectMapper.treeToValue(recruitment.snapshot, RecruitmentSnapshot::class.java)
        val participantEmails = mutableSetOf<String>()
        val participantUsernames = mutableSetOf<String>()
        recruitmentSnapshot.participants.forEach { p ->
            run {
                when (p.accountIdentity) {
                    is EmailAccountIdentity -> participantEmails.add((p.accountIdentity as EmailAccountIdentity).emailAddress.address)
                    is UsernameAccountIdentity -> participantUsernames.add((p.accountIdentity as UsernameAccountIdentity).username.name)
                }
            }
        }
        val accounts = arrayListOf<Account>()
        participantUsernames.forEach{
            val account = accountService.findByAccountIdentity(UsernameAccountIdentity(it))
            if (account != null) {
                accounts.add(account)
            }
        }
        participantEmails.forEach{
            val account = accountService.findByAccountIdentity(EmailAccountIdentity(it))
            if (account != null) {
                accounts.add(account)
            }
        }
        return@runBlocking accounts
    }

    override suspend fun addRecruitment(recruitment: Recruitment) = runBlocking {
        val studyId = recruitment.studyId.stringRepresentation
        val existingRecruitment = recruitmentRepository.findRecruitmentByStudyId(studyId)
        if (existingRecruitment != null) {
            throw IllegalArgumentException("A recruitment already exists for the study with id $studyId.")
        }

        val snapshotJsonNode = objectMapper.valueToTree<JsonNode>(recruitment.getSnapshot())
        val newRecruitment = dk.cachet.carp.webservices.study.domain.Recruitment().apply {
            snapshot = snapshotJsonNode
        }
        val saved = recruitmentRepository.save(newRecruitment)
        LOGGER.info("New recruitment with id ${saved.id} is saved for study with id $studyId.")
    }

    override suspend fun getRecruitment(studyId: UUID): Recruitment? = runBlocking {
        val existingRecruitment = recruitmentRepository.findRecruitmentByStudyId(studyId.stringRepresentation)
        if (existingRecruitment == null)
        {
            LOGGER.info("Recruitment for studyId $studyId is not found.")
            return@runBlocking null
        }
        return@runBlocking mapWSRecruitmentToCore(existingRecruitment)
    }

    override suspend fun removeStudy(studyId: UUID): Boolean = runBlocking {
        getRecruitment(studyId) ?: return@runBlocking false
        recruitmentRepository.deleteByStudyId(studyId.stringRepresentation)
        LOGGER.info("Recruitment with studyId ${studyId.stringRepresentation} is deleted.")
        return@runBlocking true
    }

    override suspend fun updateRecruitment(recruitment: Recruitment) = runBlocking {
        val studyId = recruitment.studyId.stringRepresentation
        val recruitmentFound = recruitmentRepository.findRecruitmentByStudyId(studyId)
            ?: throw ResourceNotFoundException("Recruitment with studyId $studyId is not found.")

        val newSnapshotNode = objectMapper.valueToTree<JsonNode>(recruitment.getSnapshot())
        recruitmentFound.snapshot = newSnapshotNode
        recruitmentRepository.save(recruitmentFound)
        LOGGER.info("Recruitment with studyId $studyId is updated.")
    }

    private fun mapWSRecruitmentToCore(recruitment: dk.cachet.carp.webservices.study.domain.Recruitment): Recruitment
    {
        val snapshot = objectMapper.treeToValue(recruitment.snapshot!!, RecruitmentSnapshot::class.java)
        return Recruitment.fromSnapshot(snapshot)
    }
}