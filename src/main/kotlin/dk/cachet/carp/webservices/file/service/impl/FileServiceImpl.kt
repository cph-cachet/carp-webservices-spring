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
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import dk.cachet.carp.webservices.common.query.QueryVisitor
import dk.cachet.carp.webservices.export.service.ResourceExporter
import dk.cachet.carp.webservices.file.domain.File
import dk.cachet.carp.webservices.file.repository.FileRepository
import dk.cachet.carp.webservices.file.service.FileService
import dk.cachet.carp.webservices.file.service.FileStorage
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Role
import kotlinx.coroutines.Dispatchers
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
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant

@Service
@Transactional
// Extract S3 methods into a separate service
@Suppress("LongParameterList", "TooManyFunctions")
class FileServiceImpl(
    private val fileRepository: FileRepository,
    private val fileStorage: FileStorage,
    private val validateMessages: MessageBase,
    private val s3Client: AmazonS3,
    private val authenticationService: AuthenticationService,
    @Value("\${s3.space.bucket}") private val s3SpaceBucket: String,
    @Value("\${s3.space.endpoint}") private val s3SpaceEndpoint: String,
) : FileService, ResourceExporter<File> {
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
                val specification = RSQLParser().parse(queryForRole).accept(QueryVisitor<File>())
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

    @Deprecated("Use -create- instead")
    override fun createDEPRECATED(
        studyId: String,
        file: MultipartFile,
        metadata: String?,
        ownerId: UUID,
    ): File {
        val relativePath = Path.of("studies", studyId, "deployments", "unknown")
        val filename = fileStorage.storeAtPath(file, relativePath)

        val saved =
            fileRepository.save(
                studyId,
                file,
                filename,
                metadata?.let { json -> ObjectMapper().readTree(json) },
                ownerId.toString(),
                null,
                relativePath.toString(),
            )

        LOGGER.info("File saved (deprecated method), id = ${saved.id}")

        return saved
    }

    override fun create(
        studyId: UUID,
        deploymentId: UUID,
        ownerId: UUID,
        file: MultipartFile,
        metadata: String?,
    ): File {
        val relativePath =
            Path.of("studies", studyId.stringRepresentation, "deployments", deploymentId.stringRepresentation)
        val filename = fileStorage.storeAtPath(file, relativePath)

        val saved =
            fileRepository.save(
                studyId = studyId.stringRepresentation,
                uploadedFile = file,
                fileName = filename,
                metadata = metadata?.let { json -> ObjectMapper().readTree(json) },
                ownerId = ownerId.stringRepresentation,
                deploymentId = deploymentId.stringRepresentation,
                relativePath = relativePath.toString(),
            )

        LOGGER.info("File saved, id = ${saved.id}")

        return saved
    }

    override fun download(
        id: Int,
        studyId: UUID,
    ): Pair<Resource, String> {
        val file = getOne(id)

        val fileToDownload =
            fileStorage.getFileAtPath(
                file.fileName,
                Path.of(file.relativePath),
            )

        return Pair(fileToDownload, file.originalName)
    }

    override fun delete(
        id: Int,
        studyId: UUID,
    ) {
        val file = getOne(id)

        fileStorage.deleteFileAtPath(file.fileName, Path.of(file.relativePath))
        fileRepository.delete(file)

        LOGGER.info("File deleted, id = $id")
    }

    @Suppress("MagicNumber")
    override fun deleteAllOlderThan(days: Int) {
        val clockNow7DaysAgo = System.currentTimeMillis() - days * 24 * 60 * 60 * 1000
        val filesToDelete = fileRepository.getAllByUpdatedAtIsBefore(Instant.ofEpochMilli(clockNow7DaysAgo))

        filesToDelete.forEach { file ->
            fileRepository.delete(file)
            try {
                fileStorage.deleteFileAtPath(file.fileName, Path.of(file.relativePath))
            } catch (e: IOException) {
                LOGGER.error("Failed to delete file with id = ${file.id}", e)
            }
        }

        LOGGER.info("All files older than $days days deleted")
    }

    override suspend fun deleteAllByStudyId(studyId: String) {
        val files =
            withContext(Dispatchers.IO) {
                fileRepository.findByStudyId(studyId)
            }

        files.forEach { fileRepository.deleteById(it.id) }
        files.forEach { fileStorage.deleteFileAtPath(it.fileName, Path.of(it.relativePath)) }

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
            PutObjectRequest(
                s3SpaceBucket,
                filename,
                file.inputStream,
                metadata,
            ).withCannedAcl(CannedAccessControlList.PublicRead),
        )

        return UriComponentsBuilder.fromUriString(s3SpaceEndpoint).pathSegment(s3SpaceBucket).pathSegment(filename)
            .build().toUriString()
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
        getAll(null, studyId.stringRepresentation).onEach {
            val resource =
                fileStorage.getResourceAtPath(
                    it.fileName,
                    Path.of("studies", studyId.stringRepresentation, "deployments", it.deploymentId ?: "unknown"),
                )
            val copyPath = target.resolve(it.originalName)
            Files.copy(resource.file.toPath(), copyPath)
        }
    }
}
