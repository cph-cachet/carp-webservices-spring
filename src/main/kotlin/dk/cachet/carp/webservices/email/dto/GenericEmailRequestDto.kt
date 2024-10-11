package dk.cachet.carp.webservices.email.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank


//todo add documentation bs

data class GenericEmailRequestDto(
    @field:Email val recipient: String,
    @field:Email val sender: String,
    @field:NotBlank val subject: String,
    val body: String,
)