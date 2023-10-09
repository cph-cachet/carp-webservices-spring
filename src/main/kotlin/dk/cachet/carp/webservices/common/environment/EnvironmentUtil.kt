package dk.cachet.carp.webservices.common.environment

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.*


@Component
class EnvironmentUtil(
    @Value("\${spring.profiles.active}") private val ACTIVE_PROFILE: String,
    @Value("\${environment.server}") private val BASE_SERVER: String,
    @Value("\${environment.subfolder}") private val SUBFOLDER: String,
    @Value("\${environment.portal-subfolder}") private val PORTAL_SUBFOLDER: String,
    @Value("\${server.port}") private val SERVER_PORT: String
)
{
    val portalUrl: URI by lazy {
        UriComponentsBuilder
            .fromUriString(BASE_SERVER)
            .pathSegment(PORTAL_SUBFOLDER, SUBFOLDER)
            .path("/")
            .build()
            .toUri()
    }

    val serverUrl: URI by lazy {
        if (profile == EnvironmentProfile.LOCAL) {
            return@lazy UriComponentsBuilder.newInstance()
                .scheme("http")
                .host("localhost")
                .port(SERVER_PORT)
                .build()
                .toUri()
        }

        return@lazy UriComponentsBuilder
            .fromUriString(BASE_SERVER)
            .path(SUBFOLDER)
            .build()
            .toUri()
    }

    val profile: EnvironmentProfile by lazy {
        EnvironmentProfile.getEnvironmentProfile(ACTIVE_PROFILE)
    }
}