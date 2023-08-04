package dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenResponse (

    @JsonProperty("access_token")
    val accessToken: String?,

    @JsonProperty("expires_in")
    val expiresIn: Int?,

    @JsonProperty("refresh_expires_in")
    val refreshExpiresIn: Int?,

    @JsonProperty("token_type")
    val tokenType: String?,

    @JsonProperty("not-before-policy")
    val notBeforePolicy: Int?,

    @JsonProperty("refresh_token")
    val refreshToken: String?,

    val scope: String?
)