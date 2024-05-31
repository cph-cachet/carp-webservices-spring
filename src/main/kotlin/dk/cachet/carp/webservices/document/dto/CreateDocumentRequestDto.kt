package dk.cachet.carp.webservices.document.dto

import com.fasterxml.jackson.databind.JsonNode
import dk.cachet.carp.webservices.collection.domain.Collection
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateDocumentRequestDto(
    /** The [name] of the document. */
    @field:NotBlank
    var name: String = "",
    /** The [collectionId] where the document is associated with. */
    @field:NotNull
    var collectionId: Int? = null,
    /** The [collections] where the document is associated with. */
    var collections: List<Collection>? = null,
    /** The [data] containing the document object request. */
    var data: JsonNode? = null,
)
