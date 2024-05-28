package dk.cachet.carp.webservices.common.audit

import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import org.springframework.data.domain.AuditorAware
import java.util.*

/**
 * An [AuditorAware] which retrieves the account ID of the currently authenticated user.
 * JPA entities annotated with [EntityListeners] will automatically have their `createdBy` and
 * `lastModifiedBy` fields set.
 */
class EntityAuditorAware(
    private val authenticationService: AuthenticationService,
) : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> =
        try {
            Optional.of(authenticationService.getId().stringRepresentation)
        } catch (e: Exception) {
            Optional.of("SYSTEM")
        }
}
