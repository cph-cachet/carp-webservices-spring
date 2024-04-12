//package dk.cachet.carp.webservices.dataPoint.authorization
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import dk.cachet.carp.webservices.dataPoint.repository.DataPointRepository
//import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
//import dk.cachet.carp.webservices.deployment.repository.ParticipantGroupRepository
//import dk.cachet.carp.webservices.security.authentication.service.AuthenticationService
//import dk.cachet.carp.webservices.security.authorization.AuthorizationService
//import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
//import org.springframework.stereotype.Service
//
//@Service
//class DataPointAuthorizationService(
//        private val dataPointRepository: DataPointRepository,
//        studyService: CoreStudyRepository,
//        deploymentRepository: CoreDeploymentRepository,
//        participantGroupRepository: ParticipantGroupRepository,
//        objectMapper: ObjectMapper,
//        authenticationService: AuthenticationService
//): AuthorizationService(studyService, deploymentRepository, participantGroupRepository, objectMapper, authenticationService)
//{
//    fun canCreateDataPoint(deploymentId: String): Boolean {
//        if (isAccountSystemAdmin()) return true
//
//        return isResearcherPartOfTheDeployment(deploymentId) || isParticipantPartOfTheDeployment(deploymentId)
//    }
//
//    fun canViewDataPoint(deploymentId: String, dataPointId: Int): Boolean {
//        if (isAccountSystemAdmin()) return true
//
//        return isResearcherPartOfTheDeployment(deploymentId) || isCreator(dataPointId)
//    }
//
//    private fun isCreator(dataPointId: Int): Boolean
//    {
//        val userAccountId = authenticationService.getCurrentPrincipal().id
//
//        return dataPointRepository.findById(dataPointId)
//            .map { dataPoint -> dataPoint.createdBy == userAccountId }.orElse(false)
//    }
//}