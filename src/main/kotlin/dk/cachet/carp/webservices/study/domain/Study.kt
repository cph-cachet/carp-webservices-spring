package dk.cachet.carp.webservices.study.domain

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import dk.cachet.carp.webservices.common.audit.Auditable
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.Type
import org.hibernate.type.SqlTypes

@Entity(name = "studies")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Study
(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Int = 0,

        /** The core StudyProtocolSnapshot class ([snapshot]) as a JsonNode. */
        @JdbcTypeCode(SqlTypes.JSON)
        @Column(columnDefinition = "jsonb")
        @Type(JsonBinaryType::class)
        var snapshot: JsonNode? = null,

        @ElementCollection(fetch = FetchType.EAGER)
        @CollectionTable(name = "studies_researcher_account_ids", joinColumns = [JoinColumn(name = "studies_id")])
        var researcherAccountIds: MutableSet<String> = mutableSetOf()
): Auditable()