package dk.cachet.carp.webservices.account.domain

import dk.cachet.carp.common.application.UUID
import kotlinx.datetime.Instant

data class MagicLink (

    val magicLink: String,

    val accountId: UUID,

    val studyDeploymentId: String,

    val expiryDate: Instant,

    )