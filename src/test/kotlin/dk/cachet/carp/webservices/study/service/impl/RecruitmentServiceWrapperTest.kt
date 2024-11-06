package dk.cachet.carp.webservices.study.service.impl

import org.junit.jupiter.api.Assertions.*
import com.fasterxml.jackson.databind.ObjectMapper
import cz.jirutka.rsql.parser.RSQLParser
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.collection.repository.CollectionRepository
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService

import dk.cachet.carp.webservices.collection.domain.Collection
import dk.cachet.carp.webservices.collection.dto.CollectionUpdateRequestDto
import dk.cachet.carp.webservices.common.exception.responses.ResourceNotFoundException
import io.mockk.*
import org.junit.jupiter.api.Nested
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.EmailAccountIdentity
import dk.cachet.carp.webservices.collection.dto.CollectionCreateRequestDto
import dk.cachet.carp.webservices.common.exception.responses.AlreadyExistsException
import dk.cachet.carp.webservices.common.query.QueryUtil
import dk.cachet.carp.webservices.common.query.QueryVisitor
import dk.cachet.carp.webservices.common.services.CoreServiceContainer
import dk.cachet.carp.webservices.datastream.service.DataStreamService
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authorization.Role
import org.junit.jupiter.api.AfterEach
import org.springframework.data.jpa.domain.Specification
import java.util.*
import kotlin.test.*

class RecruitmentServiceWrapperTest {
    private val accountService: AccountService = mockk()
    private val dataStreamService: DataStreamService = mockk()
    val services: CoreServiceContainer = mockk()

    @Nested
    inner class InviteResearcher {
        @Test
        fun `researcher is invited if account does not exist`() {
            val mockStudyId = UUID.randomUUID()
            val mockEmail = "lol@gmail.com"

            val sut = RecruitmentServiceWrapper(accountService, dataStreamService, services)

            coEvery { accountService.findByAccountIdentity(ofType<EmailAccountIdentity>()) } returns null
            coEvery { accountService.invite(ofType<EmailAccountIdentity>(), Role.RESEARCHER) } returns AccountService.

        }





    }
}