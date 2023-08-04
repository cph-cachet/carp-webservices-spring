package dk.cachet.carp.webservices.protocol.repository

import dk.cachet.carp.webservices.protocol.domain.Protocol
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * The Interface [ProtocolRepository].
 * The [ProtocolRepository] implements the [Protocol] interface for retrieving the [Protocol].
 */
@Repository
interface ProtocolRepository: JpaRepository<Protocol, String>
{
    /**
     * The [findAllByOwnerId] function retrieves all the [Protocol]s associated with the given [ownerId].
     *
     * @param ownerId The [ownerId] of the [Protocol]'s to retrieve.
     * @return A list of [Protocol]s associated with the given [ownerId].
     */
    @Query(value = "SELECT DISTINCT ON (snapshot->>'id') snapshot->>'id', * " +
            "FROM protocols " +
            "where snapshot->>'ownerId'= ?1 " +
            "ORDER BY (snapshot->>'id'), created_at DESC ", nativeQuery = true)
    fun findAllByOwnerId(ownerId: String): List<Protocol>

    /**
     * The [findByParams] function retrieves the [Protocol] associated with the given [id],
     * and optionally by [versionTag].
     *
     * @param id The [id] of the [Protocol]'s to retrieve.
     * @param versionTag The [versionTag] version of the [Protocol].
     * @return A list of [Protocol] for the given parameters.
     */
    @Query(value = "SELECT * FROM protocols WHERE snapshot->>'id' = ?1" +
            " AND (?2 IS NULL OR version_tag = cast(?2 as varchar))" +
            " ORDER BY created_at DESC",
            nativeQuery = true)
    fun findByParams(id: String, versionTag: String?): List<Protocol>

    /**
     * The [findById] function retrieves the [Protocol] associated with the given [id]
     *
     * @param id The [id] of the [Protocol]'s to retrieve.
     *
     * @return [Protocol] for the given [id].
     */
    @Query(value = "SELECT * FROM protocols WHERE snapshot->>'id' = ?1", nativeQuery = true)
    fun findByIdParam(id: String): List<Protocol>

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "DELETE FROM protocols WHERE snapshot->>'id' = :protocolId")
    fun deleteByProtocolId(@Param(value = "protocolId") protocolId: String)


    /**
     * The [findLatestById] function retrieves the most recent [Protocol] by the given protocol [id].
     *
     * @param id of the protocol (UUID from snapshot)
     * @return [Protocol] associated with the given [id].
     */
    @Query(value = "SELECT * " +
            " FROM protocols " +
            " where snapshot->>'id'= ?1 " +
            " ORDER BY (snapshot->>'id'), created_at DESC " +
            " LIMIT 1", nativeQuery = true)
    fun findLatestById(id: String): Optional<Protocol>

    /**
     * The [findFirstById] function retrieves the first version of [Protocol] by the given protocol [id].
     *
     * @param id of the protocol (UUID from snapshot)
     * @return [Protocol] associated with the given [id].
     */
    @Query(value = "SELECT * " +
            " FROM protocols " +
            " where snapshot->>'id'= ?1 " +
            " ORDER BY (snapshot->>'id'), created_at ASC " +
            " LIMIT 1", nativeQuery = true)
    fun findFirstById(id: String): Optional<Protocol>


}
