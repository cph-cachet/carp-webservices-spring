package dk.cachet.carp.webservices.email.domain

import kotlinx.serialization.Serializable

/**
 * The Data Class [EmailRequest].
 * It represents an object that can be sent and received to and from the message queues.
 */
@Serializable
data class EmailRequest(
    /** The email request [id]. */
    val id: String,
    /** The email request [destinationEmail]. */
    val destinationEmail: String,
    /** The email request [subject]. */
    val subject: String,
    /** The email request [content]. */
    val content: String,
    /** The email request [cc]. */
    val cc: List<String> = listOf(),
)
