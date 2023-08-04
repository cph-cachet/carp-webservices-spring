package dk.cachet.carp.webservices.data.service

import dk.cachet.carp.webservices.data.domain.DataStreamSequence

interface IDataStreamService {
    fun getDataStream(deploymentIds: List<String>) : List<DataStreamSequence>
}