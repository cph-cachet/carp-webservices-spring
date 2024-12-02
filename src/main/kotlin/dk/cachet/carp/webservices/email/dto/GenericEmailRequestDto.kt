package dk.cachet.carp.webservices.email.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class GenericEmailRequestDto(
    @field:Email val recipient: String,
    @field:NotBlank val subject: String,
    // (the actual email content)
    @field:NotBlank val message: String,
    @field:Valid val cc: List<@Email String> = listOf(),
)
