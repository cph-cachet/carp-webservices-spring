package dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.webservices.common.environment.EnvironmentProfile
import dk.cachet.carp.webservices.common.environment.EnvironmentUtil
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.oauth2.IssuerFacade
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain.*
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriBuilder

// https://www.keycloak.org/docs-api/21.1.1/rest-api/
@Service
@PropertySources(PropertySource(value = ["classpath:config/application-\${spring.profiles.active}.yml"]))
class KeycloakFacade(
    @Value("\${keycloak.issuer-url}") private val issuerUrl: String,
    @Value("\${keycloak.admin-url}") private val adminUrl: String,
    @Value("\${keycloak.client-id}") private val clientId: String,
    private val environmentUtil: EnvironmentUtil,
    webClientBuilder: WebClient.Builder // see WebClientConfig.kt
) : IssuerFacade
{
    companion object
    {
        private val LOGGER: Logger = LogManager.getLogger()
        private const val INVITATION_LIFESPAN = 24 * 60 * 60 * 30 // 30 days
    }

    private val adminClient: WebClient = webClientBuilder.baseUrl(issuerUrl).build()
    private val resourceClient: WebClient = webClientBuilder.baseUrl(adminUrl).build()

    override suspend fun createAccount(account: Account, accountType: AccountType): Account {
        LOGGER.debug("Creating account {}", account)

        val userRepresentation = UserRepresentation
            .createFromAccount(account, RequiredAction.getForAccountType(accountType))

        adminClient.post().uri("/users")
            .bodyValue(userRepresentation)
            .retrieve()
            .awaitBodilessEntity()

        val createdAccount = getAccount(account.getIdentity())
        checkNotNull(createdAccount) { "Account not created." }

        return createdAccount
    }

    override suspend fun addRole(account: Account, role: Role) {
        LOGGER.debug("Updating role of account: {}", account)

        // getting role representation with id
        val roleRepresentation: RoleRepresentation =
            adminClient.get().uri("/roles")
                .retrieve()
                .awaitBody<Set<RoleRepresentation>>()
                .filter { it.name != null }
                .first { it.name.equals(role.toString(), true) }

        // adding role to account
        adminClient.post().uri("/users/${account.id}/role-mappings/realm")
            .bodyValue(listOf(roleRepresentation))
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun getRoles(id: UUID): Set<Role> {
        LOGGER.debug("Getting roles of account with id: {}", id)

        val roleRepresentations = adminClient.get().uri("/users/${id}/role-mappings/realm")
            .retrieve()
            .awaitBody<Set<RoleRepresentation>>()

        return roleRepresentations.mapNotNull { it.name }.map { Role.fromString(it) }.toSet()
    }


    override suspend fun getAccount(uuid: UUID): Account? {
        LOGGER.debug("Getting account with id: {}", uuid)

        val userRepresentation = adminClient.get().uri("/users/${uuid}")
            .retrieve()
            .awaitBody<UserRepresentation>()

        val roles = getRoles(uuid)
        return userRepresentation.toAccount(roles)
    }

    override suspend fun getAccount(identity: AccountIdentity): Account? {
        val queryString = when (identity) {
            is EmailAccountIdentity -> "email=${identity.emailAddress}"
            is UsernameAccountIdentity -> "username=${identity.username}"
            else -> throw IllegalArgumentException("Unsupported account identity type: ${identity::class.simpleName}.")
        }.plus("&exact=true")

        LOGGER.debug("Getting account with identity: {}", identity)

        return queryAll(queryString).firstOrNull()
    }

    override suspend fun getAllByClaim(claim: Claim): List<Account> {
        val queryString = "q=${Claim.userAttributeName(claim::class)}:${claim.value}"

        LOGGER.debug("Getting all accounts with claim: {}", claim)

        return queryAll(queryString)
    }

    override suspend fun sendInvitation(account: Account, redirectUri: String?, accountType: AccountType) {
        LOGGER.debug("Sending invitation to account with id: ${account.id}")

        val requiredActions = RequiredAction.getForAccountType(accountType)

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
            .bodyValue(requiredActions)
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun updateAccount(account: Account, requiredActions: List<RequiredAction>): Account {
        LOGGER.debug("Updating account: {}", account)

        val userRepresentation = UserRepresentation
            .createFromAccount(account, requiredActions)

        adminClient.put().uri("/users/${account.id}")
            .bodyValue(userRepresentation)
            .retrieve()
            .awaitBodilessEntity()

        return account
    }

    override suspend fun deleteAccount(id: String) {
        throw UnsupportedOperationException("Account deletion is not supported by Carp Webservices.")
    }

    override suspend fun recoverAccount(
        account: Account,
        redirectUri: String?,
        expirationSeconds: Long?,
    ): String {
        LOGGER.debug("Generating recovery link for account: {}", account)

        val request = MagicLinkRequest(
            account.email,
            account.username,
            clientId,
            expirationSeconds,
            redirectUri
        )

        val magicLinkResponse = resourceClient.post().uri("/magic-link")
            .bodyValue(request)
            .retrieve()
            .awaitBody<MagicLinkResponse>()

        return magicLinkResponse.link
    }

    private suspend fun queryAll(query: String): List<Account> {
        LOGGER.debug("Querying all accounts with query: {}", query)

        val userRepresentations = adminClient.get().uri("/users?$query")
            .retrieve()
            .awaitBody<List<UserRepresentation>>()

        return userRepresentations.map { userRepresentation ->
            val roles = getRoles(UUID(userRepresentation.id!!))
            userRepresentation.toAccount(roles)
        }
    }
}