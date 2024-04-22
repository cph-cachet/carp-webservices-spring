package dk.cachet.carp.webservices.summary.factory.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.summary.domain.Summary
import dk.cachet.carp.webservices.summary.domain.SummaryStatus
import dk.cachet.carp.webservices.summary.factory.ISummaryFactory
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class SummaryFactory(
    private val authenticationService: AuthenticationService
): ISummaryFactory {

    override fun create(studyId: UUID, deploymentIds: List<String>?): Summary {
        val createdAt = Instant.now()
        val createdBy = authenticationService.getId().stringRepresentation
        val hash = createHash(studyId, deploymentIds, createdAt, createdBy)

        // TODO the name is temporarily a studyId until we fix the callBlocking hell so I can actually get Core objects
        val fileName = "${studyId.stringRepresentation.substring(0,8)}_${createdAt}.zip"

        return Summary(hash, fileName, SummaryStatus.IN_PROGRESS, studyId.stringRepresentation)
    }

    /**
     * The function [createHash] takes properties regarding the circumstances of the summary creation to create a unique
     * hash of it. The hash is used to clearly identify the summary. We don't want initiate a new summary creation with
     * each request, so we hash the hour of creation into it.
     *
     * @param createdBy The name of the user who created the hash. To avoid overcomplicating things we put this into
     * the hash as well.
     * TODO remove if researcher to study mapping is not that tedious anymore.
     */
    private fun createHash(studyId: UUID, deploymentIds: List<String>?, createdAt: Instant, createdBy: String): String {
        val createdAtHour = createdAt.truncatedTo(ChronoUnit.HOURS)
        val hash = "$studyId${deploymentIds?.joinToString()}${createdAtHour}$createdBy".hashCode()
        val hashBytes = ByteBuffer.allocate(4).putInt(hash).array()

        return java.util.UUID.nameUUIDFromBytes(hashBytes).toString()
    }
}