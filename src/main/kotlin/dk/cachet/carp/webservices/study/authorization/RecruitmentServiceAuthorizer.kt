package dk.cachet.carp.webservices.study.authorization

import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceAuthorizer
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import org.springframework.stereotype.Service

@Service
class RecruitmentServiceAuthorizer(
   private val auth: AuthorizationService
) : ApplicationServiceAuthorizer<RecruitmentService, RecruitmentServiceRequest<*>>
{
    override fun RecruitmentServiceRequest<*>.authorize() =
        when ( this )
        {
            is RecruitmentServiceRequest.AddParticipantByEmailAddress -> auth.require( Claim.ManageStudy( studyId ) )
            is RecruitmentServiceRequest.AddParticipantByUsername -> auth.require( Claim.ManageStudy( studyId ) )
            is RecruitmentServiceRequest.GetParticipant -> auth.require( Claim.ManageStudy( studyId ) )
            is RecruitmentServiceRequest.GetParticipants -> auth.require( Claim.ManageStudy( studyId ) )
            is RecruitmentServiceRequest.InviteNewParticipantGroup -> auth.require( Claim.ManageStudy( studyId ) )
            is RecruitmentServiceRequest.GetParticipantGroupStatusList -> auth.require( Claim.ManageStudy( studyId ) )
            is RecruitmentServiceRequest.StopParticipantGroup -> auth.require( Claim.ManageStudy( studyId ) )
        }

    override suspend fun RecruitmentServiceRequest<*>.grantClaimsOnSuccess( result: Any? ) =
        when ( this )
        {
            is RecruitmentServiceRequest.AddParticipantByEmailAddress,
            is RecruitmentServiceRequest.AddParticipantByUsername,
            is RecruitmentServiceRequest.GetParticipant,
            is RecruitmentServiceRequest.GetParticipants,
            is RecruitmentServiceRequest.InviteNewParticipantGroup,
            is RecruitmentServiceRequest.GetParticipantGroupStatusList,
            is RecruitmentServiceRequest.StopParticipantGroup -> Unit
        }
}