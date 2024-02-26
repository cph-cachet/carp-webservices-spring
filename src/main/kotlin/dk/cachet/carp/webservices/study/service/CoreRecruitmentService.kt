package dk.cachet.carp.webservices.study.service

import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.google.gson.JsonParser
import dk.cachet.carp.common.application.EmailAddress
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHost
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import dk.cachet.carp.webservices.data.service.IDataStreamService
import dk.cachet.carp.webservices.deployment.service.CoreDeploymentService
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.domain.AccountFactory
import dk.cachet.carp.webservices.security.authentication.oauth2.issuers.keycloak.KeycloakFacade
import dk.cachet.carp.webservices.study.domain.MagicLink
import dk.cachet.carp.webservices.study.domain.ParticipantAccount
import dk.cachet.carp.webservices.study.domain.ParticipantGroupInfo
import dk.cachet.carp.webservices.study.domain.ParticipantGroupsStatus
import dk.cachet.carp.webservices.study.repository.CoreParticipantRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

@Component
class CoreRecruitmentService
(
    participantRepository: CoreParticipantRepository,
    coreEventBus: CoreEventBus,
    private val coreDeploymentService: CoreDeploymentService,
    private val dataStreamService: IDataStreamService,
    private val accountService: AccountService,
    private val accountFactory: AccountFactory,
    private val keycloakFacade: KeycloakFacade
)
{
    final val instance: RecruitmentService = RecruitmentServiceHost(
            participantRepository,
            coreDeploymentService.instance,
            coreEventBus.createApplicationServiceAdapter(RecruitmentService::class)
    )

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
        private val threadPoolExecutor = Executors.newCachedThreadPool()
    }

    suspend fun getParticipantAccounts(studyId: UUID) : List<Account>
    {
        val participants = instance.getParticipants(studyId)
        val accounts = arrayListOf<Account>()
        for (participant in participants) {
            val account = accountService.findByAccountIdentity(participant.accountIdentity)
            if (account != null) {
                accounts.add(account)
            } else {
                accounts.add(accountFactory.fromAccountIdentity(participant.accountIdentity))
            }
        }
        return accounts
    }

    suspend fun getParticipantGroupStatus(studyId: UUID): ParticipantGroupsStatus {
        val participantGroupStatusList = instance.getParticipantGroupStatusList(studyId)
        val participantGroupInfoList = arrayListOf<ParticipantGroupInfo>()

        for (participantGroupStatus in participantGroupStatusList) {

            val participantAccounts = arrayListOf<ParticipantAccount>()
            val deploymentStatus = coreDeploymentService.instance.getStudyDeploymentStatus(participantGroupStatus.id)
            participantGroupStatus.participants.map {
                val participantAccount = ParticipantAccount.fromParticipant(it)
                val account = accountService.findByAccountIdentity(it.accountIdentity)
                if (account != null) {
                    val lastDataUpload = dataStreamService.getLatestUpdatedAt(participantGroupStatus.id)
                    participantAccount.lateInitFrom(account)
                    participantAccount.dateOfLastDataUpload = lastDataUpload
                }
                participantAccounts.add(participantAccount)
            }

            val info = ParticipantGroupInfo(participantGroupStatus.id, deploymentStatus, participantAccounts)
            participantGroupInfoList.add(info)
        }
        return ParticipantGroupsStatus(participantGroupInfoList, participantGroupStatusList)
    }

    suspend fun sendMagicLinks(studyId: UUID, numberOfAccounts: Number, expiryDate: Instant?) {

        // CSV file path with study ID, /w time of generation
        val nowTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val nowTimeFormatted = nowTime.format(formatter)

        val csvFile = "\"magic_links.${studyId.toString().take(8)}.$nowTimeFormatted.csv\""

        /*TODO dummy data to delete after it generates user with right attributes*/
        // Example data, replace this with your actual data
        val csvDataList = listOf(
            MagicLink("https://example.com/magiclink1",
                UUID("8076f1cd-ce2a-4f98-bbdc-619de87e9f07")
                ,"94b89928-e8c2-4ece-96d6-7a03bd2e5f71",
                    Clock.System.now()),
            MagicLink("https://example.com/magiclink2",
                UUID("8076f1cd-ce2a-4f98-bbdc-619de87e9f07")
                ,"94b89928-e8c2-4ece-96d6-7a03bd2e5f71",
                    Clock.System.now())
        )


        /*TODO move this part at the end of cycle (!)*/

        // Writing CSV file
        CsvWriter().open(csvFile) {
            writeRow(listOf("Magic Link", "Account ID", "Study Deployment ID", "Expiry Date"))

            csvDataList.forEach { data ->
                writeRow(listOf(
                    data.magicLink,
                    data.accountId,
                    data.studyDeploymentId,
                    data.expiryDate
                ))
            }}

        // Execute the code in a separate thread
        threadPoolExecutor.execute {
            LOGGER.info("Generating users...")

            try {

                for (i in 0 until numberOfAccounts.toInt()-1) {
                    // Use runBlocking to call the suspending function generateMagicLink
                    val generatedLinkUser = runBlocking {
                        keycloakFacade.generateMagicLink(studyId)
                    }
                    // Parse the JSON string into a JSONObject
                    val jsonObject = JsonParser.parseString(generatedLinkUser).asJsonObject

                    val link = jsonObject.get("link").asString
                    val userId = UUID(jsonObject.get("user_id").asString)

                    val dummyEmail = EmailAddress("${userId}@catchet.dk")

                    // adding a generated user to being a participant in the study
                    runBlocking{
                        val participant = instance.addParticipant(studyId, dummyEmail)
                        print(participant)
/*
                        val group = setOf("AssignedParticipantRoles.ROLE1", "AssignedParticipantRoles.ROLE2") // Replace with your set of roles
*/
                        /* change */
                        val assignedParticipantRoles = AssignedParticipantRoles(participant.id, AssignedTo.Roles(setOf("Participant")))

                        val assignedRolesSet: Set<AssignedParticipantRoles> = setOf(assignedParticipantRoles)

                        val participantGroupStatus = instance.inviteNewParticipantGroup(studyId, assignedRolesSet)

                        print(participantGroupStatus)

                    }

                    val csvDataList = listOf(MagicLink(link, userId , studyDeploymentId = "Need to retrieve", expiryDate))
                }


            } catch (ex: Exception) {
                // Handle exceptions
                throw ex
            } finally {
                // Cleanup or finalization code
            }

        }
    }
}