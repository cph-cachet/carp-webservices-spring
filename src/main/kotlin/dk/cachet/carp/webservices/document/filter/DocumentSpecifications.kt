package dk.cachet.carp.webservices.document.filter

import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.document.domain.Document
import org.springframework.data.jpa.domain.Specification

object DocumentSpecifications {
    /**
     * The [belongsToStudyId] function validates whether the document is associated with the given [studyId].
     *
     * @param studyId The study [studyId] is associated with the document.
     * @return The validates the criteria request.
     */
    fun belongsToStudyId(studyId: String): Specification<Document> {
        return Specification<Document> { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<Collection>("collection").get<Int>("studyId"),
                studyId,
            )
        }
    }

    /**
     * The [belongsToUserAccountId] function validates whether the document is associated with the given [accountId].
     *
     * @param accountId The [accountId] is associated with the document.
     * @return The validates the criteria request.
     */
    fun belongsToUserAccountId(accountId: String): Specification<Document> {
        return Specification<Document> { root, _, criteriaBuilder ->
            criteriaBuilder.equal(
                root.get<String>("createdBy"),
                accountId,
            )
        }
    }
}
