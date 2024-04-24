package dk.cachet.carp.webservices.consent.service

import com.fasterxml.jackson.databind.JsonNode
import dk.cachet.carp.webservices.consent.domain.ConsentDocument

/**
 * The Interface [ConsentDocumentService].
 * The [ConsentDocumentService] creates an interface for handling consent document requests.
 */
interface ConsentDocumentService
{
    /** The [getAll] interface for retrieving consent documents by their [deploymentId]. */
    fun getAll(deploymentId: String): List<ConsentDocument>

    /** The [getAll] interface for retrieving consent documents for several deployment id's. */
    fun getAll(deploymentIds: List<String>): List<ConsentDocument>

    /** The [getOne] interface for retrieving one specific consent document. */
    fun getOne(id: Int): ConsentDocument

    /** The [delete] interface for deleting a consent document. */
    fun delete(id: Int)

    /** The [create] interface to create a new consent document. */
    fun create(deploymentId: String, data: JsonNode?): ConsentDocument
}