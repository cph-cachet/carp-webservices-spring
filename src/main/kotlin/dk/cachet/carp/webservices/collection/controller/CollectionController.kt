package dk.cachet.carp.webservices.collection.controller

import dk.cachet.carp.webservices.collection.controller.CollectionController.Companion.COLLECTION_BASE
import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.collection.dto.CollectionCreateRequestDto
import dk.cachet.carp.webservices.collection.dto.CollectionUpdateRequestDto
import dk.cachet.carp.webservices.collection.service.ICollectionService
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.constants.RequestParamName
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value= [COLLECTION_BASE])
class CollectionController(private val collectionService: ICollectionService)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val COLLECTION_BASE = "/api/studies/{${PathVariableName.STUDY_ID}}/collections"
        const val GET_COLLECTION_BY_ID = "/id/{${PathVariableName.COLLECTION_ID}}"
        const val GET_COLLECTION_BY_NAME = "/{${PathVariableName.COLLECTION_NAME}}"
        const val GET_COLLECTION_BY_DEPLOYMENT_ID = "/deployments/{${PathVariableName.DEPLOYMENT_ID}}"
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@collectionAuthorizationService.canViewCollection(#studyId)")
    fun create(
            @PathVariable(PathVariableName.STUDY_ID) studyId: String,
            @Valid @RequestBody request: CollectionCreateRequestDto): Collection
    {
        LOGGER.info("Start POST: /api/studies/$studyId/collections")
        return collectionService.create(request, studyId, request.deploymentId)
    }

    @GetMapping(value = [GET_COLLECTION_BY_ID])
    @PreAuthorize("@collectionAuthorizationService.canViewCollection(#studyId)")
    @Operation(tags = ["collection/getByStudyIdAndCollectionId.json"])
    fun getByStudyIdAndCollectionId(
            @PathVariable(PathVariableName.STUDY_ID) studyId: String,
            @PathVariable(PathVariableName.COLLECTION_ID) collectionId: Int): Collection
    {
        LOGGER.info("Start GET: /api/studies/$studyId/collections/id/$collectionId")
        return collectionService.getCollectionByStudyIdAndId(studyId, collectionId)
    }

    @GetMapping(value = [GET_COLLECTION_BY_NAME])
    @PreAuthorize("@collectionAuthorizationService.canViewCollection(#studyId)")
    @Operation(tags = ["collection/getByStudyIdAndCollectionName.json"])
    fun getByStudyIdAndCollectionName(
            @PathVariable(PathVariableName.STUDY_ID) studyId: String,
            @PathVariable(PathVariableName.COLLECTION_NAME) collectionName: String): Collection
    {
        LOGGER.info("Start GET: /api/studies/$studyId/collections/$collectionName")
        return collectionService.getCollectionByStudyIdAndByName(studyId, collectionName)
    }

    @GetMapping
    @PreAuthorize("@collectionAuthorizationService.canViewCollection(#studyId)")
    @Operation(tags = ["collection/getAll.json"])
    fun getAll(
            @PathVariable(PathVariableName.STUDY_ID) studyId: String,
            @RequestParam(RequestParamName.QUERY, required = true) query: String?): List<Collection>
    {
        LOGGER.info("Start GET: /api/studies/$studyId/collections?query=$query")
        return collectionService.getAll(studyId, query)
    }

    @GetMapping(value = [GET_COLLECTION_BY_DEPLOYMENT_ID])
    @PreAuthorize("@collectionAuthorizationService.canViewCollection(#studyId)")
    fun getByStudyIdAndDeploymentId(
            @PathVariable(PathVariableName.STUDY_ID) studyId: String,
            @PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: String): List<Collection>
    {
        LOGGER.info("Start GET: /api/studies/$studyId/deployments/$deploymentId/collections")
        return collectionService.getAllByStudyIdAndDeploymentId(studyId, deploymentId)
    }

    @DeleteMapping(value = [GET_COLLECTION_BY_ID])
    @PreAuthorize("@collectionAuthorizationService.canModifyCollection(#studyId, #collectionId)")
    @Operation(tags = ["collection/delete.json"])
    fun delete(
            @PathVariable(PathVariableName.STUDY_ID) studyId: String,
            @PathVariable(PathVariableName.COLLECTION_ID) collectionId: Int)
    {
        LOGGER.info("Start DELETE: /api/studies/$studyId/collections/id/$collectionId")
        collectionService.delete(studyId, collectionId)
    }

    @PutMapping(value = [GET_COLLECTION_BY_ID])
    @PreAuthorize("@collectionAuthorizationService.canModifyCollection(#studyId, #collectionId)")
    @Operation(tags = ["collection/update.json"])
    fun update(
            @PathVariable(PathVariableName.STUDY_ID) studyId: String,
            @PathVariable(PathVariableName.COLLECTION_ID) collectionId: Int,
            @Valid @RequestBody request: CollectionUpdateRequestDto): Collection
    {
        LOGGER.info("Start PUT: /api/studies/$studyId/collections/id/$collectionId")
        return collectionService.update(studyId, collectionId, request)
    }
}