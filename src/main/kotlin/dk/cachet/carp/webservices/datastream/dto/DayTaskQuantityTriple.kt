package dk.cachet.carp.webservices.datastream.dto

import java.sql.Date

data class DayTaskQuantityTriple(
    val day: Date,
    val task: String,
    val quantity: Long,
)
