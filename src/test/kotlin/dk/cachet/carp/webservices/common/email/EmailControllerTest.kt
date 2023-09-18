package dk.cachet.carp.webservices.common.email

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.account.controller.AccountController
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.email.controller.EmailController
import dk.cachet.carp.webservices.common.email.domain.NotificationRequest
import dk.cachet.carp.webservices.common.email.service.EmailInvitationService
import dk.cachet.carp.webservices.common.exception.advices.ExceptionAdvices
import dk.cachet.carp.webservices.common.notification.service.INotificationService
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@OptIn(ExperimentalCoroutinesApi::class)
class EmailControllerTest {

    private val accountService: AccountService = mockk()
    private val emailInvitationService: EmailInvitationService = mockk()
    private val objectMapper: ObjectMapper = ObjectMapper()

    private lateinit var mockMvc: MockMvc

    @BeforeTest
    fun setup() {
        val notificationService = mockk<INotificationService> {
            every { sendExceptionNotificationToSlack(any()) } just runs
        }

        mockMvc = MockMvcBuilders
            .standaloneSetup(EmailController(emailInvitationService, accountService))
            .setControllerAdvice(ExceptionAdvices(notificationService))
            .build()
    }

    @Nested
    inner class Send {

        private val endpoint = "/api/email/send/"

        @Test
        fun `should return 400 if email address is not present`() = runTest {
            val accountIdWithNoEmail = "85cd269a-c191-40ef-8ae8-6337ccd78fcb"
            val notificationRequest = NotificationRequest(accountIdWithNoEmail, "subject", "body", "deploymentId")

            coEvery { accountService.findByUUID(any()) } returns null

            mockMvc.perform(
                post(endpoint)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 201 if email is sent`() = runTest {
            val accountId = "85cd269a-c191-40ef-8ae8-6337ccd78fcb"
            val notificationRequest = NotificationRequest(accountId, "subject", "body", "deploymentId")

            coEvery { accountService.findByUUID(any()) } returns mockk {
                every { email } returns "email"
            }
            coEvery { emailInvitationService.sendEmail(any(), any(), any()) } just runs

            mockMvc.perform(
                post(endpoint)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isCreated)
        }
    }
}
