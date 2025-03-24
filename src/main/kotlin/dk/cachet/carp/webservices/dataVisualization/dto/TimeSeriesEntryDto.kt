package dk.cachet.carp.webservices.dataVisualization.dto;

import java.sql.Date
import java.util.HashMap

data class TimeSeriesEntryDto(
    val x: Date,
    val y: HashMap<String, Long>
)
