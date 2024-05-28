package dk.cachet.carp.webservices.common.environment

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EnvironmentUtil(
    @Value("\${environment.url}") val url: String,
    @Value("\${environment.portalUrl}") val portalUrl: String,
    @Value("\${spring.profiles.active}") private val _profile: String,
) {
    val profile: EnvironmentProfile by lazy {
        EnvironmentProfile.getEnvironmentProfile(_profile)
    }
}
