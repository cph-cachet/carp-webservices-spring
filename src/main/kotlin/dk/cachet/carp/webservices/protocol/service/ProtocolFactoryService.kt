package dk.cachet.carp.webservices.protocol.service

import dk.cachet.carp.protocols.infrastructure.ProtocolFactoryServiceDecorator

interface ProtocolFactoryService
{
    val core: ProtocolFactoryServiceDecorator
}