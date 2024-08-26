package dk.cachet.carp.webservices.datastream.controller

import dk.cachet.carp.common.application.services.ApplicationService
import dk.cachet.carp.common.infrastructure.services.ApplicationServiceRequest
import dk.cachet.carp.data.application.DataStreamBatch
import dk.cachet.carp.data.infrastructure.DataStreamServiceRequest
import dk.cachet.carp.webservices.common.serialisers.ResponseSerializer
import kotlinx.serialization.serializer

class DataStreamRequestSerializer : ResponseSerializer<DataStreamServiceRequest<*>>() {
    @Suppress("UNCHECKED_CAST")
    override fun <TService : ApplicationService<TService, *>> serializeResponse(
        request: ApplicationServiceRequest<TService, *>,
        content: Any?,
    ): Any? {
        return when (request) {
            is DataStreamServiceRequest.GetDataStream ->
                json.encodeToString(DataStreamBatch.serializer(), content as DataStreamBatch)
            is DataStreamServiceRequest.OpenDataStreams,
            is DataStreamServiceRequest.AppendToDataStreams,
            is DataStreamServiceRequest.CloseDataStreams,
            ->
                json.encodeToString(serializer<Unit>(), content as Unit)
            else -> content
        }
    }
}
