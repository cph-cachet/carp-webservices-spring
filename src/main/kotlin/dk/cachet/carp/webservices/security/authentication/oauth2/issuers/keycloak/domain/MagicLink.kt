package dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class MagicLinkResponse(
    @JsonProperty("user_id")
    val accountId: String,
    val link: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MagicLinkRequest(
    var email: String? = null,
    var username: String? = null,
    var clientId: String? = null,
    var expirationSeconds: Long? = null,
    var redirectUri: String? = null,
    var forceCreate: Boolean? = null,
)