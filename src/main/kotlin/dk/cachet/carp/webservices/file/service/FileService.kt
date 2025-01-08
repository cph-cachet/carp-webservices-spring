
package dk.cachet.carp.webservices.file.service

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.file.domain.File
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

/**
 * The Interface [FileService].
 * The [FileService] creates an interface for handling file requests.
 */
interface FileService {
    /** The [delete] interface for deleting a file. */
    fun delete(
        id: Int,
        studyId: UUID,
    )

    suspend fun deleteAllByStudyId(studyId: String)

    /** The [createDEPRECATED] interface for creating a file. */
    fun createDEPRECATED(
        studyId: String,
        file: MultipartFile,
        metadata: String?,
    ): File

    /** The [create] interface for creating a file. */
    fun create(
        studyId: UUID,
        deploymentId: UUID,
        ownerId: UUID,
        file: MultipartFile,
        metadata: String?,
    ): File

    /** The [download] interface for downloading a file for a given id and study id. */
    fun download(
        id: Int,
        studyId: UUID,
    ): Pair<Resource, String>

    /** The [getOne] interface for retrieving a file for a given id. */
    fun getOne(id: Int): File

    /** The [getAll] interface for retrieving all files for a given query and study id. */
    fun getAll(
        query: String?,
        studyId: String,
    ): List<File>

    fun uploadImage(file: MultipartFile): String

    fun deleteImage(url: String)
}
