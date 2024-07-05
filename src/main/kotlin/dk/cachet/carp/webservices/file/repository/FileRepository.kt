package dk.cachet.carp.webservices.file.repository

import com.fasterxml.jackson.databind.JsonNode
import dk.cachet.carp.webservices.file.domain.File
import org.springframework.context.annotation.Lazy
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Repository
interface FileRepository : JpaRepository<File, Int>, JpaSpecificationExecutor<File>, FileRepositoryCustom {
    fun findByStudyIdAndCreatedBy(
        studyId: String,
        createdBy: String,
    ): List<File>

    fun findByStudyId(studyId: String): List<File>

    @Modifying
    @Transactional
    @Query(
        nativeQuery = true,
        value = "DELETE FROM files WHERE study_id = ?1",
    )
    fun deleteByStudyId(studyId: String)
}

// TODO: This is not a repository, dont't be mislead by that, it needs to be moved to its own service somewhere else.
interface FileRepositoryCustom {
    /**
     * The [save] interface inserters the files in the filesystem.
     *
     * @param studyId The [studyId] of the study.
     * @param uploadedFile The [uploadedFile] in a multipart request.
     * @param fileName The [fileName] of the file.
     * @param metadata The [metadata] of the file.
     */
    fun save(
        studyId: String,
        uploadedFile: MultipartFile,
        fileName: String,
        metadata: JsonNode?,
    ): File
}

@Repository
class FileRepositoryImpl(
    @Lazy private val fileRepository: FileRepository,
) : FileRepositoryCustom {
    /**
     * The [save] function saves the files to the filesystem.
     *
     * @param studyId The [studyId] of the study to save the file.
     * @param uploadedFile The [uploadedFile] to be saved to the filesystem.
     * @param fileName The [fileName] of the file.
     */
    override fun save(
        studyId: String,
        uploadedFile: MultipartFile,
        fileName: String,
        metadata: JsonNode?,
    ): File {
        val file =
            File(
                studyId = studyId,
                storageName = fileName,
                originalName = uploadedFile.originalFilename!!,
                metadata = metadata,
            )
        return fileRepository.save(file)
    }
}
