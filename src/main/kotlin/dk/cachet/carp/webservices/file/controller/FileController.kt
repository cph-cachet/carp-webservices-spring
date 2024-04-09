package dk.cachet.carp.webservices.file.controller

import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.constants.RequestParamName
import dk.cachet.carp.webservices.file.domain.File
import dk.cachet.carp.webservices.file.service.FileService
import dk.cachet.carp.webservices.file.service.FileStorage
import io.swagger.v3.oas.annotations.Operation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
class FileController(private val fileStorage: FileStorage, private val fileService: FileService)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val FILE_BASE = "/api/studies/{${PathVariableName.STUDY_ID}}/files"
        const val UPLOAD_IMAGE = "/api/studies/{${PathVariableName.STUDY_ID}}/images"
        const val DOWNLOAD = "$FILE_BASE/{${PathVariableName.FILE_ID}}/download"
        const val FILE_ID = "$FILE_BASE/{${PathVariableName.FILE_ID}}"

        /** NOTE: make sure to refactor this, it sounds like this
         *        was supposed to be a query param to an existing endpoint */
        const val GET_BY_DEPLOYMENT_ID =
            "/api/studies/{${PathVariableName.STUDY_ID}}/deployments/{${PathVariableName.DEPLOYMENT_ID}}/files"
    }

    @GetMapping(FILE_BASE)
    @Operation(tags = ["file/getAll.json"])
    @PreAuthorize("#{false}")
    fun getAll(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String,
        @RequestParam(RequestParamName.QUERY) query: String?): List<File>
    {
        LOGGER.info("Start GET: /api/studies/$studyId/files")
        return fileService.getAll(query, studyId)
    }

    @GetMapping(GET_BY_DEPLOYMENT_ID)
    @PreAuthorize("#{false}")
    fun getByStudyIdAndDeploymentId(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String,
        @PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: String): List<File>
    {
        LOGGER.info("Start GET: /api/studies/$studyId/deployments/$deploymentId/files")
        return fileService.getAllByStudyIdAndDeploymentId(studyId, deploymentId)
    }

    @GetMapping(FILE_ID)
    @Operation(tags = ["file/getOne.json"])
    @PreAuthorize("#{false}")
    fun getOne(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String,
        @PathVariable(PathVariableName.FILE_ID) fileId: Int): File
    {
        LOGGER.info("Start GET: /api/studies/$studyId/files/$fileId")
        return fileService.getOne(fileId)
    }

    @GetMapping(produces = [MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE], value = [DOWNLOAD])
    @ResponseBody
    @Operation(tags = ["file/download.json"])
    @PreAuthorize("#{false}")
    fun download(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String,
        @PathVariable(PathVariableName.FILE_ID) id: Int): ResponseEntity<Resource>
    {
        LOGGER.info("Start GET: /api/studies/$studyId/files/$id/download")
        val file = fileService.getOne(id)
        val fileToDownload = fileStorage.getFile(file.storageName)
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.originalName + "\"").body<Resource>(fileToDownload)
    }

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE],
                 produces = [MediaType.APPLICATION_JSON_VALUE],
                 value = [FILE_BASE])
    @Operation(tags = ["file/create.json"])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#{false}")
    fun create(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String,
        @RequestParam(RequestParamName.DEPLOYMENT_ID, required = false) deploymentId: String?,
        @RequestParam(RequestParamName.METADATA, required = false) metadata: String?,
        @RequestPart file: MultipartFile): File
    {
        LOGGER.info("Start POST: /api/studies/$studyId/files")
        return fileService.create(studyId, file, metadata)
    }

    @DeleteMapping(FILE_ID)
    @Operation(tags = ["file/delete.json"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#{false}")
    fun delete(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String,
        @PathVariable(PathVariableName.FILE_ID) fileId: Int)
    {
        LOGGER.info("Start DELETE: /api/studies/$studyId/files/$fileId")
        fileService.delete(fileId)
    }

    @PostMapping(UPLOAD_IMAGE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#{false}")
    fun uploadS3(
        @PathVariable(PathVariableName.STUDY_ID) studyId: String,
        @RequestParam(RequestParamName.IMAGE, required = true) image: MultipartFile): String
    {
        LOGGER.info("Start PUT: /api/studies/$studyId/images")
        return fileService.uploadImage(image)
    }
}