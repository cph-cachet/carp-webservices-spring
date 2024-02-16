package dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.webservices.common.configuration.cache.CachingConfig.Companion.ADMIN_BEARER_TOKEN
import dk.cachet.carp.webservices.common.configuration.cache.CachingConfig.Companion.TOKEN_CACHE
import dk.cachet.carp.webservices.common.environment.EnvironmentProfile
import dk.cachet.carp.webservices.common.environment.EnvironmentUtil
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.oauth2.IssuerFacade
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain.RequiredActions
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain.RoleRepresentation
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain.TokenResponse
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain.UserRepresentation
import dk.cachet.carp.webservices.security.authorization.Role
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriBuilder

// https://www.keycloak.org/docs-api/21.1.1/rest-api/
@Service
@PropertySources(PropertySource(value = ["classpath:config/application-\${spring.profiles.active}.yml"]))
class KeycloakFacade(
    @Value("\${keycloak.auth-server-url}") private val authServerUrl: String,
    @Value("\${keycloak.realm}") private val realm: String,
    @Value("\${keycloak.admin.client-id}") private val clientId: String,
    @Value("\${keycloak.admin.client-secret}") private val clientSecret: String,
    private val objectMapper: ObjectMapper,
    private val environmentUtil: EnvironmentUtil
) : IssuerFacade {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
        private const val INVITATION_LIFESPAN = 24 * 60 * 60 * 30 // 30 days
    }


    private val serializationStrategies: ExchangeStrategies =
        ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs()
                    .jackson2JsonEncoder(
                        Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON)
                    )
                configurer.defaultCodecs()
                    .jackson2JsonDecoder(
                        Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON)
                    )
            }
            .build()

    private val authClient: WebClient = WebClient.builder()
        .baseUrl("$authServerUrl/realms/$realm")
        .exchangeStrategies(serializationStrategies)
        .defaultHeaders {
            it.contentType = MediaType.parseMediaType(APPLICATION_FORM_URLENCODED_VALUE)
            it.accept = listOf(MediaType.APPLICATION_JSON)
            it.setBasicAuth(clientId, clientSecret)
        }
        .build()

    private val adminClient: WebClient = WebClient.builder()
        .baseUrl("$authServerUrl/admin/realms/$realm")
        .exchangeStrategies(serializationStrategies)
        .defaultHeaders {
            it.contentType = MediaType.APPLICATION_JSON
            it.accept = listOf(MediaType.APPLICATION_JSON)
        }
        .build()

    override suspend fun createAccount(account: Account) {
        val token = authenticate().accessToken

        LOGGER.info("Creating account with email: ${account.email}")

        val userRepresentation = UserRepresentation
            .createFromAccount(account)
            .setDefaultActions()

        adminClient.post().uri("/users")
            .headers {
                it.setBearerAuth(token!!)
            }
            .bodyValue(userRepresentation)
            .retrieve()
            .awaitBodilessEntity()
    }

    // TODO: change the baked in strings to using resources
    override suspend fun addRole(account: Account, role: Role) {
        val token = authenticate().accessToken

        LOGGER.info("Updating role of account with id: $account")

        // getting role representation with id
        val roleRepresentation: RoleRepresentation =
            adminClient.get().uri("/roles")
                .headers {
                    it.setBearerAuth(token!!)
                }
                .retrieve()
                .awaitBody<Set<RoleRepresentation>>()
                .filter { it.name != null }
                .first { it.name.equals(role.toString(), true) }

        // adding role to account
        adminClient.post().uri("/users/${account.id}/role-mappings/realm")
            .headers {
                it.setBearerAuth(token!!)
            }
            .bodyValue(listOf(roleRepresentation))
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun getRoles(id: UUID): Set<Role> {
        val token = authenticate().accessToken

        LOGGER.info("Getting roles of account with id: $id")

        val roleRepresentations = adminClient.get().uri("/users/${id}/role-mappings/realm")
            .headers {
                it.setBearerAuth(token!!)
            }
            .retrieve()
            .awaitBody<Set<RoleRepresentation>>()

        return roleRepresentations.mapNotNull { it.name }.map { Role.fromString(it) }.toSet()
    }


    override suspend fun getAccount(uuid: UUID): Account? {
        val token = authenticate().accessToken

        LOGGER.info("Getting account with id: $uuid")

        val userRepresentation = adminClient.get().uri("/users/${uuid}")
            .headers {
                it.setBearerAuth(token!!)
            }
            .retrieve()
            .awaitBody<UserRepresentation>()

        val roles = getRoles(uuid)
        return userRepresentation.toAccount(roles)
    }

    override suspend fun getAccount(identity: AccountIdentity): Account? {
        val auth = authenticate()
        val token = auth.accessToken
        val queryString = when (identity) {
            is EmailAccountIdentity -> "email=${identity.emailAddress}"
            else -> throw IllegalArgumentException("Unsupported account identity type: ${identity::class.simpleName}.")
        }

        LOGGER.info("Getting account with identity: $identity")

        val userRepresentation = adminClient.get().uri("/users?$queryString&exact=true")
            .headers {
                it.setBearerAuth(token!!)
            }
            .retrieve()
            .awaitBody<List<UserRepresentation>>()
            .firstOrNull()

        if (userRepresentation == null) {
            LOGGER.info("No account found with identity: $identity")
            return null
        }

        val roles = getRoles(UUID(userRepresentation.id!!))
        return userRepresentation.toAccount(roles)
    }

    override suspend fun sendInvitation(account: Account, redirectUri: String?, isNewAccount: Boolean) {
        val token = authenticate().accessToken

        LOGGER.info("Sending invitation to account with id: ${account.id}")

        val requiredActions =
            if (isNewAccount)
                RequiredActions.getActionsForNewAccount()
            else
                RequiredActions.getActionsForExistingAccount()

        adminClient.put().uri("/users/${account.id}/execute-actions-email")
        { uriBuilder: UriBuilder ->
            var builder = uriBuilder
                .queryParam("client_id", clientId)
                .queryParam("lifespan", INVITATION_LIFESPAN)

            if (environmentUtil.profile != EnvironmentProfile.LOCAL) {
                builder = builder.queryParam("redirect_uri", redirectUri ?: environmentUtil.portalUrl)
            }

            builder.build()
        }
            .headers {
                it.setBearerAuth(token!!)
            }
            .bodyValue(requiredActions)
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun updateAccount(account: Account) {
        throw UnsupportedOperationException("Updating accounts is not supported by Carp Webservices.")
    }

    override suspend fun deleteAccount(id: String) {
        throw UnsupportedOperationException("Account deletion is not supported by Carp Webservices.")
    }

    suspend fun generateMagicLink(studyId: UUID): String {
        val token = authenticate().accessToken
        print("check")

        LOGGER.info("Generating magic links with $studyId")

        val requestBody = """
        {
            "email": "asdfa-asdf-as-df-asd-fa-sd-fa-sdf-asdf@definetelyamail.com",
            "expiration_seconds": 2000000,
            "client_id": "caws-client",
            "redirect_uri": "https://carp.computerome.dk/icat/dev/",
            "force_create": true,
            "send_email": false
        }
    """.trimIndent()

        val generateUser = authClient.post()
            .uri("/magic-link")
            .headers {
                it.setBearerAuth(token!!)
                it.contentType = MediaType.APPLICATION_JSON // Set content type for JSON
            }
            .body(BodyInserters.fromValue(requestBody))
            .retrieve()
            .awaitBody<String>()

        return generateUser
    }

    @Cacheable(value = [TOKEN_CACHE], key = ADMIN_BEARER_TOKEN)
    suspend fun authenticate(): TokenResponse =
        authClient.post().uri("/protocol/openid-connect/token")
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .awaitBody()
}