package dk.cachet.carp.webservices.dataPoint.controller

import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.common.constants.RequestParamName
import dk.cachet.carp.webservices.common.query.QueryUtil
import dk.cachet.carp.webservices.dataPoint.controller.DataPointController.Companion.DATA_POINT_BASE
import dk.cachet.carp.webservices.dataPoint.domain.DataPoint
import dk.cachet.carp.webservices.dataPoint.dto.CreateDataPointRequestDto
import dk.cachet.carp.webservices.dataPoint.service.IDataPointService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping(value = [DATA_POINT_BASE])
class DataPointController(private val dataPointService: IDataPointService)
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val DATA_POINT_BASE = "/api/deployments/{${PathVariableName.DEPLOYMENT_ID}}/data-points"
        const val GET_DATAPOINT_BY_ID = "/{${PathVariableName.DATA_POINT_ID}}"
        const val BATCH = "/batch"
        const val COUNT = "/count"

        /** Others */
        const val DEFAULT_PAGE_SIZE = 250000
    }

    @GetMapping
    @PreAuthorize("@dataPointAuthorizationService.canCreateDataPoint(#deploymentId)")
    @Operation(tags = ["dataPoint/getAll.json"])
    fun getAll(
            @RequestParam(RequestParamName.PAGE, required = false) page: Int?,
            @RequestParam(RequestParamName.QUERY, required = false) query: String?,
            @RequestParam(RequestParamName.SORT, required = false) sort: String?,
            @PathVariable(PathVariableName.DEPLOYMENT_ID, required = true) deploymentId: String): List<DataPoint>
    {
        LOGGER.info("Start GET: /api/deployments/$deploymentId/data-points")
        val pageRequest = PageRequest.of(page ?: 0, DEFAULT_PAGE_SIZE, QueryUtil.sort(sort))
        return runBlocking { dataPointService.getAll(deploymentId, pageRequest, query) }
    }

    @GetMapping(value = [GET_DATAPOINT_BY_ID])
    @PreAuthorize("@dataPointAuthorizationService.canViewDataPoint(#deploymentId, #dataPointId)")
    @Operation(tags = ["dataPoint/getOne.json"])
    fun getOne(
            @PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: String,
            @PathVariable(PathVariableName.DATA_POINT_ID) dataPointId: Int): DataPoint
    {
        LOGGER.info("Start GET: /api/deployments/$deploymentId/data-points/$dataPointId")
        return dataPointService.getOne(dataPointId)
    }

    @PostMapping
    @PreAuthorize("@dataPointAuthorizationService.canCreateDataPoint(#deploymentId)")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(tags = ["dataPoint/create.json"])
    fun create(
            @PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: String,
            @RequestPart file: MultipartFile?,
            @Valid @RequestBody request: CreateDataPointRequestDto): DataPoint
    {
        LOGGER.info("Start POST: /api/deployments/$deploymentId/data-points")
        return dataPointService.create(deploymentId, file, request)
    }

    @PostMapping(value = [BATCH])
    @PreAuthorize("@dataPointAuthorizationService.canCreateDataPoint(#deploymentId)")
    @Operation(tags = ["dataPoint/createMany.json"])
    fun createMany(
            @PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: String,
            @RequestPart file: MultipartFile)
    {
        LOGGER.info("Start POST: /api/deployments/$deploymentId/data-points/batch")
        dataPointService.createMany(file, deploymentId)
    }

    @DeleteMapping(value = [GET_DATAPOINT_BY_ID])
    @PreAuthorize("@dataPointAuthorizationService.canViewDataPoint(#deploymentId, #dataPointId)")
    @Operation(tags = ["dataPoint/delete.json"])
    fun delete(
            @PathVariable(PathVariableName.DEPLOYMENT_ID) deploymentId: String,
            @PathVariable(PathVariableName.DATA_POINT_ID) dataPointId: Int)
    {
        LOGGER.info("Start DELETE: /api/deployments/$deploymentId/data-points/$dataPointId")
        dataPointService.delete(dataPointId)
    }

    @GetMapping(value = [COUNT])
    @PreAuthorize("@dataPointAuthorizationService.canCreateDataPoint(#deploymentId)")
    @Operation(tags = ["dataPoint/count.json"])
    fun count(
            @RequestParam(RequestParamName.QUERY, required = false) query: String?,
            @PathVariable(PathVariableName.DEPLOYMENT_ID, required = true) deploymentId: String): Long
    {
        LOGGER.info("Start GET: /api/deployments/$deploymentId/data-points/$COUNT")
        return dataPointService.getNumberOfDataPoints(deploymentId, query)
    }
}