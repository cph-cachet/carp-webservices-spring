package dk.cachet.carp.webservices.common.email.domain

import kotlinx.serialization.Serializable

/**
 * The Data Class [EmailRequest].
 * It represents an object that can be sent and received to and from the message queues.
 */
@Serializable
data class EmailRequest
(
    val address: String,

    val subject: String,

    val message: String
)

@Serializable
data class NotificationRequest
(
    val recipientAccountId: String,

    val subject: String,

    val message: String,

    val deploymentId: String
)