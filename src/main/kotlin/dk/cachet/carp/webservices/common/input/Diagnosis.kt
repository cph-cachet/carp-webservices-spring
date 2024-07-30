package dk.cachet.carp.webservices.common.input

import dk.cachet.carp.common.application.data.Data
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a diagnosis of a participant.
 *
 * We are using the
 * WHO [ICD-11](https://www.who.int/standards/classifications/classification-of-diseases) classification.
 */
@Serializable
@SerialName(WSInputDataTypes.DIAGNOSIS_TYPE_NAME)
data class Diagnosis(
    /**
     * The date when the diagnosis was effective.
     */
    val effectiveDate: Instant?,
    /**
     * A free text description of the diagnosis.
     */
    val diagnosis: String?,
    /**
     * The [ICD-11](https://www.who.int/standards/classifications/classification-of-diseases) code of the diagnosis.
     */
    val icd11Code: String,
    /**
     * Any conclusion or notes from the physician.
     */
    val conclusion: String?,
) : Data
