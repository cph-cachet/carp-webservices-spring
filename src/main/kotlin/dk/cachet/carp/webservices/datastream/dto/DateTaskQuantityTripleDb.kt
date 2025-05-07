package dk.cachet.carp.webservices.datastream.dto

import java.sql.Timestamp

data class DateTaskQuantityTripleDb(
    val date: Timestamp,
    val task: String,
    val quantity: Long,
)
