package dk.cachet.carp.webservices.protocol.service.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.protocols.application.StudyProtocolSnapshot
import dk.cachet.carp.protocols.domain.StudyProtocol
import dk.cachet.carp.webservices.protocol.dto.GetLatestProtocolResponseDto
import dk.cachet.carp.webservices.protocol.repository.ProtocolRepository
import dk.cachet.carp.webservices.protocol.service.ProtocolService
import dk.cachet.carp.webservices.protocol.service.core.CoreProtocolService
import org.springframework.stereotype.Service

@Service
class ProtocolServiceImpl(
    private val protocolRepository: ProtocolRepository,
    private val objectMapper: ObjectMapper,
    coreProtocolService: CoreProtocolService
): ProtocolService
{
    final override val core = coreProtocolService.instance

    override fun getLatestProtocolById(protocolId: String): GetLatestProtocolResponseDto?
    {
        val latestVersion = protocolRepository.findLatestById(protocolId)
        val firstVersion = protocolRepository.findFirstById(protocolId)
        if (latestVersion.isPresent && firstVersion.isPresent)
        {
            val protocol = convertJsonNodeToStudyProtocol(latestVersion.get().snapshot!!)
            return GetLatestProtocolResponseDto(
                latestVersion.get().versionTag,
                protocol.getSnapshot(),
                firstVersion.get().createdAt!!,
                latestVersion.get().createdAt!!
            )
        }
        return null
    }

    private fun convertJsonNodeToStudyProtocol(jsonNode: JsonNode): StudyProtocol
    {
        val snapshot = objectMapper.treeToValue(jsonNode, StudyProtocolSnapshot::class.java)
        return StudyProtocol.fromSnapshot(snapshot)
    }
}