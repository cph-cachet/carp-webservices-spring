package dk.cachet.carp.webservices.collection.dto

import jakarta.validation.constraints.NotBlank

/**
 * The Data Class [CollectionCreateRequestDto].
 * The [CollectionCreateRequestDto] represents a user collection request, with the given [name].
 */
data class CollectionCreateRequestDto(
    /** The [name] of the collection. */
    @field:NotBlank
    val name: String,
    val deploymentId: String? = null,
)
