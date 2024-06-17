package dk.cachet.carp.webservices.common.services

import dk.cachet.carp.common.application.services.createApplicationServiceAdapter
import dk.cachet.carp.data.infrastructure.DataStreamServiceDecorator
import dk.cachet.carp.deployments.application.DeploymentService
import dk.cachet.carp.deployments.application.DeploymentServiceHost
import dk.cachet.carp.deployments.application.ParticipationService
import dk.cachet.carp.deployments.application.ParticipationServiceHost
import dk.cachet.carp.deployments.domain.users.AccountService
import dk.cachet.carp.deployments.domain.users.ParticipantGroupService
import dk.cachet.carp.deployments.infrastructure.DeploymentServiceDecorator
import dk.cachet.carp.deployments.infrastructure.ParticipationServiceDecorator
import dk.cachet.carp.protocols.application.ProtocolFactoryServiceHost
import dk.cachet.carp.protocols.application.ProtocolServiceHost
import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceDecorator
import dk.cachet.carp.protocols.infrastructure.ProtocolServiceDecorator
import dk.cachet.carp.studies.application.RecruitmentService
import dk.cachet.carp.studies.application.RecruitmentServiceHost
import dk.cachet.carp.studies.application.StudyService
import dk.cachet.carp.studies.application.StudyServiceHost
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceDecorator
import dk.cachet.carp.studies.infrastructure.StudyServiceDecorator
import dk.cachet.carp.webservices.common.authorization.ApplicationServiceRequestAuthorizer
import dk.cachet.carp.webservices.common.eventbus.CoreEventBus
import dk.cachet.carp.webservices.data.authorization.DataStreamServiceAuthorizer
import dk.cachet.carp.webservices.data.service.core.CoreDataStreamService
import dk.cachet.carp.webservices.deployment.authorization.DeploymentServiceAuthorizer
import dk.cachet.carp.webservices.deployment.authorization.ParticipationServiceAuthorizer
import dk.cachet.carp.webservices.deployment.repository.CoreDeploymentRepository
import dk.cachet.carp.webservices.deployment.repository.CoreParticipationRepository
import dk.cachet.carp.webservices.protocol.authorization.ProtocolFactoryServiceAuthorizer
import dk.cachet.carp.webservices.protocol.authorization.ProtocolServiceAuthorizer
import dk.cachet.carp.webservices.protocol.repository.CoreProtocolRepository
import dk.cachet.carp.webservices.study.authorization.RecruitmentServiceAuthorizer
import dk.cachet.carp.webservices.study.authorization.StudyServiceAuthorizer
import dk.cachet.carp.webservices.study.repository.CoreParticipantRepository
import dk.cachet.carp.webservices.study.repository.CoreStudyRepository
import org.springframework.stereotype.Service

/**
 * Initializes all services used in the application. Services depending on other services can get a reference of the
 * plain service without any decorators, while anyone accessing them from the outside will always have to be authorized.
 */
@Suppress("LongParameterList")
@Service
class CoreServiceContainer(
    coreEventBus: CoreEventBus,
    // repositories
    participationRepository: CoreParticipationRepository,
    deploymentRepository: CoreDeploymentRepository,
    participantRepository: CoreParticipantRepository,
    protocolRepository: CoreProtocolRepository,
    studyRepository: CoreStudyRepository,
    // services
    cawsDataStreamService: CoreDataStreamService,
    accountService: AccountService,
    // authorizers
    dataStreamServiceAuthorizer: DataStreamServiceAuthorizer,
    deploymentServiceAuthorizer: DeploymentServiceAuthorizer,
    participationServiceAuthorizer: ParticipationServiceAuthorizer,
    protocolFactoryServiceAuthorizer: ProtocolFactoryServiceAuthorizer,
    protocolServiceAuthorizer: ProtocolServiceAuthorizer,
    recruitmentServiceAuthorizer: RecruitmentServiceAuthorizer,
    studyServiceAuthorizer: StudyServiceAuthorizer,
) {
    final val dataStreamService =
        DataStreamServiceDecorator(
            cawsDataStreamService,
        ) { command -> ApplicationServiceRequestAuthorizer(dataStreamServiceAuthorizer, command) }

    private val _deploymentService =
        DeploymentServiceHost(
            deploymentRepository,
            cawsDataStreamService,
            coreEventBus.createApplicationServiceAdapter(DeploymentService::class),
        )
    final val deploymentService =
        DeploymentServiceDecorator(
            _deploymentService,
        ) { command -> ApplicationServiceRequestAuthorizer(deploymentServiceAuthorizer, command) }

    private val _participationService =
        ParticipationServiceHost(
            participationRepository,
            ParticipantGroupService(accountService),
            coreEventBus.createApplicationServiceAdapter(ParticipationService::class),
        )
    final val participationService =
        ParticipationServiceDecorator(
            _participationService,
        ) { command -> ApplicationServiceRequestAuthorizer(participationServiceAuthorizer, command) }

    private val _protocolFactoryService = ProtocolFactoryServiceHost()
    final val protocolFactoryService =
        ProtocolFactoryServiceDecorator(
            _protocolFactoryService,
        ) { command -> ApplicationServiceRequestAuthorizer(protocolFactoryServiceAuthorizer, command) }

    private val _protocolService = ProtocolServiceHost(protocolRepository)
    final val protocolService =
        ProtocolServiceDecorator(
            _protocolService,
        ) { command -> ApplicationServiceRequestAuthorizer(protocolServiceAuthorizer, command) }

    private val _recruitmentService =
        RecruitmentServiceHost(
            participantRepository,
            _deploymentService,
            coreEventBus.createApplicationServiceAdapter(RecruitmentService::class),
        )
    final val recruitmentService =
        RecruitmentServiceDecorator(
            _recruitmentService,
        ) { command -> ApplicationServiceRequestAuthorizer(recruitmentServiceAuthorizer, command) }

    private val _studyService =
        StudyServiceHost(
            studyRepository,
            coreEventBus.createApplicationServiceAdapter(StudyService::class),
        )
    final val studyService =
        StudyServiceDecorator(
            _studyService,
        ) { command -> ApplicationServiceRequestAuthorizer(studyServiceAuthorizer, command) }
}
