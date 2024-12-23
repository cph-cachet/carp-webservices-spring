package dk.cachet.carp.webservices.file.service.impl

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3URI
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.fasterxml.jackson.databind.ObjectMapper
import cz.jirutka.rsql.parser.RSQLParser
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.common.query.QueryVisitor
import dk.cachet.carp.webservices.export.service.ResourceExporter
import dk.cachet.carp.webservices.file.domain.File
import dk.cachet.carp.webservices.file.repository.FileRepository
import dk.cachet.carp.webservices.file.service.FileService
import dk.cachet.carp.webservices.file.service.FileStorage
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.UriComponentsBuilder
import java.nio.file.Files
import java.nio.file.Path

@Service
@Transactional
// Extract S3 methods into a separate service
@Suppress("LongParameterList")
class FileServiceImpl(
    private val fileRepository: FileRepository,
    private val fileStorage: FileStorage,
    private val validateMessages: MessageBase,
    private val s3Client: AmazonS3,
    private val authenticationService: AuthenticationService,
    private val accountService: AccountService,
    private val authorizationService: AuthorizationService,
    @Value("\${s3.space.bucket}") private val s3SpaceBucket: String,
    @Value("\${s3.space.endpoint}") private val s3SpaceEndpoint: String,
) : FileService, ResourceExporter<File> {
    private val backgroundWorker = CoroutineScope(Dispatchers.IO)

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun getAll(
        query: String?,
        studyId: String,
    ): List<File> {
        val id = authenticationService.getId()
        val role = authenticationService.getRole()
        val isResearcher = role >= Role.RESEARCHER

        if (isResearcher && query == null) {
            return fileRepository.findByStudyId(studyId)
        } else {
            query?.let {
                val queryForRole =
                    if (!isResearcher) {
                        // Return data relevant to this user only.
                        "$query;created_by==$id;study_id==$studyId"
                    } else {
                        // Return data relevant to this study.
                        "$query;study_id==$studyId"
                    }
                val specification =
                    RSQLParser()
                        .parse(queryForRole)
                        .accept(QueryVisitor<File>())
                return fileRepository.findAll(specification)
            }
            return fileRepository.findByStudyIdAndCreatedBy(studyId, id.stringRepresentation)
        }
    }

    override fun getOne(id: Int): File {
        val optionalFile = fileRepository.findById(id)
        if (!optionalFile.isPresent) {
            LOGGER.warn("File is not found with id = $id")
            throw ResourceNotFoundException(validateMessages.get("file.not_found", id))
        }
        return optionalFile.get()
    }

    override fun createDEPRICATED(
        studyId: String,
        file: MultipartFile,
        metadata: String?,
    ): File {
        val filename = fileStorage.storeAtPath(file, Path.of("studies", studyId))

        val saved =
            fileRepository.save(
                studyId,
                file,
                filename,
                metadata?.let { json -> ObjectMapper().readTree(json) },
            )

        LOGGER.info("File saved, id = ${saved.id}")

        val identity = authenticationService.getCarpIdentity()
        backgroundWorker.launch {
            accountService.grant(identity, setOf(Claim.FileOwner(saved.id)))
        }

        return saved
    }

    override fun create(
        studyId: String,
        deploymentId: UUID,
        ownerId: UUID,
        file: MultipartFile,
        metadata: String?,
    ): File {

//        val filename = fileStorage.storeAtPath(file, Path.of("studies", studyId))
//
//        val saved =
//            fileRepository.save(
//                studyId,
//                file,
//                filename,
//                metadata?.let { json -> ObjectMapper().readTree(json) },
//            )
//
//        LOGGER.info("File saved, id = ${saved.id}")
//
//        val identity = authenticationService.getCarpIdentity()
//        backgroundWorker.launch {
//            accountService.grant(identity, setOf(Claim.FileOwner(saved.id)))
//        }
//
//        return saved
    }

    override fun download(
        id: Int,
        studyId: UUID,
    ): Pair<Resource, String> {
        val file = getOne(id)
        val fileToDownload =
            fileStorage.getFileAtPath(
                file.storageName,
                Path.of("studies", studyId.stringRepresentation),
            )

        return Pair(fileToDownload, file.originalName)
    }

    override fun delete(
        id: Int,
        studyId: UUID,
    ) {
        val file = getOne(id)
        fileStorage.deleteFileAtPath(file.storageName, Path.of("studies", studyId.stringRepresentation))
        fileRepository.delete(file)

        LOGGER.info("File deleted, id = $id")

        val identity = authenticationService.getCarpIdentity()
        backgroundWorker.launch {
            accountService.revoke(identity, setOf(Claim.FileOwner(file.id)))
        }
    }

    override suspend fun deleteAllByStudyId(studyId: String) {
        val files =
            withContext(Dispatchers.IO) {
                fileRepository.findByStudyId(studyId)
            }
        val claimsToRemove = HashSet<Claim>()
        files.forEach {
            claimsToRemove.add(Claim.FileOwner(it.id))
        }
        authorizationService.revokeClaimsFromAllAccounts(claimsToRemove)

        files.forEach { fileRepository.deleteById(it.id) }
        files.forEach { fileStorage.deleteFile(it.storageName) }

        LOGGER.info("All files deleted for study, studyId = $studyId")
    }

    /**
     * The function [uploadImage] uploads the image to the configured S3 space.
     * TODO: functions for handling images are compromises due to time limitations.
     *       In an ideal scenario there would be an imageService and controller
     *       handling these kinds of requests. S3 storage is more sophisticated
     *       so it would make sense to handle not only the images, but every file
     *       as s3 objects. It would require to rewrite endpoints that deal with
     *       files though. If one is worried about privacy, there are self-hosted
     *       s3 solutions, which only need to be added to the docker-compose stack
     *
     * @param file The [file] is the file that needs to be uploaded
     * @return The url where you can access the content
     */
    override fun uploadImage(file: MultipartFile): String {
        val extension = StringUtils.getFilenameExtension(file.originalFilename)
        val filename = "${UUID.randomUUID().stringRepresentation}.$extension"

        val metadata = ObjectMetadata()
        metadata.contentLength = file.inputStream.available().toLong()

        if (!file.contentType.isNullOrEmpty()) {
            metadata.contentType = file.contentType
        }

        s3Client.putObject(
            PutObjectRequest(s3SpaceBucket, filename, file.inputStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead),
        )

        return UriComponentsBuilder
            .fromUriString(s3SpaceEndpoint)
            .pathSegment(s3SpaceBucket)
            .pathSegment(filename)
            .build()
            .toUriString()
    }

    override fun deleteImage(url: String) {
        val uri: AmazonS3URI
        try {
            uri = AmazonS3URI(url.replaceBefore(s3SpaceBucket, ""))
            LOGGER.info("Deleting s3 resource with uri: $url")
        } catch (e: IllegalArgumentException) {
            LOGGER.warn("Ignoring deletion of malformed s3 uri: $url", e)
            return
        }

        s3Client.deleteObject(
            DeleteObjectRequest(s3SpaceBucket, uri.key),
        )
    }

    override val dataFileName = "files.json"

    override suspend fun exportDataOrThrow(
        studyId: UUID,
        deploymentIds: Set<UUID>,
        target: Path,
    ) = withContext(Dispatchers.IO) {
        getAll(null, studyId.stringRepresentation)
            .onEach {
                val resource =
                    fileStorage.getResourceAtPath(
                        it.storageName,
                        Path.of("studies", studyId.stringRepresentation),
                    )
                val copyPath = target.resolve(it.originalName)
                Files.copy(resource.file.toPath(), copyPath)
            }
    }
}
