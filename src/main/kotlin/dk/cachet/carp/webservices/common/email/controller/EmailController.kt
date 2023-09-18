package dk.cachet.carp.webservices.common.email.controller

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.email.controller.EmailController.Companion.EMAIL_BASE
import dk.cachet.carp.webservices.common.email.domain.NotificationRequest
import dk.cachet.carp.webservices.common.email.service.EmailInvitationService
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(EMAIL_BASE)
class EmailController(
    private val emailInvitationService: EmailInvitationService,
    private val accountService: AccountService
) {

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        const val EMAIL_BASE = "/api/email"
        const val SEND = "/send"
    }

    @PostMapping(SEND)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@emailAuthorizationService.canAccountSendMail(#request)")
    fun send(@Valid @RequestBody request: NotificationRequest) = runBlocking {
        LOGGER.info("Start POST: $EMAIL_BASE$SEND")

        val emailAddress = accountService.findByUUID(UUID.parse(request.recipientAccountId))?.email
        requireNotNull(emailAddress) { "No email address found for account with ID ${request.recipientAccountId}." }

        emailInvitationService.sendEmail(emailAddress, request.subject, request.message)
    }
}