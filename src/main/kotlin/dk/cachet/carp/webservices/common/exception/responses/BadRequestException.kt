package dk.cachet.carp.webservices.common.exception.responses

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * The Class [BadRequestException].
 * The [BadRequestException] is thrown when a request is invalid or missing a required parameter(s).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
class BadRequestException: RuntimeException
{
    companion object
    {
        private const val serialVersionUID = 1L
        private const val INVALID_PARAM_MSG = "Invalid value [%s] found for parameter [%s]."
        private const val INVALID_DATE_PARAM_MSG = "Invalid value [%s] found for parameter [%s]. Expected date format is [%s]"
    }

    /**
     * Instantiates a new BadRequestException exception.
     */
    constructor() : super()

    /**
     * Instantiates a new BadRequestException exception.
     *
     * @param [paramName] The [paramName] of the request.
     * @param [value] The [value] of the error response.
     */
    constructor(paramName: String?, value: String?) : super(String.format(INVALID_PARAM_MSG, value, paramName))

    /**
     * Instantiates a new BadRequestException exception.
     *
     * @param [paramName] The [paramName] of the request.
     * @param [value] The [value] of the error response.
     * @param [format] The [format].
     */
    constructor(paramName: String?, value: String?, format: String?) : super(String.format(INVALID_DATE_PARAM_MSG, value, paramName, format))

    /**
     * Instantiates a new BadRequestException exception.
     *
     * @param message The [message] of exception.
     */
    constructor(message: String?) : super(message)

    /**
     * Instantiates a new BadRequestException exception.
     *
     * @param message The [message] of exception.
     * @param cause The [cause] of exception.
     */
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    /**
     * Instantiates a new BadRequestException exception.
     *
     * @param cause The [cause] of exception.
     */
    constructor(cause: Throwable?) : super(cause)
}