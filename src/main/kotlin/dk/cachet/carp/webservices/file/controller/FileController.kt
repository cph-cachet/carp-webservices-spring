package dk.cachet.carp.webservices.file.controller

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.constants.RequestParamName
import dk.cachet.carp.webservices.file.domain.File
import dk.cachet.carp.webservices.file.service.FileService
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
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
class FileController(private val fileService: FileService, private val authenticationService: AuthenticationService) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val FILE_BASE = "/api/studies/{${PathVariableName.STUDY_ID}}/files"
        const val UPLOAD_IMAGE = "/api/studies/{${PathVariableName.STUDY_ID}}/images"
        const val DOWNLOAD = "$FILE_BASE/{${PathVariableName.FILE_ID}}/download"
        const val FILE_ID = "$FILE_BASE/{${PathVariableName.FILE_ID}}"
        const val CREATE = "$FILE_BASE/{${PathVariableName.DEPLOYMENT_ID}}"
    }

    @GetMapping(FILE_ID)
    @Operation(tags = ["file/getOne.json"])
    @PreAuthorize("canManageStudy(#studyId) or @fileControllerAuthorizer.isFileOwner(#fileId)")
    fun getOne(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @PathVariable(PathVariableName.FILE_ID) fileId: Int,
    ): File {
        LOGGER.info("Start GET: /api/studies/$studyId/files/$fileId")
        return fileService.getOne(fileId)
    }

    @GetMapping(FILE_BASE)
    @Operation(tags = ["file/getAll.json"])
    @PreAuthorize("canManageStudy(#studyId)")
    fun getAll(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @RequestParam(RequestParamName.QUERY) query: String?,
    ): List<File> {
        LOGGER.info("Start GET: /api/studies/$studyId/files")
        return fileService.getAll(query, studyId.stringRepresentation)
    }

    @GetMapping(
        produces = [
            MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
        ],
        value = [DOWNLOAD],
    )
    @ResponseBody
    @Operation(tags = ["file/download.json"])
    @PreAuthorize("canManageStudy(#studyId) or @fileControllerAuthorizer.isFileOwner(#id)")
    fun download(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @PathVariable(PathVariableName.FILE_ID) id: Int,
    ): ResponseEntity<Resource> {
        LOGGER.info("Start GET: /api/studies/$studyId/files/$id/download")
        val (fileToDownload, originalFilename) = fileService.download(id, studyId)

        return ResponseEntity.ok().header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"$originalFilename\"",
        ).body(fileToDownload)
    }

    @PostMapping(
        consumes = [
            MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
        ],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        value = [FILE_BASE],
    )
    @Operation(tags = ["file/create_deprecated.json"])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("canManageStudy(#studyId) or isInDeploymentOfStudy(#studyId)")
    @Deprecated("Use the other -create- method instead.")
    fun createDEPRICATED(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @RequestParam(RequestParamName.METADATA, required = false) metadata: String?,
        @RequestPart file: MultipartFile,
    ): File {
        LOGGER.info("Start POST: /api/studies/$studyId/files")
        val ownerId = authenticationService.getId()

        return fileService.createDEPRECATED(studyId.stringRepresentation, file, metadata, ownerId)
    }

    @PostMapping(
        consumes = [
            MediaType.MULTIPART_FORM_DATA_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
        ],
        produces = [MediaType.APPLICATION_JSON_VALUE],
        value = [CREATE],
    )
    @Operation(tags = ["file/create.json"])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("canManageStudy(#studyId) or isInDeploymentOfStudy(#studyId)")
    fun create(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: UUID,
        @RequestParam(RequestParamName.METADATA, required = false) metadata: String?,
        @RequestPart file: MultipartFile,
    ): File {
        LOGGER.info("Start POST: /api/studies/$studyId/files/$deploymentId")
        val ownerId = authenticationService.getId()

        return fileService.create(studyId, deploymentId, ownerId, file, metadata)
    }

    @DeleteMapping(FILE_ID)
    @Operation(tags = ["file/delete.json"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("canManageStudy(#studyId) or @fileControllerAuthorizer.isFileOwner(#fileId)")
    fun delete(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @PathVariable(PathVariableName.FILE_ID) fileId: Int,
    ) {
        LOGGER.info("Start DELETE: /api/studies/$studyId/files/$fileId")
        fileService.delete(fileId, studyId)
    }

    @PostMapping(UPLOAD_IMAGE)
    @Operation(tags = ["file/uploadImage.json"])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("canManageStudy(#studyId) or isInDeploymentOfStudy(#studyId)")
    fun uploadS3(
        @PathVariable(PathVariableName.STUDY_ID) studyId: UUID,
        @RequestParam(RequestParamName.IMAGE, required = true) image: MultipartFile,
    ): String {
        LOGGER.info("Start PUT: /api/studies/$studyId/images")
        return fileService.uploadImage(image)
    }
}
