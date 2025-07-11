package dk.cachet.carp.webservices.account.controller

import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.webservices.account.controller.AccountController.Companion.ACCOUNT_BASE
import dk.cachet.carp.webservices.account.domain.AccountRequest
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.constants.PathVariableName
import dk.cachet.carp.webservices.security.authentication.domain.Account
import jakarta.validation.Valid
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(ACCOUNT_BASE)
class AccountController(private val accountService: AccountService) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        const val ACCOUNT_BASE = "/api/accounts"
        const val INVITE = "/invite"
        const val ROLE = "/role"
        const val ACCOUNT = "/{${PathVariableName.ACCOUNT_ID}}"
        const val REDIRECT_URIS = "/redirect-uris"
    }

    @PostMapping(INVITE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RESEARCHER_ASSISTANT') and hasRole(#request.role)")
    suspend fun invite(
        @Valid @RequestBody request: AccountRequest,
    ) {
        LOGGER.info("Start POST: $ACCOUNT_BASE$INVITE")
        accountService.invite(AccountIdentity.fromEmailAddress(request.emailAddress), request.role)
    }

    @PostMapping(ROLE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole(#request.role)")
    suspend fun role(
        @Valid @RequestBody request: AccountRequest,
    ): ResponseEntity<Any> {
        LOGGER.info("Start POST: $ACCOUNT_BASE$ROLE")

        if (!accountService.hasRoleByEmail(EmailAddress(request.emailAddress), request.role)) {
            return ResponseEntity(HttpStatus.NOT_FOUND)
        }

        return ResponseEntity(HttpStatus.OK)
    }

    @GetMapping(ACCOUNT)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('RESEARCHER')") // TODO: currently not used in portal.
    suspend fun info(
        @PathVariable(PathVariableName.ACCOUNT_ID) accountId: UUID,
    ): Account? {
        LOGGER.info("Start GET: $ACCOUNT_BASE$ACCOUNT")
        return accountService.findByUUID(accountId)
    }

    @GetMapping(REDIRECT_URIS)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('RESEARCHER_ASSISTANT')")
    suspend fun redirectUris(): List<String> {
        LOGGER.info("Start GET: $ACCOUNT_BASE$REDIRECT_URIS")
        return accountService.getRedirectUris()
    }
}
