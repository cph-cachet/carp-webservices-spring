package dk.cachet.carp.webservices.dataVisualization.controller;

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.dataVisualization.dto.BarChartDataDto
import dk.cachet.carp.webservices.dataVisualization.service.DataVisualizationService
import kotlinx.datetime.Instant
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/data-visualization")
class DataVisualizationController(
    val dataVisualizationService: DataVisualizationService
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        /** Endpoint URI constants */
        const val BAR_CHART = "/bar-chart"
    }

    @GetMapping(value = [BAR_CHART])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("canManageStudy(#studyId)")
    suspend fun getBarChartData(
        @RequestParam("studyId", required = true) studyId: UUID,
        @RequestParam("deploymentId", required = true) deploymentId: UUID,
        @RequestParam("participantId", required = true) participantId: UUID,
        @RequestParam("scope", required = true) scope: String,
        @RequestParam("type", required = true) type: String,
        @RequestParam("from", required = true) from: Long,
        @RequestParam("to", required = true) to: Long
    ): BarChartDataDto {
        val validScopes = setOf("study", "deployment", "participant")
        val validTypes = setOf("survey", "health", "cognition", "image", "audio", "video")

        //todo redezign this
        if (scope !in validScopes)
            throw IllegalArgumentException("Invalid scope: $scope. Allowed values: $validScopes")

        if (type !in validTypes)
            throw IllegalArgumentException("Invalid type: $type. Allowed values: $validTypes")



        LOGGER.info(
            "Start GET: /api/data-visualization/bar-chart" +
                    "?studyId=$studyId&deploymentId=$deploymentId&participantId=$participantId&scope=$scope&type=$type&from=$from&to=$to"
        )

        return dataVisualizationService.getBarChartData(studyId, deploymentId, participantId, scope, type, from, to)
    }
}
