package dk.cachet.carp.webservices.common.audit

import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.data.domain.AuditorAware
import java.util.*

/**
 * An [AuditorAware] which retrieves the account ID of the currently authenticated user.
 * JPA entities annotated with [EntityListeners] will automatically have their `createdBy` and
 * `lastModifiedBy` fields set.
 */
@Suppress("TooGenericExceptionCaught")
class EntityAuditorAware(
    private val authenticationService: AuthenticationService,
) : AuditorAware<String> {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override fun getCurrentAuditor(): Optional<String> =
        try {
            Optional.of(authenticationService.getId().stringRepresentation)
        } catch (e: Throwable) {
            LOGGER.warn("Cannot get entity auditor.", e)
            Optional.of("SYSTEM")
        }
}
