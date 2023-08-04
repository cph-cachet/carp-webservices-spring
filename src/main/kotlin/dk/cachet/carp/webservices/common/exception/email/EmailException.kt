package dk.cachet.carp.webservices.common.exception.email

/**
 * The Class [EmailException].
 * The [EmailException] is thrown when the email address is invalid or the SMTP server cannot be reached.
 */
class EmailException(message: String?): RuntimeException(message)