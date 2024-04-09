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
import dk.cachet.carp.webservices.file.domain.File
import dk.cachet.carp.webservices.file.repository.FileRepository
import dk.cachet.carp.webservices.file.service.FileService
import dk.cachet.carp.webservices.file.service.FileStorage
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Role
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.UriComponentsBuilder


@Service
@Transactional
class FileServiceImpl(
    private val fileRepository: FileRepository,
    private val fileStorage: FileStorage,
    private val validateMessages: MessageBase,
    private val s3Client: AmazonS3,
    private val authenticationService: AuthenticationService,
    @Value("\${s3.space.bucket}") private val s3SpaceBucket: String,
    @Value("\${s3.space.endpoint}") private val s3SpaceEndpoint: String
): FileService
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun getAll(query: String?, studyId: String): List<File>
    {
        val account = authenticationService.getAuthentication()
        val isResearcher = account.role!! >= Role.RESEARCHER


        if (isResearcher && query == null)
        {
            return fileRepository.findByStudyId(studyId)
        }
        else
        {
            query?.let {
                val queryForRole = if (!isResearcher)
                     // Return data relevant to this user only.
                    "$query;created_by==${account.id!!};study_id==$studyId"
                else
                {
                    // Return data relevant to this study.
                    "$query;study_id==$studyId"
                }
                val specification = RSQLParser()
                        .parse(queryForRole)
                        .accept(QueryVisitor<File>())
                return fileRepository.findAll(specification)
            }
            return fileRepository.findByStudyIdAndCreatedBy(studyId, account.id!!)
        }
    }

    override fun getAllByStudyIdAndDeploymentId(studyId: String, deploymentId: String): List<File> {
        return fileRepository.findByStudyIdAndDeploymentId(studyId, deploymentId)
    }

    override fun getOne(id: Int): File
    {
        val optionalFile = fileRepository.findById(id)
        if (!optionalFile.isPresent)
        {
            LOGGER.warn("File is not found with id = $id")
            throw ResourceNotFoundException(validateMessages.get("file.not_found", id))
        }
        return optionalFile.get()
    }

    override fun create(studyId: String, file: MultipartFile, metadata: String?): File
    {
        val filename= fileStorage.store(file)

        val saved = fileRepository.save(
                studyId,
                file,
                filename,
                metadata?.let { json -> ObjectMapper().readTree(json) }
        )

        LOGGER.info("File saved, id = ${saved.id}")
        return saved
    }

    override fun delete(id: Int)
    {
        // Find the file by id.
        val file = getOne(id)

        // Delete the file.
        fileStorage.deleteFile(file.storageName)

        // Delete the file from file repository
        fileRepository.delete(file)
        LOGGER.info("File deleted, id = $id")
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
    override fun uploadImage(file: MultipartFile): String
    {
        val extension = StringUtils.getFilenameExtension(file.originalFilename)
        val filename = "${UUID.randomUUID().stringRepresentation}.$extension"

        val metadata = ObjectMetadata()
        metadata.contentLength = file.inputStream.available().toLong()

        if (!file.contentType.isNullOrEmpty()) {
            metadata.contentType = file.contentType
        }

        s3Client.putObject(
            PutObjectRequest(s3SpaceBucket, filename, file.inputStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead)
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
            LOGGER.warn("Ignoring deletion of malformed s3 uri: $url")
            return
        }

        s3Client.deleteObject(
            DeleteObjectRequest(s3SpaceBucket, uri.key)
        )
    }
}