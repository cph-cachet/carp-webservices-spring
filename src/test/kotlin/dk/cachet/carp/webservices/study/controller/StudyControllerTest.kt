package dk.cachet.carp.webservices.study.controller

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.study.service.RecruitmentService
import dk.cachet.carp.webservices.study.service.StudyService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import kotlin.test.BeforeTest
import kotlin.test.Test

class StudyControllerTest {
    private val authenticationService: AuthenticationService = mockk()
    private val accountService: AccountService = mockk()
    private val studyService: StudyService = mockk()
    private val recruitmentService: RecruitmentService = mockk()
    private lateinit var mockMvc: MockMvc

    @BeforeTest
    fun setup() {
        mockMvc =
            MockMvcBuilders.standaloneSetup(
                StudyController(
                    authenticationService, accountService, studyService, recruitmentService,
                ),
            ).build()
    }

    @Nested
    inner class GetParticipantAccounts {
        @Test
        fun `should return response as DTO if specified in query`() {
            runTest {
                val mockStudyId = UUID.randomUUID().stringRepresentation
                val url = "/api/studies/$mockStudyId/participants/accounts?response_as_dto=true"

                coEvery { recruitmentService.countParticipants(any(), any()) } returns 0
                coEvery { recruitmentService.getParticipants(any(), any(), any(), any(), any()) }

                mockMvc.get(url).andExpect { status { isOk() } }
                coVerify(exactly = 1) { recruitmentService.getParticipants(any(), any(), any(), any(), any()) }
                coVerify(exactly = 1) { recruitmentService.countParticipants(any(), any()) }
            }
        }

        @Test
        fun `should return a list if dto not required`() {
            runTest {
                val mockStudyId = UUID.randomUUID().stringRepresentation
                val url = "/api/studies/$mockStudyId/participants/accounts"

                coEvery { recruitmentService.countParticipants(any(), any()) } returns 0
                coEvery { recruitmentService.getParticipants(any(), any(), any(), any(), any()) }

                mockMvc.get(url).andExpect { status { isOk() } }
                coVerify(exactly = 1) { recruitmentService.getParticipants(any(), any(), any(), any(), any()) }
                coVerify(exactly = 0) { recruitmentService.countParticipants(any(), any()) }
            }
        }
    }
}
