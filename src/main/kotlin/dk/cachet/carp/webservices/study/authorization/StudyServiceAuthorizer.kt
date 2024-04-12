package dk.cachet.carp.webservices.study.authorization

import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyStatus
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceAuthorizer
import dk.cachet.carp.webservices.security.authorization.Claim
import dk.cachet.carp.webservices.security.authorization.Role
import dk.cachet.carp.webservices.security.authorization.service.AuthorizationService
import org.springframework.stereotype.Service

@Service
class StudyServiceAuthorizer(
    private val authorizationService: AuthorizationService
) : ApplicationServiceAuthorizer<StudyService, StudyServiceRequest<*>>
{
    override fun StudyServiceRequest<*>.authorize() =
        when ( this )
        {
            is StudyServiceRequest.CreateStudy -> {
                authorizationService.require( Role.RESEARCHER )
                authorizationService.requireOwner( ownerId )
            }
            is StudyServiceRequest.GetStudiesOverview -> authorizationService.requireOwner( ownerId )

            // the duplication seems unavoidable if we still want to keep exhaustive pattern matching
            is StudyServiceRequest.SetInternalDescription -> authorizationService.require( Claim.ManageStudy( studyId ) )
            is StudyServiceRequest.GetStudyDetails -> authorizationService.require( Claim.ManageStudy( studyId ) )
            is StudyServiceRequest.GetStudyStatus -> authorizationService.require( Claim.ManageStudy( studyId ) )
            is StudyServiceRequest.SetInvitation -> authorizationService.require( Claim.ManageStudy( studyId ) )
            is StudyServiceRequest.SetProtocol -> authorizationService.require( Claim.ManageStudy( studyId ) )
            is StudyServiceRequest.RemoveProtocol -> authorizationService.require( Claim.ManageStudy( studyId ) )
            is StudyServiceRequest.GoLive -> authorizationService.require( Claim.ManageStudy( studyId ) )
            is StudyServiceRequest.Remove -> authorizationService.require( Claim.ManageStudy( studyId ) )
        }

    override suspend fun StudyServiceRequest<*>.grantClaimsOnSuccess( result: Any? ) =
        when ( this )
        {
            is StudyServiceRequest.CreateStudy -> {
                require( result is StudyStatus )
                authorizationService.grantCurrentAuthentication( Claim.ManageStudy( result.studyId ) )
            }
            is StudyServiceRequest.SetInternalDescription,
            is StudyServiceRequest.GetStudyDetails,
            is StudyServiceRequest.GetStudyStatus,
            is StudyServiceRequest.GetStudiesOverview,
            is StudyServiceRequest.SetInvitation,
            is StudyServiceRequest.SetProtocol,
            is StudyServiceRequest.RemoveProtocol,
            is StudyServiceRequest.GoLive,
            is StudyServiceRequest.Remove -> Unit

        }
}