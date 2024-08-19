package dk.cachet.carp.webservices.common.serialisers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.devices.DeviceRegistration
import dk.cachet.carp.common.application.users.AccountIdentity
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.application.DataStreamsConfiguration
import dk.cachet.carp.data.application.Measurement
import dk.cachet.carp.data.application.SyncPoint
import dk.cachet.carp.deployments.application.PrimaryDeviceDeployment
import dk.cachet.carp.deployments.application.StudyDeploymentStatus
import dk.cachet.carp.deployments.application.users.ActiveParticipationInvitation
import dk.cachet.carp.deployments.application.users.ParticipantData
import dk.cachet.carp.deployments.domain.StudyDeploymentSnapshot
import dk.cachet.carp.deployments.domain.users.ParticipantGroupSnapshot
import dk.cachet.carp.protocols.application.ProtocolVersion
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.studies.domain.users.RecruitmentSnapshot
import dk.cachet.carp.webservices.account.serdes.AccountIdentityDeserializer
import dk.cachet.carp.webservices.account.serdes.AccountIdentitySerializer
import dk.cachet.carp.webservices.common.configuration.internationalisation.service.MessageBase
import dk.cachet.carp.webservices.common.serialisers.serdes.UUIDDeserializer
import dk.cachet.carp.webservices.common.serialisers.serdes.UUIDSerializer
import dk.cachet.carp.webservices.datastream.serdes.*
import dk.cachet.carp.webservices.deployment.serdes.*
import dk.cachet.carp.webservices.protocol.serdes.*
import dk.cachet.carp.webservices.study.serdes.*
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * The Configuration Class [ObjectMapperConfig].
 * The [ObjectMapperConfig] implements the [SimpleModule] that allows registration of serializers and deserializers,
 * bean serializer and deserializer modifiers, registration of subtypes and mix-ins as well as some other commonly
 * needed aspects [com.fasterxml.jackson.databind.AbstractTypeResolver].
 */
@Configuration
class ObjectMapperConfig(validationMessages: MessageBase) : SimpleModule() {
    init
    {
        // Snapshot Serializer
        this.addSerializer(StudyProtocolSnapshot::class.java, StudyProtocolSnapshotSerializer(validationMessages))
        this.addDeserializer(StudyProtocolSnapshot::class.java, StudyProtocolSnapshotDeserializer(validationMessages))
        // StudyDeploymentSnapshot
        this.addSerializer(StudyDeploymentSnapshot::class.java, StudyDeploymentSnapshotSerializer(validationMessages))
        this.addDeserializer(
            StudyDeploymentSnapshot::class.java,
            StudyDeploymentSnapshotDeserializer(validationMessages),
        )

        // DeviceRegistration
        this.addSerializer(DeviceRegistration::class.java, DeviceRegistrationSerializer(validationMessages))
        this.addDeserializer(DeviceRegistration::class.java, DeviceRegistrationDeserializer(validationMessages))

        // StudyDeploymentStatus
        this.addSerializer(StudyDeploymentStatus::class.java, StudyDeploymentStatusSerializer(validationMessages))
        this.addDeserializer(StudyDeploymentStatus::class.java, StudyDeploymentStatusDeserializer(validationMessages))
        // MasterDeviceDeployment
        this.addSerializer(PrimaryDeviceDeployment::class.java, PrimaryDeviceDeploymentSerializer(validationMessages))
        this.addDeserializer(
            PrimaryDeviceDeployment::class.java,
            MasterDeviceDeploymentDeserializer(validationMessages),
        )
        // AccountIdentity
        this.addSerializer(AccountIdentity::class.java, AccountIdentitySerializer(validationMessages))
        this.addDeserializer(AccountIdentity::class.java, AccountIdentityDeserializer(validationMessages))
        // UUID
        this.addSerializer(UUID::class.java, UUIDSerializer(validationMessages))
        this.addDeserializer(UUID::class.java, UUIDDeserializer(validationMessages))
        // ParticipantGroupStatus
        this.addSerializer(ParticipantGroupStatus::class.java, ParticipantGroupStatusSerializer(validationMessages))
        this.addDeserializer(
            ParticipantGroupStatus::class.java,
            ParticipantGroupStatusDeserializer(validationMessages),
        )

        // ParticipantGroupSnapshot
        this.addSerializer(ParticipantGroupSnapshot::class.java, ParticipantGroupSnapshotSerializer(validationMessages))
        this.addDeserializer(
            ParticipantGroupSnapshot::class.java,
            ParticipantGroupSnapshotDeserializer(validationMessages),
        )
        // Participant Data
        this.addSerializer(ParticipantData::class.java, ParticipantDataSerializer(validationMessages))
        this.addDeserializer(ParticipantData::class.java, ParticipantDataDeserializer(validationMessages))
        // Recruitment Snapshot
        this.addSerializer(RecruitmentSnapshot::class.java, RecruitmentSnapshotSerializer(validationMessages))
        this.addDeserializer(RecruitmentSnapshot::class.java, RecruitmentSnapshotDeserializer(validationMessages))
        // DataStreamConfiguration
        this.addSerializer(DataStreamsConfiguration::class.java, DataStreamsConfigurationSerializer(validationMessages))
        this.addDeserializer(
            DataStreamsConfiguration::class.java,
            DataStreamsConfigurationDeserializer(validationMessages),
        )

        // ProtocolVersion
        this.addSerializer(ProtocolVersion::class.java, ProtocolVersionSerializer(validationMessages))
        this.addDeserializer(ProtocolVersion::class.java, ProtocolVersionDeserializer(validationMessages))

        // SyncPoint
        this.addSerializer(SyncPoint::class.java, SyncPointSerializer(validationMessages))
        this.addDeserializer(SyncPoint::class.java, SyncPointDeserializer(validationMessages))
        // Measurement
        this.addSerializer(Measurement::class.java, MeasurementSerializer(validationMessages))
        this.addDeserializer(Measurement::class.java, MeasurementDeserializer(validationMessages))
        // Act
        this.addSerializer(
            ActiveParticipationInvitation::class.java,
            ActiveParticipationInvitationSerializer(validationMessages),
        )
        this.addDeserializer(
            ActiveParticipationInvitation::class.java,
            ActiveParticipationInvitationDeserializer(validationMessages),
        )
        // DataStreamBatch
        this.addSerializer(DataStreamBatch::class.java, DataStreamBatchSerializer(validationMessages))
        this.addDeserializer(DataStreamBatch::class.java, DataStreamBatchDeserializer(validationMessages))

        this.addSerializer(java.time.Instant::class.java, InstantSerializer.INSTANCE)
        this.addDeserializer(java.time.Instant::class.java, InstantDeserializer.INSTANT)

        // TODO: request change in http client and portal to use kotlinx.datetime.Instant
//        this.addSerializer(Instant::class.java, KInstantSerializer.INSTANCE)
    }

    class KInstantSerializer : JsonSerializer<Instant>() {
        override fun serialize(
            value: Instant,
            gen: JsonGenerator,
            serializers: SerializerProvider,
        ) {
            return(InstantSerializer.INSTANCE.serialize(value.toJavaInstant(), gen, serializers))
        }

        companion object {
            val INSTANCE = KInstantSerializer()
        }
    }

    /**
     * The function [objectMapper] provides functionality for reading and writing JSON, either to and from
     * basic POJOs (Plain Old Java Objects), or to and from a general-purpose JSON Tree Model.
     *
     * @return The [ObjectMapper] to register the object serialization.
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule.Builder().build())

        objectMapper.registerModule(this)

//        objectMapper.registerModule(JavaTimeModule())
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

        return objectMapper
    }
}
