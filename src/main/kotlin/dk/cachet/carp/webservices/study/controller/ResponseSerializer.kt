package dk.cachet.carp.webservices.study.controller

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.studies.application.StudyDetails
import dk.cachet.carp.studies.application.StudyStatus
import dk.cachet.carp.studies.application.users.Participant
import dk.cachet.carp.studies.application.users.ParticipantGroupStatus
import dk.cachet.carp.studies.infrastructure.RecruitmentServiceRequest
import dk.cachet.carp.studies.infrastructure.StudyServiceRequest
import dk.cachet.carp.webservices.common.serialisers.ResponseSerializer
import kotlinx.serialization.serializer

class StudyRequestSerializer : ResponseSerializer<StudyServiceRequest<*>>() {
    @Suppress("UNCHECKED_CAST")
    override fun <TService : ApplicationService<TService, *>> serializeResponse(
        request: ApplicationServiceRequest<TService, *>,
        content: Any?,
    ): Any? {
        return when (request) {
            is StudyServiceRequest -> {
                when (content) {
                    is StudyStatus ->
                        json.encodeToString(serializer<StudyStatus>(), content)
                    is StudyDetails ->
                        json.encodeToString(serializer<StudyDetails>(), content)
                    is List<*> ->
                        json.encodeToString(serializer<List<StudyStatus>>(), content as List<StudyStatus>)
                    is Boolean ->
                        json.encodeToString(serializer<Boolean>(), content)
                    else -> content
                }
            }
            else -> content
        }
    }
}

class RecruitmentRequestSerializer : ResponseSerializer<RecruitmentServiceRequest<*>>() {
    @Suppress("UNCHECKED_CAST")
    override fun <TService : ApplicationService<TService, *>> serializeResponse(
        request: ApplicationServiceRequest<TService, *>,
        content: Any?,
    ): Any? {
        return when (request) {
            is RecruitmentServiceRequest.AddParticipantByEmailAddress,
            is RecruitmentServiceRequest.AddParticipantByUsername,
            is RecruitmentServiceRequest.GetParticipant,
            ->
                json.encodeToString(serializer<Participant>(), content as Participant)
            is RecruitmentServiceRequest.GetParticipants ->
                json.encodeToString(serializer<List<Participant>>(), content as List<Participant>)
            is RecruitmentServiceRequest.InviteNewParticipantGroup,
            is RecruitmentServiceRequest.StopParticipantGroup,
            ->
                json.encodeToString(serializer<ParticipantGroupStatus>(), content as ParticipantGroupStatus)
            is RecruitmentServiceRequest.GetParticipantGroupStatusList ->
                json.encodeToString(serializer<List<ParticipantGroupStatus>>(), content as List<ParticipantGroupStatus>)
            else -> content
        }
    }
}
