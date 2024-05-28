package dk.cachet.carp.webservices.collection.domain

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dk.cachet.carp.webservices.common.audit.Auditable
import dk.cachet.carp.webservices.document.domain.Document
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.DynamicUpdate
import java.io.Serializable

/**
 * The Data Class [Collection].
 * The [Collection] represents the collection with the [id], [name], [studyId], [documentId], and [documents] of the study.
 */
@DynamicUpdate
@Entity(name = "collections")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Collection(
    /** The collection [id].*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    /** Name of the collection.*/
    @field:NotNull
    var name: String = "",
    /** The ID of the study the collection belongs to.*/
    @field:NotNull
    var studyId: String = "",
    /** The deploymentId of the study the collection belongs to.*/
    var studyDeploymentId: String? = "",
    /** The [documentId] of the collection.*/
    val documentId: Int? = null,
    /** The [Document] instances the collection contains.*/
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "collectionId")
    @OrderBy("createdAt desc")
    var documents: List<Document>? = null,
) : Auditable(), Serializable
