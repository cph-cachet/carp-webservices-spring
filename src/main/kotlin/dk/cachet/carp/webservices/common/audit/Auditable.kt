package dk.cachet.carp.webservices.common.audit

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

/**
 * The Class [Auditable].
 * A common superclass to provide common audit information to classes.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class Auditable
{
    /** The [createdBy] creator identity. */
    @CreatedBy
    var createdBy: String? = null

    /** The [createdAt] time of creation. */
    @CreatedDate
    var createdAt: Instant? = null

    /** The [updatedBy] the ID of the user the entity was updated by. */
    @LastModifiedBy
    var updatedBy: String? = null

    /** The [updatedAt] last time the entity was updated. */
    @LastModifiedDate
    var updatedAt: Instant? = null
}