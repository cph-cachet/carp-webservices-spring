package dk.cachet.carp.webservices.datastream.domain

import kotlinx.datetime.Instant

data class DateTaskQuantityTriple(
    val date: Instant,
    val task: String,
    val quantity: Long,
)
