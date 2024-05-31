package dk.cachet.carp.webservices.study.authorization

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
    private val auth: AuthorizationService,
) : ApplicationServiceAuthorizer<StudyService, StudyServiceRequest<*>> {
    override fun StudyServiceRequest<*>.authorize() =
        when (this) {
            is StudyServiceRequest.CreateStudy -> {
                auth.require(Role.RESEARCHER)
                auth.requireOwner(ownerId)
            }
            is StudyServiceRequest.GetStudiesOverview -> auth.requireOwner(ownerId)

            // the duplication seems unavoidable if we still want to keep exhaustive pattern matching
            is StudyServiceRequest.SetInternalDescription -> auth.require(Claim.ManageStudy(studyId))
            is StudyServiceRequest.GetStudyDetails -> auth.require(Claim.ManageStudy(studyId))
            is StudyServiceRequest.GetStudyStatus -> auth.require(Claim.ManageStudy(studyId))
            is StudyServiceRequest.SetInvitation -> auth.require(Claim.ManageStudy(studyId))
            is StudyServiceRequest.SetProtocol -> auth.require(Claim.ManageStudy(studyId))
            is StudyServiceRequest.RemoveProtocol -> auth.require(Claim.ManageStudy(studyId))
            is StudyServiceRequest.GoLive -> auth.require(Claim.ManageStudy(studyId))
            is StudyServiceRequest.Remove -> auth.require(Claim.ManageStudy(studyId))
        }

    override suspend fun StudyServiceRequest<*>.changeClaimsOnSuccess(result: Any?) =
        when (this) {
            is StudyServiceRequest.CreateStudy -> {
                require(result is StudyStatus)
                auth.grantCurrentAuthentication(Claim.ManageStudy(result.studyId))
            }
            is StudyServiceRequest.SetInternalDescription,
            is StudyServiceRequest.GetStudyDetails,
            is StudyServiceRequest.GetStudyStatus,
            is StudyServiceRequest.GetStudiesOverview,
            is StudyServiceRequest.SetInvitation,
            is StudyServiceRequest.SetProtocol,
            is StudyServiceRequest.RemoveProtocol,
            is StudyServiceRequest.GoLive,
            -> Unit
            is StudyServiceRequest.Remove -> {
                auth.revokeClaimFromAllAccounts(Claim.ManageStudy(studyId))
            }
        }
}
