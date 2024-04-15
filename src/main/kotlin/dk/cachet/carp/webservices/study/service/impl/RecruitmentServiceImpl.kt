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
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.study.domain.AnonymousParticipant
import dk.cachet.carp.webservices.study.domain.ParticipantAccount
import dk.cachet.carp.webservices.study.domain.ParticipantGroupInfo
import dk.cachet.carp.webservices.study.domain.ParticipantGroupsStatus
import dk.cachet.carp.webservices.study.service.RecruitmentService
import dk.cachet.carp.webservices.study.service.StudyService
import dk.cachet.carp.webservices.study.service.core.CoreRecruitmentService
import kotlinx.datetime.Clock
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Service
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Service
class RecruitmentServiceImpl(
    private val accountService: AccountService,
    private val dataStreamService: DataStreamService,
    private val validationMessages: MessageBase,
    private val studyService: StudyService,
    coreRecruitmentService: CoreRecruitmentService
) : RecruitmentService
{
    final override val core = coreRecruitmentService.instance

    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    override suspend fun inviteResearcher(studyId: UUID, email: String )
    {
        val accountIdentity = AccountIdentity.fromEmailAddress( email )
        var account = accountService.findByAccountIdentity(accountIdentity)

        if ( account == null )
        {
            LOGGER.info("Account with email $email is not found.")
            account = accountService.invite(accountIdentity, Role.RESEARCHER)
        }

        if ( account.role!! < Role.RESEARCHER ) {
            accountService.addRole( accountIdentity, Role.RESEARCHER )
            LOGGER.info("Account with email $email is granted the role RESEARCHER.")
        }

        if ( account.carpClaims?.contains( Claim.ManageStudy( studyId ) ) == true )
        {
            LOGGER.info("Study with id $studyId already contains the account with id ${account.id}")
            throw IllegalArgumentException(validationMessages.get("study.core.invite.researcher.exists", account.id!!, studyId))
        }

        accountService.grant( accountIdentity, setOf( Claim.ManageStudy( studyId ) ) )
        LOGGER.info("Account with email $email is added as a researcher to study with id $studyId.")
    }

    override suspend fun removeResearcher(studyId: UUID, email: String ): Boolean
    {
        val claim = Claim.ManageStudy( studyId )

        val account = accountService.revoke(
            AccountIdentity.fromEmailAddress( email ),
            setOf( claim )
        )

        return account.carpClaims?.contains( claim ) == false
    }

    override suspend fun getParticipants(studyId: UUID ) : List<Account>
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

        return accounts
    }

    override suspend fun getParticipantGroupsStatus( studyId: UUID ): ParticipantGroupsStatus
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

        return ParticipantGroupsStatus( participantGroupInfoList, participantGroupStatusList )
    }

    override suspend fun addAnonymousParticipants(
        studyId: UUID,
        amount: Int,
        expirationSeconds: Long,
        participantRoleName: String,
        redirectUri: String?
    ): List<AnonymousParticipant> {
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

            val link = accountService.generateTemporaryAccount(
                UsernameAccountIdentity(username.toString()),
                expirationSeconds,
                redirectUri
            )

            val participant = core.addParticipant(studyId, Username(username.toString()))
            val groupStatus = core.inviteNewParticipantGroup(
                studyId,
                setOf(AssignedParticipantRoles(participant.id, AssignedTo.Roles(setOf(participantRoleName))))
            ) as ParticipantGroupStatus.InDeployment

            participants.add(
                AnonymousParticipant(
                    username,
                    groupStatus.studyDeploymentStatus.studyDeploymentId,
                    link,
                    Clock.System.now() + expirationSeconds.toDuration(DurationUnit.SECONDS)
                )
            )
        }

        return participants
    }
}