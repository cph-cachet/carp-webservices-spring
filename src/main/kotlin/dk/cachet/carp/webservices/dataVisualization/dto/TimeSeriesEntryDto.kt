package dk.cachet.carp.webservices.dataVisualization.dto;

import kotlinx.datetime.Instant
import java.util.HashMap

data class TimeSeriesEntryDto(
    val x: Instant,
    val y: HashMap<String, Double>
)
