
package dk.cachet.carp.webservices.file.service

import dk.cachet.carp.webservices.file.domain.File
import org.springframework.web.multipart.MultipartFile

/**
 * The Interface [FileService].
 * The [FileService] creates an interface for handling file requests.
 */
interface FileService
{
    /** The [delete] interface for deleting a file. */
    fun delete(id: Int)

    /** The [create] interface for creating a file. */
    fun create(studyId: String, file: MultipartFile, metadata: String?): File

    /** The [getOne] interface for retrieving a file for a given id. */
    fun getOne(id: Int): File

    /** The [getAll] interface for retrieving all files for a given query and study id. */
    fun getAll(query: String?, studyId: String): List<File>

    /** The [getAllByStudyIdAndDeploymentId] interface for retrieving all files for a given study id and deployment id. */
    fun getAllByStudyIdAndDeploymentId(studyId: String, deploymentId: String): List<File>

    fun uploadImage(file: MultipartFile): String

    fun deleteImage(url: String)
}