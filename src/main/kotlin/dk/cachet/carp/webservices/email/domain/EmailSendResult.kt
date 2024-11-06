package dk.cachet.carp.webservices.email.domain

/**
 *  The Enum [EmailSendResult].
 *  This indicates the end result of an email sending operation.
 */
enum class EmailSendResult(val status: Int) {
    SUCCESS(1),
    FAILURE(2),
}
