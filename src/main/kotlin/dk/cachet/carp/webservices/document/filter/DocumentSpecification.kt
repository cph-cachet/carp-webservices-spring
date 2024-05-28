package dk.cachet.carp.webservices.document.filter

import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.document.domain.Document
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

/**
 * The Repository [DocumentSpecification].
 * The [DocumentSpecification] implements the specification for querying [Document]s.
 */
class DocumentSpecification {
    companion object {
        /**
         * The [belongsToStudyId] function validates whether the document is associated with the given [studyId].
         *
         * @param studyId The study [studyId] is associated with the document.
         * @return The validates the criteria request.
         */
        fun belongsToStudyId(studyId: String): Specification<Document> {
            return object : Specification<Document> {
                override fun toPredicate(
                    root: Root<Document>,
                    query: CriteriaQuery<*>,
                    criteriaBuilder: CriteriaBuilder,
                ): Predicate? {
                    return criteriaBuilder.equal(root.get<Collection>("collection").get<Int>("studyId"), studyId)
                }
            }
        }

        /**
         * The [belongsToUserAccountId] function validates whether the document is associated with the given [accountId].
         *
         * @param accountId The [accountId] is associated with the document.
         * @return The validates the criteria request.
         */
        fun belongsToUserAccountId(accountId: String): Specification<Document> {
            return object : Specification<Document> {
                override fun toPredicate(
                    root: Root<Document>,
                    query: CriteriaQuery<*>,
                    criteriaBuilder: CriteriaBuilder,
                ): Predicate? {
                    return criteriaBuilder.equal(root.get<String>("createdBy"), accountId)
                }
            }
        }
    }
}
