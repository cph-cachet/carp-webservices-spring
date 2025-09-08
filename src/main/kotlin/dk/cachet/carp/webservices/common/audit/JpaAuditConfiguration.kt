package dk.cachet.carp.webservices.common.audit

import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * The configuration class [JpaAuditConfiguration].
 * The [JpaAuditConfiguration] implements the configuration logic for the [EntityAuditorAware].
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "dateTimeProvider")
class JpaAuditConfiguration(private val authenticationService: AuthenticationService) {
    /**
     * The function [auditorAware] returns the current auditor of the application.
     */
    @Bean("auditorAware")
    fun auditorAware(): AuditorAware<String> = EntityAuditorAware(authenticationService)

    /**
     * The function [dateTimeProvider] returns the current date and time.
     */
    @Bean("dateTimeProvider")
    fun dateTimeProvider(): EntityDateTimeProvider = EntityDateTimeProvider()
}
