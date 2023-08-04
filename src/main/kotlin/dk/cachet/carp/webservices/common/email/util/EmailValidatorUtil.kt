package dk.cachet.carp.webservices.common.email.util

import org.springframework.stereotype.Component
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * The Class [EmailValidatorUtil].
 * The [EmailValidatorUtil] provides the email validation functionality.
 */
@Component
class EmailValidatorUtil
{
    /**
     * The [emailPattern] instantiates the email [Pattern].
     */
    private val emailPattern: Pattern = Pattern.compile(EMAIL_PATTERN)

    /**
     * The [matcher] instantiates the email [Matcher].
     */
    private lateinit var matcher: Matcher

    companion object
    {
        private const val EMAIL_PATTERN =
                ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
    }

    /**
     * The function [isValid] enables validation of the email against the regular expression pattern.
     *
     * @param [email] The [email] as hex for validation.
     * @return [Boolean] `true` if the email is valid, `false` otherwise.
     */
    fun isValid(email: String?): Boolean
    {
        if (email == null) return false
        matcher = emailPattern.matcher(email)
        return matcher.matches()
    }
}