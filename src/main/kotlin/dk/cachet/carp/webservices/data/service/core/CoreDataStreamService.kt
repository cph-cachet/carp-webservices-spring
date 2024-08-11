package dk.cachet.carp.webservices.data.service.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import dk.cachet.carp.common.application.UUID
import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.intersect
import dk.cachet.carp.data.application.*
import dk.cachet.carp.webservices.data.domain.DataStreamConfiguration
import dk.cachet.carp.webservices.data.domain.DataStreamSequence
import dk.cachet.carp.webservices.data.domain.DataStreamSnapshot
import dk.cachet.carp.webservices.data.repository.DataStreamConfigurationRepository
import dk.cachet.carp.webservices.data.repository.DataStreamIdRepository
import dk.cachet.carp.webservices.data.repository.DataStreamSequenceRepository
import dk.cachet.carp.webservices.data.service.impl.CawsMutableDataStreamBatchWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.stereotype.Component

@Component
class CoreDataStreamService(
    private val configRepository: DataStreamConfigurationRepository,
    private val dataStreamIdRepository: DataStreamIdRepository,
    private val dataStreamSequenceRepository: DataStreamSequenceRepository,
    private val objectMapper: ObjectMapper,
) : DataStreamService {
    companion object {
        private val LOGGER: Logger = LogManager.getLogger()
    }

    /**
     * Append a [batch] of data point sequences to corresponding data streams in [studyDeploymentId].
     *
     * @throws IllegalArgumentException when:
     *  - the `studyDeploymentId` of one or more sequences in [batch] does not match [studyDeploymentId]
     *  - the start of one or more of the sequences contained in [batch]
     *  precede the end of a previously appended sequence to the same data stream
     *  - [batch] contains a sequence with [DataStreamId] which wasn't configured for [studyDeploymentId]
     * @throws IllegalStateException when data streams for [studyDeploymentId] have been closed.
     */
    override suspend fun appendToDataStreams(
        studyDeploymentId: UUID,
        batch: DataStreamBatch,
    ) {
        // the `studyDeploymentId` of one or more sequences in [batch] does not match [studyDeploymentId]
        require(
            batch.sequences.all {
                it.dataStream.studyDeploymentId == studyDeploymentId
            },
        ) { "The study deployment ID of one or more sequences in `batch` doesn't match `studyDeploymentId`." }

        // whether there is a config present in the database
        val configOptional =
            withContext(Dispatchers.IO) {
                configRepository.findById(studyDeploymentId.stringRepresentation)
            }

        require(configOptional.isPresent) {
            "No configuration was found for studyDeploymentId ${studyDeploymentId.stringRepresentation}."
        }

        val config = mapToCoreConfig(configOptional.get().config!!)

        // checks whether any of the streams wasn't configured for studyDeploymentId
        require(
            batch.sequences.all {
                it.dataStream in config.expectedDataStreamIds
            },
        ) { "The batch contains a sequence with a data stream which wasn't configured for this study deployment." }

        val dataStreams = CawsMutableDataStreamBatchWrapper()

        // appending sequences to batch
        dataStreams.appendBatch(batch)

        // save the sequences
        val dataStreamSequence =
            dataStreams.sequences.map {
                val dataStream = it.dataStream
                val dataStreamId =
                    dataStreamIdRepository.findByStudyDeploymentIdAndDeviceRoleNameAndNameAndNameSpace(
                        studyDeploymentId = dataStream.studyDeploymentId.stringRepresentation,
                        deviceRoleName = dataStream.deviceRoleName,
                        name = dataStream.dataType.name,
                        nameSpace = dataStream.dataType.namespace,
                    ).get()

                val snapshot = DataStreamSnapshot(it.measurements, it.triggerIds, it.syncPoint)

                // to ws save_to_db type
                DataStreamSequence(
                    dataStreamId = dataStreamId.id,
                    firstSequenceId = it.firstSequenceId,
                    lastSequenceId = it.range.last,
                    snapshot = objectMapper.valueToTree(snapshot),
                )
            }

        dataStreamSequenceRepository.saveAll(dataStreamSequence.asIterable())
    }

    /**
     * Stop accepting incoming data for all data streams for each of the [studyDeploymentIds].
     *
     * @throws IllegalArgumentException when no data streams were ever opened for any of the [studyDeploymentIds].
     */
    override suspend fun closeDataStreams(studyDeploymentIds: Set<UUID>) {
        val configs =
            withContext(Dispatchers.IO) {
                configRepository.getConfigurationsForIds(studyDeploymentIds.map { it.stringRepresentation })
            }

        require(configs.size == studyDeploymentIds.size) { "One of the configurations passed is not valid." }

        configs.forEach { it.closed = true }
        configRepository.saveAll(configs)
    }

    /**
     * Retrieve all data points in [dataStream] that fall within the inclusive range
     * defined by [fromSequenceId] and [toSequenceIdInclusive].
     * If [toSequenceIdInclusive] is null, all data points starting [fromSequenceId] are returned.
     *
     * In case no data for [dataStream] is stored in this repository, or is available for the specified range,
     * an empty [DataStreamBatch] is returned.
     *
     * @throws IllegalArgumentException if:
     *  - [dataStream] has never been opened
     *  - [fromSequenceId] is negative or [toSequenceIdInclusive] is smaller than [fromSequenceId]
     */
    @Suppress("LongMethod")
    override suspend fun getDataStream(
        dataStream: DataStreamId,
        fromSequenceId: Long,
        toSequenceIdInclusive: Long?,
    ): DataStreamBatch {
        when {
            fromSequenceId < 0 -> throw IllegalArgumentException(
                "[fromSequenceId] is negative for requested dataStream " +
                    "with studyDeploymentId ${dataStream.studyDeploymentId.stringRepresentation}",
            )

            toSequenceIdInclusive != null && fromSequenceId > toSequenceIdInclusive -> throw IllegalArgumentException(
                "[toSequenceIdInclusive] is smaller than [fromSequenceId] for requested dataStream " +
                    "with studyDeploymentId ${dataStream.studyDeploymentId.stringRepresentation}",
            )
        }

        // whether there is a config present in the database
        val configOptional =
            withContext(Dispatchers.IO) {
                configRepository.findById(dataStream.studyDeploymentId.stringRepresentation)
            }

        require(configOptional.isPresent) {
            "No configuration was found " +
                "for studyDeploymentId ${dataStream.studyDeploymentId.stringRepresentation} or study is closed"
        }

        val config = mapToCoreConfig(configOptional.get().config!!)

        // checks if dataStream is configured for studyDeploymentId
        require(
            dataStream in config.expectedDataStreamIds,
        ) { "Data stream wasn't configured for this study deployment." }

        val dataStreamId =
            withContext(Dispatchers.IO) {
                dataStreamIdRepository.findByStudyDeploymentIdAndDeviceRoleNameAndNameAndNameSpace(
                    studyDeploymentId = dataStream.studyDeploymentId.stringRepresentation,
                    deviceRoleName = dataStream.deviceRoleName,
                    name = dataStream.dataType.name,
                    nameSpace = dataStream.dataType.namespace,
                )
            }.get()

        val toSequenceId = toSequenceIdInclusive?.toInt() ?: Int.MAX_VALUE

        val dataStreamSequences =
            withContext(Dispatchers.IO) {
                dataStreamSequenceRepository.findAllBySequenceIdRange(
                    dataStreamId.id,
                    fromSequenceId.toInt(),
                    toSequenceId,
                )
            }

        return dataStreamSequences
            .mapNotNull {
                val queryRange = fromSequenceId.rangeTo(toSequenceId)
                val range = (it.firstSequenceId!!)..(it.lastSequenceId!!)
                val subRange = range.intersect(queryRange)
                if (subRange.isEmpty()) {
                    null
                } else {
                    val snapshot = mapToDataStreamSnapshot(it.snapshot!!)
                    MutableDataStreamSequence<Data>(dataStream, subRange.first, snapshot.triggerIds, snapshot.syncPoint)
                        .apply {
                            val startOffset = subRange.first - range.first
                            val exclusiveEnd = startOffset + subRange.last - subRange.first + 1
                            check(
                                startOffset <= Int.MAX_VALUE && exclusiveEnd <= Int.MAX_VALUE,
                            ) { "Exceeded capacity of measurements which can be held in memory." }
                            appendMeasurements(snapshot.measurements.subList(startOffset.toInt(), exclusiveEnd.toInt()))
                        }
                }
            }
            .fold(CawsMutableDataStreamBatchWrapper()) { batch, sequence ->
                batch.apply { appendSequence(sequence) }
            }
    }

    /**
     * Start accepting data for a study deployment for data streams configured in [configuration].
     *
     * @throws IllegalStateException when data streams for the specified study deployment have already been configured.
     */
    override suspend fun openDataStreams(configuration: DataStreamsConfiguration) {
        val id = configuration.studyDeploymentId.stringRepresentation

        check(
            !withContext(Dispatchers.IO) {
                configRepository.existsById(id)
            },
        ) {
            "Data streams for deployment with \\\"$id\\\" have already been configured."
        }

        val node = objectMapper.valueToTree<JsonNode>(configuration)
        withContext(Dispatchers.IO) {
            configRepository.save(DataStreamConfiguration(id, node))
        }
        LOGGER.info("New data stream configuration is saved for deployment with id $id.")

        val ids =
            configuration.expectedDataStreamIds.map {
                dk.cachet.carp.webservices.data.domain.DataStreamId(
                    studyDeploymentId = it.studyDeploymentId.stringRepresentation,
                    deviceRoleName = it.deviceRoleName,
                    name = it.dataType.name,
                    nameSpace = it.dataType.namespace,
                )
            }
        dataStreamIdRepository.saveAll(ids)
        LOGGER.info("New data stream id(s) is saved for deployment with id $id.")
    }

    /**
     * Close data streams and remove all data for each of the [studyDeploymentIds].
     *
     * @return The IDs of the study deployments for which data streams were configured.
     * IDs for which no study deployment exists are ignored.
     */
    override suspend fun removeDataStreams(studyDeploymentIds: Set<UUID>): Set<UUID> {
        // close study deployments
        closeDataStreams(studyDeploymentIds)

        val deploymentIds = HashSet<UUID>()

        studyDeploymentIds.map {
            val configOptional = configRepository.findById(it.stringRepresentation)
            if (configOptional.isPresent) {
                val config = mapToCoreConfig(configOptional.get().config!!)

                val ids =
                    config.expectedDataStreamIds.map { dataStream: DataStreamId ->
                        dataStreamIdRepository.findByStudyDeploymentIdAndDeviceRoleNameAndNameAndNameSpace(
                            studyDeploymentId = dataStream.studyDeploymentId.stringRepresentation,
                            deviceRoleName = dataStream.deviceRoleName,
                            name = dataStream.dataType.name,
                            nameSpace = dataStream.dataType.namespace,
                        ).get().id
                    }
                // delete data streams
                dataStreamSequenceRepository.deleteAllByDataStreamIds(ids)
                dataStreamIdRepository.deleteAllByDataStreamIds(ids)
                deploymentIds.add(it)
            }
        }
        return deploymentIds
    }

    private fun mapToCoreConfig(node: JsonNode) = objectMapper.treeToValue(node, DataStreamsConfiguration::class.java)

    private fun mapToDataStreamSnapshot(node: JsonNode) = objectMapper.treeToValue(node, DataStreamSnapshot::class.java)
}
