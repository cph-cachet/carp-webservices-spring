package dk.cachet.carp.webservices.common.input

import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.infrastructure.serialization.JSON

object WSInputDataTypes : InputDataTypeList() {
    /**
     * The [WSInputDataTypes] namespace of all Webservice input data type definitions.
     */
    private const val WS_NAMESPACE: String = "dk.carp.webservices.input"

    internal const val IC_TYPE_NAME = "$WS_NAMESPACE.informed_consent"

    /**
     * Informed consent signed by participant.
     */
    val INFORMED_CONSENT =
        add(
            inputDataType = InputDataType.fromString(IC_TYPE_NAME),
            inputElement = Text("Informed Consent"),
            dataClass = InformConsent::class,
            inputToData = { JSON.decodeFromString(InformConsent.serializer(), it) },
            dataToInput = { JSON.encodeToString(InformConsent.serializer(), it) },
        )
}
