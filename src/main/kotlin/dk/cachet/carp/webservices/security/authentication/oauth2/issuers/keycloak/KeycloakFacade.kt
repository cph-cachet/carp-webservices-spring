package dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.webservices.common.environment.EnvironmentProfile
import dk.cachet.carp.webservices.common.environment.EnvironmentUtil
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.oauth2.IssuerFacade
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain.*
import dk.cachet.carp.webservices.security.authorization.Role
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Service
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

    private val adminClient: WebClient = buildWebClient("$authServerUrl/admin/realms/$realm")

    private val resourceClient: WebClient = buildWebClient("$authServerUrl/realms/$realm")

    private val authClient: WebClient = buildWebClient("$authServerUrl/realms/$realm")
        .mutate().defaultHeaders {
            it.contentType = MediaType.parseMediaType(APPLICATION_FORM_URLENCODED_VALUE)
            it.accept = listOf(MediaType.APPLICATION_JSON)
            it.setBasicAuth(clientId, clientSecret)
        }.build()


    override suspend fun createAccount(account: Account, accountType: AccountType): Account {
        val token = authenticate().accessToken

        LOGGER.debug("Creating account {}", account)

        val userRepresentation = UserRepresentation
            .createFromAccount(account, accountType)

        adminClient.post().uri("/users")
            .headers {
                it.setBearerAuth(token!!)
            }
            .bodyValue(userRepresentation)
            .retrieve()
            .awaitBodilessEntity()

        return account
    }

    override suspend fun addRole(account: Account, role: Role) {
        val token = authenticate().accessToken

        LOGGER.debug("Updating role of account: {}", account)

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

        LOGGER.debug("Getting roles of account with id: {}", id)

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

        LOGGER.debug("Getting account with id: {}", uuid)

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
            is UsernameAccountIdentity -> "username=${identity.username}"
            else -> throw IllegalArgumentException("Unsupported account identity type: ${identity::class.simpleName}.")
        }

        LOGGER.debug("Getting account with identity: {}", identity)

        val userRepresentation = adminClient.get().uri("/users?$queryString&exact=true")
            .headers {
                it.setBearerAuth(token!!)
            }
            .retrieve()
            .awaitBody<List<UserRepresentation>>()
            .firstOrNull()

        if (userRepresentation == null) {
            LOGGER.debug("No account found with identity: {}", identity)
            return null
        }

        val roles = getRoles(UUID(userRepresentation.id!!))
        return userRepresentation.toAccount(roles)
    }

    override suspend fun sendInvitation(account: Account, redirectUri: String?, accountType: AccountType) {
        val token = authenticate().accessToken

        LOGGER.debug("Sending invitation to account with id: ${account.id}")

        val requiredActions = RequiredActions.getForAccountType(accountType)

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

    override suspend fun recoverAccount(
        account: Account,
        redirectUri: String?,
        expirationSeconds: Long?,
    ): String {
        val token = authenticate().accessToken

        LOGGER.debug("Generating recovery link for account: {}", account)

        val request = MagicLinkRequest(
            account.email,
            account.username,
            clientId,
            expirationSeconds,
            redirectUri
        )

        val magicLinkResponse = resourceClient.post().uri("/magic-link")
            .headers {
                it.setBearerAuth(token!!)
            }
            .bodyValue(request)
            .retrieve()
            .awaitBody<MagicLinkResponse>()

        return magicLinkResponse.link
    }

    suspend fun authenticate(): TokenResponse =
        authClient.post().uri("/protocol/openid-connect/token")
            .bodyValue("grant_type=client_credentials")
            .retrieve()
            .awaitBody()

    private fun buildWebClient(baseUrl: String): WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .exchangeStrategies(serializationStrategies)
        .defaultHeaders {
            it.contentType = MediaType.APPLICATION_JSON
            it.accept = listOf(MediaType.APPLICATION_JSON)
        }
        .build()
}