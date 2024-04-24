package dk.cachet.carp.webservices.protocol.service

import dk.cachet.carp.protocols.infrastructure.ProtocolServiceDecorator
import dk.cachet.carp.webservices.protocol.dto.GetLatestProtocolResponseDto

interface ProtocolService
{
    val core: ProtocolServiceDecorator
    fun getLatestProtocolById(protocolId: String): GetLatestProtocolResponseDto?
}