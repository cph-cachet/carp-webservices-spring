package dk.cachet.carp.webservices.email.controller

import dk.cachet.carp.webservices.email.controller.EmailController.Companion.EMAIL_BASE
import dk.cachet.carp.webservices.email.dto.GenericEmailRequestDto
import dk.cachet.carp.webservices.email.service.EmailService
import jakarta.validation.Valid
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(EMAIL_BASE)
class EmailController(
    private val emailService: EmailService,
) {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()

        const val EMAIL_BASE = "/api/email"
        const val SEND_GENERIC = "/send-generic"
    }

    @PostMapping(SEND_GENERIC)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('RESEARCH_ASSISTANT') or hasRole('RESEARCHER')")
    fun sendGeneric(
        @Valid @RequestBody request: GenericEmailRequestDto,
    ) {
        LOGGER.info("Start POST: $EMAIL_BASE$SEND_GENERIC")

        emailService.sendGenericEmail(request)
    }
}
