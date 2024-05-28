package dk.cachet.carp.webservices.deployment.repository

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.deployments.domain.DeploymentRepository
import dk.cachet.carp.deployments.domain.StudyDeployment
import dk.cachet.carp.deployments.domain.StudyDeploymentSnapshot
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CoreDeploymentRepository(
    private val studyDeploymentRepository: StudyDeploymentRepository,
    private val objectMapper: ObjectMapper,
    private val validationMessages: MessageBase,
) : DeploymentRepository {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
        private const val VERSION: Int = 0
    }

    override suspend fun add(studyDeployment: StudyDeployment) =
        withContext(Dispatchers.IO) {
            if (studyDeploymentRepository.findByDeploymentId(studyDeployment.id.stringRepresentation).isPresent) {
                LOGGER.warn("Deployment already exists, id: ${studyDeployment.id.stringRepresentation}")
                throw IllegalArgumentException(
                    validationMessages.get(
                        "deployment.add.study_deployment.exists",
                        studyDeployment.id.stringRepresentation,
                    ),
                )
            }
            val studyDeploymentToSave = dk.cachet.carp.webservices.deployment.domain.StudyDeployment()

            val snapshot = StudyDeploymentSnapshot.fromDeployment(studyDeployment, VERSION)
            studyDeploymentToSave.snapshot = objectMapper.valueToTree(snapshot)

            studyDeploymentRepository.save(studyDeploymentToSave)
            LOGGER.info("Deployment saved, id: ${studyDeployment.id.stringRepresentation}")
        }

    override suspend fun getStudyDeploymentBy(id: UUID): StudyDeployment? =
        withContext(Dispatchers.IO) {
            val result = getWSDeploymentById(id) ?: return@withContext null
            val snapshot = objectMapper.treeToValue(result.snapshot, StudyDeploymentSnapshot::class.java)
            StudyDeployment.fromSnapshot(snapshot)
        }

    override suspend fun getStudyDeploymentsBy(ids: Set<UUID>): List<StudyDeployment> =
        withContext(Dispatchers.IO) {
            val idStrings = ids.map { it.toString() }.toSet()
            studyDeploymentRepository.findAllByStudyDeploymentIds(idStrings).map { mapWSDeploymentToCore(it) }
        }

    override suspend fun remove(studyDeploymentIds: Set<UUID>): Set<UUID> =
        withContext(Dispatchers.IO) {
            val ids = studyDeploymentIds.map { it.stringRepresentation }.toSet()
            val idsPresent =
                studyDeploymentRepository.findAllByStudyDeploymentIds(ids)
                    .map { mapWSDeploymentToCore(it).id.stringRepresentation }
            studyDeploymentRepository.deleteByDeploymentIds(idsPresent)
            LOGGER.info("Deployments removed with ids: ${idsPresent.joinToString(", ")}")
            idsPresent.map { UUID(it) }.toSet()
        }

    override suspend fun update(studyDeployment: StudyDeployment) =
        withContext(Dispatchers.IO) {
            val deploymentId = studyDeployment.id
            val stored = getWSDeploymentById(deploymentId)

            checkNotNull(stored) {
                LOGGER.warn("Deployment is not found, id: ${deploymentId.stringRepresentation}")
                validationMessages.get(
                    "deployment.update.study_deployment.not_found",
                    deploymentId.stringRepresentation,
                )
            }

            val snapshot = StudyDeploymentSnapshot.fromDeployment(studyDeployment, VERSION)
            stored.snapshot = objectMapper.valueToTree(snapshot)

            studyDeploymentRepository.save(stored)
            LOGGER.info("Deployment updated, id: ${studyDeployment.id.stringRepresentation}")
        }

    fun getWSDeploymentById(id: UUID): dk.cachet.carp.webservices.deployment.domain.StudyDeployment? {
        val optionalResult = studyDeploymentRepository.findByDeploymentId(id.stringRepresentation)
        if (!optionalResult.isPresent) {
            LOGGER.info("Deployment is not found, id: ${id.stringRepresentation}")
            return null
        }
        return optionalResult.get()
    }

    private fun mapWSDeploymentToCore(deployment: dk.cachet.carp.webservices.deployment.domain.StudyDeployment): StudyDeployment {
        val snapshot = objectMapper.treeToValue(deployment.snapshot, StudyDeploymentSnapshot::class.java)
        return StudyDeployment.fromSnapshot(snapshot)
    }
}
