package dk.cachet.carp.webservices.dataVisualization.dto

import java.sql.Date

data class DayKeyQuantityTriple(
    val day: Date, val key: String, val quantity: Long
)