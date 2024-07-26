package dk.cachet.carp.webservices.common.input

import dk.cachet.carp.common.application.data.Data
import dk.cachet.carp.common.application.data.input.InputDataType
import dk.cachet.carp.common.application.data.input.InputDataTypeList
import dk.cachet.carp.common.application.data.input.elements.Text
import dk.cachet.carp.common.infrastructure.serialization.createDefaultJSON
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

object WSInputDataTypes : InputDataTypeList() {
    /**
     * The [WSInputDataTypes] namespace of all Webservice input data type definitions.
     */
    private const val WS_NAMESPACE: String = "dk.carp.webservices.input"

    private val WS_MODULE =
        SerializersModule {
            polymorphic(Data::class) {
                subclass(InformedConsent::class)
            }
        }

    val WS_JSON = createDefaultJSON(WS_MODULE)

    internal const val IC_TYPE_NAME = "$WS_NAMESPACE.informed_consent"

    /**
     * Informed consent signed by participant.
     */

    val INFORMED_CONSENT =
        add(
            inputDataType = InputDataType.fromString(IC_TYPE_NAME),
            inputElement = Text("Informed Consent"),
            dataClass = InformedConsent::class,
            inputToData = { WS_JSON.decodeFromString(InformedConsent.serializer(), it) },
            dataToInput = { WS_JSON.encodeToString(InformedConsent.serializer(), it) },
        )
}
