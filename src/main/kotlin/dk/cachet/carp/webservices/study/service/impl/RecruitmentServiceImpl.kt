package dk.cachet.carp.webservices.study.service.impl

import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.common.application.users.AssignedTo
import dk.cachet.carp.common.application.users.Username
import dk.cachet.carp.common.application.users.UsernameAccountIdentity
import dk.cachet.carp.studies.application.users.AssignedParticipantRoles
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.webservices.account.service.AccountService
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.data.service.DataStreamService
import dk.cachet.carp.webservices.security.authentication.domain.Account
import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import dk.cachet.carp.webservices.security.config.SecurityCoroutineContext
import dk.cachet.carp.webservices.study.domain.AnonymousParticipant
import dk.cachet.carp.webservices.study.domain.ParticipantAccount
import dk.cachet.carp.webservices.study.domain.ParticipantGroupInfo
import dk.cachet.carp.webservices.study.domain.ParticipantGroupsStatus
import dk.cachet.carp.webservices.study.service.RecruitmentService
import dk.cachet.carp.webservices.study.service.StudyService
import dk.cachet.carp.webservices.study.service.core.CoreRecruitmentService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
class RecruitmentServiceImpl(
    private val authorizationService: AuthorizationService,
    private val accountService: AccountService,
    private val dataStreamService: DataStreamService,
    private val studyService: StudyService,
    coreRecruitmentService: CoreRecruitmentService
) : RecruitmentService
{
    final override val core = coreRecruitmentService.instance

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override suspend fun inviteResearcher( studyId: UUID, email: String ) =
        withContext( Dispatchers.IO + SecurityCoroutineContext() )
        {
            val accountIdentity = AccountIdentity.fromEmailAddress( email )
            var account = accountService.findByAccountIdentity(accountIdentity)

            if ( account == null )
            {
                LOGGER.info("Account with email $email is not found. Inviting...")
                account = accountService.invite(accountIdentity, Role.RESEARCHER)
            }

            if ( account.role!! < Role.RESEARCHER ) {
                accountService.addRole( accountIdentity, Role.RESEARCHER )
                LOGGER.info("Account with email $email is granted the role RESEARCHER.")
            }

            // grant it claims for the study and every deployment within it
            accountService.grant(
                accountIdentity,
                core.getParticipantGroupStatusList( studyId )
                    .filterIsInstance<ParticipantGroupStatus.InDeployment>()
                    .map { Claim.ManageDeployment( it.studyDeploymentStatus.studyDeploymentId ) }
                    .toSet()
                    .plus( Claim.ManageStudy( studyId ) )
            )

            LOGGER.info("Account with email $email is added as a researcher to study with id $studyId.")
    }

    override suspend fun removeResearcher( studyId: UUID, email: String ): Boolean =
        withContext( Dispatchers.IO + SecurityCoroutineContext() )
        {
            val accountIdentity = AccountIdentity.fromEmailAddress( email )

            // get the claims for each deployment in the study and for the study itself
            val claims = core.getParticipantGroupStatusList( studyId )
                .filterIsInstance<ParticipantGroupStatus.InDeployment>()
                .map { Claim.ManageDeployment( it.studyDeploymentStatus.studyDeploymentId ) }
                .toSet()
                .plus( Claim.ManageStudy( studyId ) )

            // revoke them
            val account = accountService.revoke( accountIdentity, claims )

            account.carpClaims?.intersect( claims )?.isEmpty() ?: false
        }

    override suspend fun getParticipants( studyId: UUID ) : List<Account> =
        withContext( Dispatchers.IO + SecurityCoroutineContext() )
        {
            val participants = core.getParticipants( studyId )
            val accounts = arrayListOf<Account>()
            for ( participant in participants )
            {
                val account = accountService.findByAccountIdentity( participant.accountIdentity )
                if ( account != null )
                {
                    accounts.add( account )
                }
                else
                {
                    accounts.add( Account.fromAccountIdentity( participant.accountIdentity ) )
                }
            }

            accounts
        }

    override fun isParticipant(studyId: UUID, accountId: UUID): Boolean =
        runBlocking( SecurityCoroutineContext() ) {
            getParticipants(studyId).any { it.id == accountId.toString() }
        }

    override suspend fun getParticipantGroupsStatus( studyId: UUID ): ParticipantGroupsStatus =
        withContext( Dispatchers.IO + SecurityCoroutineContext() )
        {
            val participantGroupStatusList = core.getParticipantGroupStatusList( studyId )

            val participantGroupInfoList = participantGroupStatusList
                .filterIsInstance<ParticipantGroupStatus.InDeployment>()
                .map {
                    val participantAccounts = it.participants.map { participant ->
                        val participantAccount = ParticipantAccount.fromParticipant( participant )
                        val account = accountService.findByAccountIdentity( participant.accountIdentity )

                        if ( account != null )
                        {
                            val lastDataUpload = dataStreamService.getLatestUpdatedAt( it.studyDeploymentStatus.studyDeploymentId )
                            participantAccount.lateInitFrom( account )

                            // TODO: we cannot track this for participants, only for deployments
                            participantAccount.dateOfLastDataUpload = lastDataUpload
                        }

                        participantAccount
                    }

                    ParticipantGroupInfo( it.id, it.studyDeploymentStatus, participantAccounts )
                }

            ParticipantGroupsStatus( participantGroupInfoList, participantGroupStatusList )
        }

    override suspend fun addAnonymousParticipants(
        studyId: UUID,
        amount: Int,
        expirationSeconds: Long,
        participantRoleName: String,
        redirectUri: String?
    ): List<AnonymousParticipant> =
        withContext( Dispatchers.IO + SecurityCoroutineContext() )
        {
            LOGGER.info("Generating $amount anonymous participants for study $studyId")

            val protocol = studyService.core.getStudyDetails( studyId ).protocolSnapshot
            requireNotNull( protocol ) { "No protocol found for study $studyId." }

            require( protocol.participantRoles.any { participantRole -> participantRole.role == participantRoleName } )
            {
                "Participant role '$participantRoleName' not found in study protocol."
            }

            val participants = arrayListOf<AnonymousParticipant>()

            for (i in 0 until amount) {
                val username = UUID.randomUUID()
                val identity = UsernameAccountIdentity(username.toString())

                // generate account and get a link that authenticates it
                val link = accountService.generateTemporaryAccount(
                    identity,
                    expirationSeconds,
                    redirectUri
                )

                // create the corresponding participant in core and deploy the participant group with a single participant
                val participant = core.addParticipant( studyId, Username(username.toString() ) )
                val groupStatus = core.inviteNewParticipantGroup(
                    studyId,
                    setOf(
                        AssignedParticipantRoles(
                            participant.id,
                            AssignedTo.Roles( setOf(participantRoleName) )
                        )
                    )
                ) as ParticipantGroupStatus.InDeployment

                val deploymentId = groupStatus.studyDeploymentStatus.studyDeploymentId

                // grant the researchers the necessary claims to manage the deployment
                authorizationService.grantEveryoneWithExistingClaim(
                    Claim.ManageStudy( studyId ),
                    Claim.ManageDeployment( deploymentId )
                )

                participants.add(
                    AnonymousParticipant(
                        username,
                        deploymentId,
                        link,
                        Clock.System.now() + expirationSeconds.toDuration(DurationUnit.SECONDS)
                    )
                )
            }

            participants
    }
}