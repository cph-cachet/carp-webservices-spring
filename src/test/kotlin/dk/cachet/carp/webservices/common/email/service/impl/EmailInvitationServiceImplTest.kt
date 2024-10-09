package dk.cachet.carp.webservices.common.email.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.common.email.listener.EmailSendingJob
import dk.cachet.carp.webservices.common.email.service.impl.javamail.EmailServiceImpl
import dk.cachet.carp.webservices.common.email.util.EmailTemplateUtil
import dk.cachet.carp.webservices.common.email.util.EmailValidatorUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EmailInvitationServiceImplTest {
    private val emailValidator: EmailValidatorUtil = mockk()
    private val emailTemplate: EmailTemplateUtil = mockk()
    private val emailService: EmailServiceImpl = mockk()
    private val emailSendingJob: EmailSendingJob = mockk()

    @Nested
    inner class inviteToStudy {
        @Test
        fun `should not throw if email address is invalid`() =
            runTest {
                val sut = EmailInvitationServiceImpl(emailValidator, emailTemplate, emailService, emailSendingJob)
                coEvery { emailValidator.isValid("invalid-email") } returns false

                sut.inviteToStudy("invalid-email", UUID.randomUUID(), mockk(), mockk())

                coVerify(exactly = 0) { emailSendingJob.send(any()) }
            }
    }
}
