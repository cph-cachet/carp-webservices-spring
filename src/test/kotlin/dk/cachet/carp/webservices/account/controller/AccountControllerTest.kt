package dk.cachet.carp.webservices.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.account.domain.AccountRequest
import dk.cachet.carp.webservices.account.service.AccountService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import kotlin.test.BeforeTest
import kotlin.test.Test
import dk.cachet.carp.webservices.security.authorization.Role as AccountRole

@OptIn(ExperimentalCoroutinesApi::class)
class AccountControllerTest {

    private val accountService: AccountService = mockk()
    private val objectMapper: ObjectMapper = ObjectMapper()

    private lateinit var mockMvc: MockMvc

    @BeforeTest
    fun setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(AccountController(accountService)).build()
    }

    @Nested
    inner class Invite {

        private val endpoint = "/api/accounts/invite/"

        @Test
        fun `should return 400 if email is invalid`() = runTest {
            val accountRequest = AccountRequest("invalid", AccountRole.PARTICIPANT)

            mockMvc.perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 400 if the request body is missing`() = runTest {
            mockMvc.perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should relay task to account service`() = runTest {
            coEvery { accountService.invite(any(), any()) } returns mockk()
            val accountRequest = AccountRequest("address@domain.org", AccountRole.PARTICIPANT)

            mockMvc.perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated)
        }
    }


    @Nested
    inner class Role {
        private val endpoint = "/api/accounts/role/"

        @Test
        fun `should return 400 if email is invalid`() = runTest {
            val accountRequest = AccountRequest("invalid", AccountRole.PARTICIPANT)

            mockMvc.perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should return 400 if the request body is missing`() = runTest {
            mockMvc.perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest)
        }

        @Test
        fun `should relay task to account service`() = runTest {
            coEvery { accountService.hasRoleByEmail(any(), any()) } returns mockk()
            val accountRequest = AccountRequest("address@domain.org", AccountRole.PARTICIPANT)

            mockMvc.perform(
                post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isOk)
        }
    }

    @Nested
    inner class Info {
        private val validUUIDEndpoint = "/api/accounts/${UUID.randomUUID().stringRepresentation}"

        @Test
        fun `should relay task to account service`() = runTest {
            coEvery { accountService.findByUUID(any()) } returns mockk()

            mockMvc.perform(
                get(validUUIDEndpoint)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
        }
    }
}